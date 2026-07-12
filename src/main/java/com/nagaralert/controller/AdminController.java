package com.nagaralert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nagaralert.model.Alert;
import com.nagaralert.model.AlertStatus;
import com.nagaralert.model.Severity;
import com.nagaralert.repository.AppUserRepository;
import com.nagaralert.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    private final AlertService alertService;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminController(AlertService alertService, AppUserRepository appUserRepository) {
        this.alertService = alertService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, @RequestParam(required = false, defaultValue = "ALL") String dept) {
        model.addAttribute("alerts", alertService.getAlertsByDepartment(dept));
        model.addAttribute("deptName", dept);
        model.addAttribute("activeTab", "queue");
        return "admin";
    }

    @GetMapping("/admin/dashboard")
    public String analyticsDashboard(Model model, @RequestParam(required = false, defaultValue = "ALL") String dept) {
        populateDashboardMetrics(model, dept);
        model.addAttribute("activeTab", "dashboard");
        return "dashboard";
    }

    @GetMapping("/admin/users")
    public String userManagement(Model model, @RequestParam(required = false, defaultValue = "ALL") String dept) {
        model.addAttribute("users", appUserRepository.findAll());
        model.addAttribute("deptName", dept);
        model.addAttribute("activeTab", "users");
        return "admin-users";
    }

    /** Real-time metrics API for dashboard polling */
    @GetMapping("/admin/api/metrics")
    @ResponseBody
    public Map<String, Object> metricsApi(@RequestParam(required = false, defaultValue = "ALL") String dept) {
        List<Alert> allAlerts = alertService.getAlertsByDepartment(dept);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        long totalAlerts = allAlerts.size();
        long alertsToday = allAlerts.stream()
                .filter(a -> a.getTimestamp() != null && a.getTimestamp().isAfter(startOfDay))
                .count();
        long criticalCount = allAlerts.stream()
                .filter(a -> a.getSeverity() == Severity.CRITICAL
                        && a.getStatus() != AlertStatus.RESOLVED)
                .count();
        long resolved24h = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED
                        && a.getResolvedAt() != null
                        && a.getResolvedAt().isAfter(last24h))
                .count();
        long resolvedAlerts = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED)
                .count();
        double resolutionRate = totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0.0;

        double avgResolutionHours = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED
                        && a.getTimestamp() != null && a.getResolvedAt() != null)
                .mapToLong(a -> ChronoUnit.MINUTES.between(a.getTimestamp(), a.getResolvedAt()))
                .average()
                .orElse(0) / 60.0;

        Map<String, Long> byDept = allAlerts.stream()
                .filter(a -> a.getDepartment() != null)
                .collect(Collectors.groupingBy(Alert::getDepartment, Collectors.counting()));

        Map<String, Long> bySeverity = allAlerts.stream()
                .filter(a -> a.getSeverity() != null)
                .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()));

        // 24-hour trend (hourly buckets)
        Map<String, Long> hourlyTrend = new LinkedHashMap<>();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = LocalDateTime.now().minusHours(i).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            String label = hourStart.getHour() + ":00";
            long count = allAlerts.stream()
                    .filter(a -> a.getTimestamp() != null
                            && !a.getTimestamp().isBefore(hourStart)
                            && a.getTimestamp().isBefore(hourEnd))
                    .count();
            hourlyTrend.put(label, count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalAlerts", totalAlerts);
        result.put("alertsToday", alertsToday);
        result.put("criticalCount", criticalCount);
        result.put("resolved24h", resolved24h);
        result.put("resolutionRate", Math.round(resolutionRate));
        result.put("avgResolutionHours", Math.round(avgResolutionHours * 10.0) / 10.0);
        result.put("byDept", byDept);
        result.put("bySeverity", bySeverity);
        result.put("hourlyTrend", hourlyTrend);
        result.put("queueCount", allAlerts.stream()
                .filter(a -> a.getStatus() != AlertStatus.RESOLVED).count());
        return result;
    }

    /** AJAX status update — triggers WhatsApp silently */
    @PostMapping("/admin/api/status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatusApi(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            AlertStatus newStatus = AlertStatus.valueOf(status);
            boolean ok = alertService.updateStatus(id, newStatus);
            return ResponseEntity.ok(Map.of("success", ok, "status", status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid status"));
        }
    }

    @PostMapping("/admin/verify/{id}")
    public String verifyAlert(@PathVariable String id, @RequestParam(required = false) String dept) {
        alertService.updateStatus(id, AlertStatus.IN_PROGRESS);
        return "redirect:/admin" + (dept != null ? "?dept=" + dept : "");
    }

    @PostMapping("/admin/resolve/{id}")
    public String resolveAlert(@PathVariable String id, @RequestParam(required = false) String dept) {
        alertService.updateStatus(id, AlertStatus.RESOLVED);
        return "redirect:/admin" + (dept != null ? "?dept=" + dept : "");
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteAlert(@PathVariable String id, @RequestParam(required = false) String dept) {
        alertService.deleteAlert(id);
        return "redirect:/admin" + (dept != null ? "?dept=" + dept : "");
    }

    @PostMapping("/admin/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAlertApi(@PathVariable String id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void populateDashboardMetrics(Model model, String dept) {
        List<Alert> allAlerts = alertService.getAlertsByDepartment(dept);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        long totalAlerts = allAlerts.size();
        long alertsToday = allAlerts.stream()
                .filter(a -> a.getTimestamp() != null && a.getTimestamp().isAfter(startOfDay))
                .count();
        long criticalCount = allAlerts.stream()
                .filter(a -> a.getSeverity() == Severity.CRITICAL
                        && a.getStatus() != AlertStatus.RESOLVED)
                .count();
        long resolved24h = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED
                        && a.getResolvedAt() != null
                        && a.getResolvedAt().isAfter(last24h))
                .count();
        long resolvedAlerts = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED)
                .count();
        double resolutionRate = totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0.0;

        double avgResolutionHours = allAlerts.stream()
                .filter(a -> a.getStatus() == AlertStatus.RESOLVED
                        && a.getTimestamp() != null && a.getResolvedAt() != null)
                .mapToLong(a -> ChronoUnit.MINUTES.between(a.getTimestamp(), a.getResolvedAt()))
                .average()
                .orElse(0) / 60.0;

        Map<String, Long> byDept = allAlerts.stream()
                .filter(a -> a.getDepartment() != null)
                .collect(Collectors.groupingBy(Alert::getDepartment, Collectors.counting()));

        Map<String, Long> bySeverity = allAlerts.stream()
                .filter(a -> a.getSeverity() != null)
                .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()));

        Map<String, Long> hourlyTrend = new LinkedHashMap<>();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = LocalDateTime.now().minusHours(i).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            String label = hourStart.getHour() + ":00";
            long count = allAlerts.stream()
                    .filter(a -> a.getTimestamp() != null
                            && !a.getTimestamp().isBefore(hourStart)
                            && a.getTimestamp().isBefore(hourEnd))
                    .count();
            hourlyTrend.put(label, count);
        }

        model.addAttribute("totalAlerts", totalAlerts);
        model.addAttribute("alertsToday", alertsToday);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("resolved24h", resolved24h);
        model.addAttribute("resolutionRate", Math.round(resolutionRate));
        model.addAttribute("avgResolutionHours", Math.round(avgResolutionHours * 10.0) / 10.0);
        model.addAttribute("deptName", dept);

        try {
            model.addAttribute("deptDataJson", objectMapper.writeValueAsString(byDept));
            model.addAttribute("severityDataJson", objectMapper.writeValueAsString(bySeverity));
            model.addAttribute("trendDataJson", objectMapper.writeValueAsString(hourlyTrend));
        } catch (Exception e) {
            model.addAttribute("deptDataJson", "{}");
            model.addAttribute("severityDataJson", "{}");
            model.addAttribute("trendDataJson", "{}");
        }
    }
}
