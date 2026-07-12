package com.nagaralert.util;

import com.nagaralert.model.Alert;

/**
 * Utility class to validate alert inputs.
 */
public class InputValidator {

    /**
     * Validates an alert object.
     *
     * @param alert The alert to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidAlert(Alert alert) {
        if (alert == null) {
            return false;
        }

        if (alert.getAlertId() == null || alert.getAlertId().trim().isEmpty()) {
            return false;
        }

        if (alert.getDescription() == null || alert.getDescription().trim().isEmpty()) {
            return false;
        }

        if (alert.getLocation() == null || alert.getLocation().trim().isEmpty()) {
            return false;
        }

        if (alert.getSeverity() == null) {
            return false;
        }

        return true;
    }
}
