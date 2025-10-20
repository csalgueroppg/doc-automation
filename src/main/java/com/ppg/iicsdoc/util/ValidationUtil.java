package com.ppg.iicsdoc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import com.ppg.iicsdoc.exception.ValidationException;

/**
 * Utility class for validation operations
 */
public class ValidationUtil {
    /**
     * Validate required string field
     */
    public static void validateRequiredField(
            String value,
            String fieldName,
            List<String> errors) {
        if (StringUtils.isBlank(value)) {
            errors.add(fieldName + " is required but was empty or null");
        }
    }

    /**
     * Validate required object field
     */
    public static void validateRequired(
            Object value,
            String fieldName,
            List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " is required but was null");
        }
    }

    /**
     * Validate collection is not empty
     */
    public static void validateNotEmpty(
            List<?> collection,
            String fieldName,
            List<String> errors) {
        if (collection == null || collection.isEmpty()) {
            errors.add(fieldName + " is required but was empty or null");
        }
    }

    /**
     * Validate string matches pattern
     */
    public static void validatePattern(
            String value,
            String pattern,
            String fieldName,
            List<String> errors) {
        if (value != null && !value.matches(pattern)) {
            errors.add(fieldName + " does not match required pattern: " + pattern);
        }
    }

    /**
     * Validate string length
     */
    public static void validateLength(
            String value,
            int minLength,
            int maxLength,
            String fieldName,
            List<String> errors) {
        if (value != null) {
            int length = value.length();
            if (length < minLength) {
                errors.add(fieldName + " must be at least " + minLength + " characters");
            }

            if (length > maxLength) {
                errors.add(fieldName + " must be at most " + maxLength + " characters");
            }
        }
    }

    /**
     * Validate number range
     */
    public static void validateRange(
            Number value,
            Number min,
            Number max,
            String fieldName,
            List<String> errors) {
        if (value != null) {
            double val = value.doubleValue();
            double minVal = min.doubleValue();
            double maxVal = max.doubleValue();

            if (val < minVal || val > maxVal) {
                errors.add(fieldName + " must be between " + min + " and " +
                        max + " (current: " + value + ")");
            }
        }
    }

    /**
     * Validate with custom predicate
     */
    public static void validateCustom(
            Object value,
            Predicate<Object> predicate,
            String fieldName,
            String errorMessage,
            List<String> errors) {
        if (value != null && !predicate.test(value)) {
            errors.add(fieldName + ": " + errorMessage);
        }
    }

    /**
     * Check if validation errors exist and thro exception
     */
    public static void throwIfErrors(List<String> errors, String context) {
        if (!errors.isEmpty()) {
            throw new ValidationException(
                    "Validation failed for " + context,
                    context,
                    errors);
        }
    }

    /**
     * Create a new error list
     */
    public static List<String> createErrorList() {
        return new ArrayList<>();
    }
}
