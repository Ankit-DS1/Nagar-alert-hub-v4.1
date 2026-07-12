package com.nagaralert.model;

/**
 * Represents an admin user who can verify alerts.
 */
public class Admin extends User {

    /**
     * Default constructor.
     */
    public Admin() {
    }

    /**
     * Parameterized constructor.
     *
     * @param name   The admin's name
     * @param userId The admin's unique ID
     */
    public Admin(String name, String userId) {
        super(name, userId);
    }

    /**
     * Verifies an alert.
     *
     * @param alert The alert to verify
     */
    public void verifyAlert(Alert alert) {
        if (alert != null) {
            alert.setStatus(AlertStatus.IN_PROGRESS);
            System.out.println("Alert " + alert.getAlertId() + " has been verified by Admin " + getName());
        }
    }

    @Override
    public String toString() {
        return "Admin{" +
                "name='" + getName() + '\'' +
                ", userId='" + getUserId() + '\'' +
                '}';
    }
}
