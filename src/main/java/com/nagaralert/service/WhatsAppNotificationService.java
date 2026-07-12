package com.nagaralert.service;

import com.nagaralert.model.Alert;
import com.nagaralert.model.AlertStatus;
import com.nagaralert.model.Severity;
import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class WhatsAppNotificationService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String twilioWhatsAppNumber;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a");

    @PostConstruct
    public void init() {
        if (!"AC_PLACEHOLDER_SID".equals(accountSid) && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
            System.out.println("Twilio initialized successfully for WhatsApp notifications.");
        } else {
            System.out.println("Twilio credentials not configured. WhatsApp notifications are disabled.");
        }
    }

    public void sendStatusUpdate(Alert alert, boolean isNewAlert) {
        if ("AC_PLACEHOLDER_SID".equals(accountSid) || accountSid.isEmpty()) {
            System.out.println("Skipping WhatsApp notification: Twilio is not configured.");
            return;
        }

        String toPhoneNumber = alert.getPhoneNumber();
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            return;
        }

        // Format phone number for India if it's just 10 digits
        String formattedNumber = toPhoneNumber.trim();
        if (formattedNumber.length() == 10 && formattedNumber.matches("\\d+")) {
            formattedNumber = "+91" + formattedNumber;
        }
        
        // Ensure it has the whatsapp: prefix
        if (!formattedNumber.startsWith("whatsapp:")) {
            formattedNumber = "whatsapp:" + formattedNumber;
        }

        String body = buildMessage(alert, isNewAlert);

        try {
            Message message = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(twilioWhatsAppNumber),
                    body
            ).create();
            
            System.out.println("WhatsApp message sent successfully. SID: " + message.getSid());
        } catch (TwilioException e) {
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
        }
    }

    private String buildMessage(Alert alert, boolean isNewAlert) {
        if (isNewAlert) {
            return submissionConfirmation(alert);
        }
        return switch (alert.getStatus()) {
            case RESOLVED    -> resolutionCertificate(alert);
            case IN_PROGRESS -> statusUpdate(alert);
            default          -> statusUpdate(alert);
        };
    }

    private String submissionConfirmation(Alert alert) {
        return """
                🏛️ *Nagar Alert Hub — Complaint Received*
                ━━━━━━━━━━━━━━━━━━━━
                📋 *ID:* #A-%s
                📍 *Location:* %s
                🏢 *Routed to:* %s Department
                ⚡ *Severity:* %s %s
                📊 *Status:* ⏳ PENDING — Awaiting review
                ━━━━━━━━━━━━━━━━━━━━
                🕐 Received: %s IST

                Your complaint has been registered and routed to the *%s Department* using AI-powered classification.

                Track your complaint anytime:
                👉 Visit /my-alerts and enter your phone number

                _Reply STOP to unsubscribe from updates._
                """.formatted(
                alert.getAlertId(),
                alert.getLocation(),
                alert.getDepartment(),
                severityEmoji(alert.getSeverity()), alert.getSeverity(),
                alert.getTimestamp() != null ? alert.getTimestamp().format(FMT) : java.time.LocalDateTime.now().format(FMT),
                alert.getDepartment()
        );
    }

    private String statusUpdate(Alert alert) {
        String statusLine = switch (alert.getStatus()) {
            case IN_PROGRESS -> "🛠️ *IN PROGRESS* — Department is working on it";
            case RESOLVED    -> "✅ *RESOLVED* — Issue has been fixed!";
            default          -> "⏳ *PENDING* — Awaiting assignment";
        };

        String timeline = buildTimeline(alert.getStatus());

        return """
                📡 *Nagar Alert Hub — Complaint Update*
                ━━━━━━━━━━━━━━━━━━━━
                📋 *ID:* #A-%s
                📍 *Location:* %s
                🏢 *Department:* %s
                ━━━━━━━━━━━━━━━━━━━━
                %s

                *Progress Timeline:*
                %s
                ━━━━━━━━━━━━━━━━━━━━
                🕐 Updated: %s IST

                Track full history: /my-alerts
                _Reply STOP to unsubscribe._
                """.formatted(
                alert.getAlertId(),
                alert.getLocation(),
                alert.getDepartment(),
                statusLine,
                timeline,
                java.time.LocalDateTime.now().format(FMT)
        );
    }

    private String resolutionCertificate(Alert alert) {
        return """
                ✅ *Complaint Resolved — Nagar Alert Hub*
                ━━━━━━━━━━━━━━━━━━━━
                🎉 *Great news!* Your complaint has been resolved.

                📋 *Complaint:* #A-%s
                📍 *Location:* %s
                🏢 *Resolved by:* %s Department
                ⚡ *Original Severity:* %s %s

                ┌─────────────────────┐
                │  📅 Reported: %s
                │  ✅ Resolved: %s IST
                └─────────────────────┘

                🙏 *Thank you for making %s safer!*

                Your report helped your community. Keep reporting issues to keep your city clean and safe. 💪

                ⭐ Rate this resolution: /feedback
                📊 View your history: /my-alerts

                *— NagarAlertHub Team*
                _Empowering communities through civic tech_
                """.formatted(
                alert.getAlertId(),
                alert.getLocation(),
                alert.getDepartment(),
                severityEmoji(alert.getSeverity()), alert.getSeverity(),
                alert.getTimestamp() != null ? alert.getTimestamp().format(FMT) : "",
                java.time.LocalDateTime.now().format(FMT),
                extractCity(alert.getLocation())
        );
    }

    private String buildTimeline(AlertStatus status) {
        return switch (status) {
            case PENDING -> """
                    ✅ Submitted
                    ⬇️
                    ⏳ In Progress ← *Waiting*
                    ⬇️
                    ⭕ Resolved""";
            case IN_PROGRESS -> """
                    ✅ Submitted
                    ⬇️
                    🛠️ In Progress ← *Current*
                    ⬇️
                    ⭕ Resolved — Coming soon""";
            case RESOLVED -> """
                    ✅ Submitted
                    ⬇️
                    ✅ In Progress
                    ⬇️
                    🎉 Resolved ← *Done!*""";
        };
    }

    private String severityEmoji(Severity severity) {
        if (severity == null) return "⚠️";
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH     -> "🟠";
            case MEDIUM   -> "🟡";
            case LOW      -> "🟢";
        };
    }

    private String extractCity(String location) {
        if (location == null || location.isBlank()) return "your city";
        String[] parts = location.split("[,\\s]+");
        return parts[parts.length - 1];
    }
}
