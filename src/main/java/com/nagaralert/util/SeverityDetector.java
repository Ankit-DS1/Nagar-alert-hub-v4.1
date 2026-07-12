package com.nagaralert.util;

import com.nagaralert.model.Severity;

/**
 * Utility class to automatically detect severity based on keywords.
 * Acts as a logic brain for the Nagar Alert Hub.
 */
public class SeverityDetector {

    // Keywords mapping
    private static final String[] CRITICAL_KEYWORDS = { "fire", "blast", "explosion", "blood", "death" };
    private static final String[] HIGH_KEYWORDS = { "accident", "robbery", "blocked", "electric" };
    private static final String[] MEDIUM_KEYWORDS = { "traffic", "jam", "water", "leak", "garbage" };
    private static final String[] LOW_KEYWORDS = { "pothole", "lights", "noise", "stray" };

    /**
     * Determines the severity of an alert based on its description.
     * Checks for keywords in descending order of severity.
     *
     * @param description The alert description
     * @return The detected Severity (defaults to LOW if no keywords match)
     */
    public static Severity determineSeverity(String description) {
        if (description == null || description.trim().isEmpty()) {
            return Severity.LOW;
        }

        String lowerCaseDesc = description.toLowerCase();

        // Check Critical
        for (String keyword : CRITICAL_KEYWORDS) {
            if (lowerCaseDesc.contains(keyword)) {
                return Severity.CRITICAL;
            }
        }

        // Check High
        for (String keyword : HIGH_KEYWORDS) {
            if (lowerCaseDesc.contains(keyword)) {
                return Severity.HIGH;
            }
        }

        // Check Medium
        for (String keyword : MEDIUM_KEYWORDS) {
            if (lowerCaseDesc.contains(keyword)) {
                return Severity.MEDIUM;
            }
        }

        // Check Low (Explicit checks, though default is also LOW)
        for (String keyword : LOW_KEYWORDS) {
            if (lowerCaseDesc.contains(keyword)) {
                return Severity.LOW;
            }
        }

        // Default
        return Severity.LOW;
    }
}
