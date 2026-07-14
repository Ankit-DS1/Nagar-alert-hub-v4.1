package com.nagaralert.controller;

import com.nagaralert.model.Alert;
import com.nagaralert.model.Severity;
import com.nagaralert.service.AlertService;
import com.nagaralert.util.SeverityDetector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    private final AlertService alertService;
    private final com.nagaralert.repository.AppUserRepository appUserRepository;

    public HomeController(AlertService alertService, com.nagaralert.repository.AppUserRepository appUserRepository) {
        this.alertService = alertService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Alert> alerts;
        try {
            alerts = alertService.getAllAlerts();
        } catch (Exception e) {
            System.err.println("HomeController: Could not load alerts (DB unavailable): " + e.getMessage());
            alerts = java.util.Collections.emptyList();
        }
        long criticalCount = alerts.stream()
                .filter(a -> a.getSeverity() == Severity.CRITICAL)
                .count();
        long activeCount = alerts.stream()
                .filter(a -> a.getStatus() == com.nagaralert.model.AlertStatus.PENDING || a.getStatus() == com.nagaralert.model.AlertStatus.IN_PROGRESS)
                .count();
        long resolvedCount = alerts.stream()
                .filter(a -> a.getStatus() == com.nagaralert.model.AlertStatus.RESOLVED)
                .count();

        model.addAttribute("alerts", alerts);
        model.addAttribute("criticalCount", criticalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("resolvedCount", resolvedCount);
        return "index";
    }

    @PostMapping("/report")
    public String reportAlert(@ModelAttribute Alert alert, @RequestParam(value = "image", required = false) MultipartFile image,
                              org.springframework.security.core.Authentication authentication) {
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setTimestamp(LocalDateTime.now());
        alert.setSeverity(SeverityDetector.determineSeverity(alert.getDescription()));

        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = null;
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken auth = null;
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            auth = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
            oauth2User = auth.getPrincipal();
        }

        if (oauth2User != null && auth != null) {
            String provider = auth.getAuthorizedClientRegistrationId().toUpperCase();
            String oauthId = oauth2User.getName();
            if (provider.equals("GOOGLE") && oauth2User.getAttribute("sub") != null) {
                oauthId = oauth2User.getAttribute("sub");
            } else if (oauth2User.getAttribute("id") != null) {
                oauthId = oauth2User.getAttribute("id").toString();
            }

            java.util.Optional<com.nagaralert.model.AppUser> appUserOpt = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider);
            if (appUserOpt.isPresent() && appUserOpt.get().getMobileNumber() != null && !appUserOpt.get().getMobileNumber().isBlank()) {
                alert.setPhoneNumber(appUserOpt.get().getMobileNumber());
            }
        }

        if (image != null && !image.isEmpty()) {
            try {
                Path uploadDir = Paths.get("uploads");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                String filename = alert.getAlertId() + "_" + image.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                Path filePath = uploadDir.resolve(filename);
                Files.copy(image.getInputStream(), filePath);
                alert.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        alertService.reportAlert(alert);

        return "redirect:/";
    }

    @PostMapping("/alert/{id}/upvote")
    public String upvoteAlert(@org.springframework.web.bind.annotation.PathVariable String id) {
        alertService.upvoteAlert(id);
        return "redirect:/";
    }

    @GetMapping("/my-alerts")
    public String myAlerts(@RequestParam(required = false) String phone, Model model,
                           org.springframework.security.core.Authentication authentication) {
        
        String targetPhone = phone;

        org.springframework.security.oauth2.core.user.OAuth2User oauth2User = null;
        org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken auth = null;
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            auth = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
            oauth2User = auth.getPrincipal();
        }

        if (oauth2User != null && auth != null && (targetPhone == null || targetPhone.isBlank())) {
            String provider = auth.getAuthorizedClientRegistrationId().toUpperCase();
            String oauthId = oauth2User.getName();
            if (provider.equals("GOOGLE") && oauth2User.getAttribute("sub") != null) {
                oauthId = oauth2User.getAttribute("sub");
            } else if (oauth2User.getAttribute("id") != null) {
                oauthId = oauth2User.getAttribute("id").toString();
            }

            java.util.Optional<com.nagaralert.model.AppUser> appUserOpt = appUserRepository.findByOauthIdAndOauthProvider(oauthId, provider);
            if (appUserOpt.isPresent() && appUserOpt.get().getMobileNumber() != null && !appUserOpt.get().getMobileNumber().isBlank()) {
                targetPhone = appUserOpt.get().getMobileNumber();
            }
        }

        if (targetPhone != null && !targetPhone.isBlank()) {
            List<Alert> myAlerts = alertService.getAlertsByPhoneNumber(targetPhone);
            model.addAttribute("alerts", myAlerts);
            model.addAttribute("phone", targetPhone);
        }
        return "my-alerts";
    }
}
