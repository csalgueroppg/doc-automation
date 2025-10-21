package com.ppg.iicsdoc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.ppg.iicsdoc.exception.ValidationException;

/**
 * Provides utility methods for validating input fields in domain models,
 * DTOs, and user input. This class supports common validation patterns such
 * as required checks, length constraints, pattern matching, and custom
 * predicates.
 * 
 * <p>
 * All methods are static and designed to accumulate validation errors into
 * a {@code List<String>} for deferred exception handling.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public class ValidationUtil {
    /**
     * Validates that a string field is not null, empty, or blank.
     * 
     * @param value     the string value to validate
     * @param fieldName the name of the field being validated
     * @param errors    the list to which validation error messages are appended
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
     * Validates that an object field is not null.
     *
     * @param value     the object to validate
     * @param fieldName the name of the field being validated
     * @param errors    the list to which validation error messages are appended
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
     * Validates that a collection is not null or empty.
     *
     * @param collection the collection to validate
     * @param fieldName  the name of the field being validated
     * @param errors     the list to which validation error messages are appended
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
     * Validates that a string matches the specified regular expression pattern.
     * 
     * <p>
     * Null values are ignored.
     * </p>
     *
     * @param value     the string to validate
     * @param pattern   the regex pattern the string must match
     * @param fieldName the name of the field being validated
     * @param errors    the list to which validation error messages are appended
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
     * Validates that a string's length falls within the specified bounds.
     * 
     * <p>
     * Null values are ignored.
     * </p>
     *
     * @param value     the string to validate
     * @param minLength the minimum allowed length (inclusive)
     * @param maxLength the maximum allowed length (inclusive)
     * @param fieldName the name of the field being validated
     * @param errors    the list to which validation error messages are appended
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
     * Validates that a numeric value falls within the specified range.
     * 
     * <p>
     * Null values are ignored.
     * </p>
     * 
     * @param value     the number to validate
     * @param min       the minimum allowed value (inclusive)
     * @param max       the maximum allowed value (inclusive)
     * @param fieldName the name of the field being validated
     * @param errors    the list to which validation error messages are appended
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
     * Validates a value using a custom predicate.
     * 
     * <p>
     * Null values are ignored.
     * </p>
     *
     * @param value        the value to validate
     * @param predicate    the predicate to apply
     * @param fieldName    the name of the field being validated
     * @param errorMessage the error message to append if the predicate fails
     * @param errors       the list to which validation error messages are appended
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
     * Throws a {@link ValidationException} if any validation errors are present.
     *
     * @param errors  the list of accumulated validation errors
     * @param context a descriptive context for the validation (e.g., entity name)
     * @throws ValidationException if the error list is not empty
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
     * Creates a new list for collecting validation errors.
     *
     * @return a new empty {@code List<String>} for validation error messages
     */
    public static List<String> createErrorList() {
        return new ArrayList<>();
    }
}
