package com.nagaralert.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents an alert in the Nagar Alert Hub system.
 * Implements Serializable for object serialization.
 */
@Document(collection = "alerts")
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String alertId;
    
    private String description;
    
    private String location;
    
    private Severity severity;
    
    private String department;
    private LocalDateTime timestamp;
    
    private AlertStatus status;
    
    private Double latitude;
    private Double longitude;
    private int upvotes;
    
    @Field("phone_number")
    private String phoneNumber;

    @Field("ai_reason")
    private String aiReason;

    @Field("image_url")
    private String imageUrl;

    @Field("potential_duplicate")
    private boolean potentialDuplicate;

    @Field("duplicate_of_id")
    private String duplicateOfId;

    @Field("resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Default constructor.
     */
    public Alert() {
    }

    /**
     * Parameterized constructor.
     *
     * @param alertId     Unique identifier for the alert
     * @param description Brief description of the alert
     * @param location    Location where the alert occurred
     * @param severity    Severity level of the alert
     * @param department  Department handling the alert
     * @param timestamp   Time when the alert occurred
     * @param status      Current status of the alert
     * @param latitude    Latitude coordinate
     * @param longitude   Longitude coordinate
     * @param upvotes     Number of upvotes from citizens
     * @param phoneNumber Citizen's phone number
     */
    public Alert(String alertId, String description, String location, Severity severity, String department,
            LocalDateTime timestamp, AlertStatus status, Double latitude, Double longitude, int upvotes, String phoneNumber) {
        this.alertId = alertId;
        this.description = description;
        this.location = location;
        this.severity = severity;
        this.department = department;
        this.timestamp = timestamp;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.upvotes = upvotes;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the alert ID.
     *
     * @return the alert ID
     */
    public String getAlertId() {
        return alertId;
    }

    /**
     * Sets the alert ID.
     *
     * @param alertId the alert ID to set
     */
    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location.
     *
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity.
     *
     * @param severity the severity to set
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Gets the department.
     *
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the department.
     *
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public AlertStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status to set
     */
    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAiReason() {
        return aiReason;
    }

    public void setAiReason(String aiReason) {
        this.aiReason = aiReason;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPotentialDuplicate() {
        return potentialDuplicate;
    }

    public void setPotentialDuplicate(boolean potentialDuplicate) {
        this.potentialDuplicate = potentialDuplicate;
    }

    public String getDuplicateOfId() {
        return duplicateOfId;
    }

    public void setDuplicateOfId(String duplicateOfId) {
        this.duplicateOfId = duplicateOfId;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "alertId='" + alertId + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", severity=" + severity +
                ", department='" + department + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", upvotes=" + upvotes +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
