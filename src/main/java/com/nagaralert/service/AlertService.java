package com.nagaralert.service;

import com.nagaralert.model.Alert;
import com.nagaralert.model.AlertStatus;
import java.util.List;

/**
 * Interface for alert management services.
 */
public interface AlertService {
    /**
     * Reports a new alert.
     *
     * @param alert The alert to report
     * @return The reported Alert object, or null if duplicate/invalid
     */
    Alert reportAlert(Alert alert);

    /**
     * Searches for alerts by location.
     *
     * @param location The location to search for
     * @return A list of alerts at the specified location
     */
    List<Alert> searchByLocation(String location);

    /**
     * Retrieves all alerts.
     *
     * @return A list of all alerts
     */
    List<Alert> getAllAlerts();

    /**
     * Retrieves alerts by department.
     *
     * @param department The department name
     * @return List of alerts for the department
     */
    List<Alert> getAlertsByDepartment(String department);

    /**
     * Retrieves alerts by phone number.
     *
     * @param phoneNumber The phone number to search for
     * @return List of alerts for the phone number
     */
    List<Alert> getAlertsByPhoneNumber(String phoneNumber);

    /**
     * Updates the status of an alert.
     *
     * @param alertId The ID of the alert
     * @param status The new status
     * @return true if successful
     */
    boolean updateStatus(String alertId, AlertStatus status);

    /**
     * Deletes an alert.
     *
     * @param alertId The ID of the alert to delete
     */
    void deleteAlert(String alertId);

    /**
     * Upvotes an alert.
     *
     * @param alertId The ID of the alert to upvote
     */
    void upvoteAlert(String alertId);
}
