package com.ppg.iicsdoc.exception;

import java.util.List;

import lombok.Getter;

/**
 * Exception thrown when validation fails.
 * 
 * <p>
 * This exception captures a list of validation errors and optional
 * section identifier to help localize the failure with a larger
 * context (e.g., a form, configuration block, or data segment).
 * </p>
 * 
 * <p>
 * The {@link #getMessage()} method is overriden to include the section
 * name and a formatted list of validation errors.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Getter
public class ValidationException extends RuntimeException {

    /**
     * A list of validation error messages.
     * May be empty but never {@code null}.
     */
    private final List<String> validationErrors;

    /**
     * An optional section identifier where the validation failed.
     * May be {@code null}.
     */
    private final String section;

    /**
     * Constructs a {@code ValidationException} with a message and list of
     * validation errors.
     *
     * @param message          the error message describing the failure
     * @param validationErrors the list of validation error messages
     */
    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.section = null;
        this.validationErrors = validationErrors;
    }

    /**
     * Constructs a {@code ValidationException} with a message, section name, and
     * list of validation errors.
     *
     * @param message          the error message describing the failure
     * @param section          the section or context where validation failed
     * @param validationErrors the list of validation error messages
     */
    public ValidationException(String message, String section, List<String> validationErrors) {
        super(message);
        this.section = section;
        this.validationErrors = validationErrors;
    }

    /**
     * Returns the formatted error message including section and validation error
     * details.
     *
     * @return the detailed error message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (section != null) {
            sb.append("[Section:").append(section).append("]");
        }

        if (validationErrors != null && !validationErrors.isEmpty()) {
            sb.append("\nValidation Errors:\n");
            validationErrors.forEach(error -> sb.append(" - ").append(error).append("\n"));
        }

        return sb.toString();
    }
}
