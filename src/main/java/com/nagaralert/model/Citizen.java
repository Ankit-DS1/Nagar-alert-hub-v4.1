package com.nagaralert.model;

/**
 * Represents a citizen user who can draft alerts.
 */
public class Citizen extends User {

    /**
     * Default constructor.
     */
    public Citizen() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name   The citizen's name
     * @param userId The citizen's unique ID
     */
    public Citizen(String name, String userId) {
        super(name, userId);
    }

    /**
     * Drafts a new alert.
     *
     * @param alertId     The ID for the new alert
     * @param description The description of the alert
     * @param location    The location of the alert
     * @param severity    The severity of the alert
     * @return The created Alert object
     */
    public Alert draftAlert(String alertId, String description, String location, Severity severity) {
        Alert alert = new Alert();
        alert.setAlertId(alertId);
        alert.setDescription(description);
        alert.setLocation(location);
        alert.setSeverity(severity);
        alert.setTimestamp(java.time.LocalDateTime.now());
        alert.setStatus(AlertStatus.PENDING); // Alerts are pending by default
        return alert;
    }

    @Override
    public String toString() {
        return "Citizen{" +
                "name='" + getName() + '\'' +
                ", userId='" + getUserId() + '\'' +
                '}';
    }
}
