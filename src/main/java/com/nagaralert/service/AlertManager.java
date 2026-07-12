package com.nagaralert.service;

import com.nagaralert.model.Alert;
import com.nagaralert.model.AlertStatus;
import com.nagaralert.repository.AlertRepository;
import com.nagaralert.util.AiDepartmentClassifier;
import com.nagaralert.util.GeoUtils;
import com.nagaralert.util.InputValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Implementation of AlertService using Spring Data JPA.
 */
@Service
public class AlertManager implements AlertService {

    private static final double DUPLICATE_RADIUS_METERS = 50.0;

    private final AlertRepository alertRepository;
    private final InputValidator inputValidator;
    private final AiDepartmentClassifier departmentClassifier;
    private final WhatsAppNotificationService whatsappService;

    public AlertManager(AlertRepository alertRepository, AiDepartmentClassifier departmentClassifier, WhatsAppNotificationService whatsappService) {
        this.alertRepository = alertRepository;
        this.inputValidator = new InputValidator();
        this.departmentClassifier = departmentClassifier;
        this.whatsappService = whatsappService;
    }

    @Override
    public Alert reportAlert(Alert alert) {
        if (!inputValidator.isValidAlert(alert)) {
            System.out.println("Invalid alert reported.");
            return null;
        }

        // Classification
        AiDepartmentClassifier.ClassificationResult aiResult = departmentClassifier.classifyDepartment(alert.getDescription());
        alert.setDepartment(aiResult.department());
        alert.setAiReason(aiResult.reason());

        // Spatial duplicate flagging (same dept within 50m radius)
        flagSpatialDuplicate(alert);

        // Set initial state
        alert.setStatus(AlertStatus.PENDING);
        alert.setUpvotes(0);

        Alert savedAlert = alertRepository.save(alert);
        System.out.println("Alert reported: " + alert.getAlertId() + " [Dept: " + aiResult.department() + "]");

        // Send WhatsApp notification asynchronously if phone is present
        if (alert.getPhoneNumber() != null && !alert.getPhoneNumber().isEmpty()) {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    whatsappService.sendStatusUpdate(alert, true);
                } catch (Exception e) {
                    System.err.println("Async WhatsApp error: " + e.getMessage());
                }
            });
        }

        return savedAlert;
    }

    @Override
    public boolean updateStatus(String alertId, AlertStatus status) {
        Optional<Alert> optAlert = alertRepository.findById(alertId);
        if (optAlert.isPresent()) {
            Alert alert = optAlert.get();
            AlertStatus oldStatus = alert.getStatus();
            alert.setStatus(status);
            if (status == AlertStatus.RESOLVED) {
                alert.setResolvedAt(LocalDateTime.now());
            }
            alertRepository.save(alert);
            
            // Send WhatsApp notification asynchronously if status changed and phone is present
            if (oldStatus != status && alert.getPhoneNumber() != null && !alert.getPhoneNumber().isEmpty()) {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        whatsappService.sendStatusUpdate(alert, false);
                    } catch (Exception e) {
                        System.err.println("Async WhatsApp error: " + e.getMessage());
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public void upvoteAlert(String alertId) {
        Optional<Alert> optAlert = alertRepository.findById(alertId);
        if (optAlert.isPresent()) {
            Alert alert = optAlert.get();
            alert.setUpvotes(alert.getUpvotes() + 1);
            alertRepository.save(alert);
        }
    }

    @Override
    public List<Alert> searchByLocation(String location) {
        if (location == null) {
            return List.of();
        }
        return alertRepository.findByLocation(location);
    }

    @Override
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @Override
    public List<Alert> getAlertsByDepartment(String department) {
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("ALL")) {
            return getAllAlerts();
        }
        
        // Normalize department casing (e.g. "FIRE" -> "Fire") to match database entries
        String normalizedDept = department.substring(0, 1).toUpperCase() + department.substring(1).toLowerCase();
        
        return alertRepository.findByDepartment(normalizedDept);
    }

    @Override
    public List<Alert> getAlertsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return List.of();
        }
        return alertRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public void deleteAlert(String alertId) {
        if (alertId != null && alertRepository.existsById(alertId)) {
            alertRepository.deleteById(alertId);
            System.out.println("Alert deleted and changes persisted: " + alertId);
        } else {
            System.out.println("Alert with ID " + alertId + " not found for deletion.");
        }
    }

    private void flagSpatialDuplicate(Alert alert) {
        if (alert.getLatitude() == null || alert.getLongitude() == null || alert.getDepartment() == null) {
            return;
        }
        List<Alert> sameDept = alertRepository.findByDepartment(alert.getDepartment());
        for (Alert existing : sameDept) {
            if (existing.getLatitude() == null || existing.getLongitude() == null) continue;
            if (existing.getStatus() == AlertStatus.RESOLVED) continue;
            double dist = GeoUtils.distanceMeters(
                    alert.getLatitude(), alert.getLongitude(),
                    existing.getLatitude(), existing.getLongitude());
            if (dist <= DUPLICATE_RADIUS_METERS) {
                alert.setPotentialDuplicate(true);
                alert.setDuplicateOfId(existing.getAlertId());
                System.out.println("Spatial duplicate flagged: " + alert.getAlertId() + " near " + existing.getAlertId());
                return;
            }
        }
    }

    /**
     * Periodically cleans up alerts older than 24 hours.
     */
    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    public void autoCleanup() {
        System.out.println("AutoCleanup: Starting cleanup process...");
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        
        List<Alert> toDelete = alertRepository.findByStatusAndTimestampBefore(AlertStatus.RESOLVED, cutoffTime);
        
        if (!toDelete.isEmpty()) {
            alertRepository.deleteAll(toDelete);
            System.out.println("AutoCleanup: Cleanup finished. Total old resolved complaints removed: " + toDelete.size());
        } else {
            System.out.println("AutoCleanup: No eligible resolved complaints older than 24 hours found.");
        }
    }
}
