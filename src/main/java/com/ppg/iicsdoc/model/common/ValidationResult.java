package com.ppg.iicsdoc.model.common;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the outcome of a validation process.
 * 
 * <p>
 * This class encapsulates whether the input was valid, along
 * with any associated error or warning messages. It supports
 * both static factory methods and dynamic accumulation of
 * validation feedback.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /** Indicates whether the validation passed. */
    private boolean valid;

    /**
     * A list of validation error messages.
     * May be empty but never {@code null}.
     */
    private List<String> errors;

    /**
     * A list of validation warning messages.
     * May be empty but never {@code null}.
     */
    private List<String> warnings;

    /**
     * Creates a valid result with no errors or warnings.
     *
     * @return a {@code ValidationResult} marked as valid
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates an invalid result with the specified errors.
     *
     * @param errors the list of validation errors
     * @return a {@code ValidationResult} marked as invalid
     */
    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult(false, errors, new ArrayList<>());
    }

    /**
     * Creates an invalid result with both errors and warnings.
     *
     * @param errors   the list of validation errors
     * @param warnings the list of validation warnings
     * @return a {@code ValidationResult} marked as invalid
     */
    public static ValidationResult invalidWithWarnings(
        List<String> errors, List<String> warnings) {
        return new ValidationResult(false, errors, warnings);
    }

    /**
     * Adds an error message to the result and marks it as invalid.
     *
     * @param error the error message to add
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }

        this.errors.add(error);
        this.valid = false;
    }

    /**
     * Adds a warning message to the result.
     *
     * @param warning the warning message to add
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }

        this.warnings.add(warning);
    }

    /**
     * Returns {@code true} if the result contains any warnings.
     *
     * @return {@code true} if warnings are present
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Returns {@code true} if the result contains any errors.
     *
     * @return {@code true} if errors are present
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
