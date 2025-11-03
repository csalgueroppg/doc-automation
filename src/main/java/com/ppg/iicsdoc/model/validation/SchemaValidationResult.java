package com.ppg.iicsdoc.model.validation;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Schema validation result object containing schema metadata and 
 * either {@code errors} or {@code warnings} lists with any problem or issues
 * identifier during validation.
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaValidationResult {
    
    /** Indicates whether the result is valid after validation */
    private boolean valid;

    /** Schema version information */
    private String schemaVersion;

    /** List of errors identified during validation */
    private List<ValidationError> errors;

    /** List of warnings identified during validation */
    private List<ValidationWarning> warnings;

    /** Metrics object from a validation */
    private ValidationMetrics metrics;

    /**
     * Creates a valid schema validation result with the given schema version.
     * 
     * @param schemaVersion schema version information associated with the result
     * @return a {@code SchemaValidationResult} instance representing a valid validation
     */
    public static SchemaValidationResult valid(String schemaVersion) {
        return SchemaValidationResult.builder()
            .valid(true)
            .schemaVersion(schemaVersion)
            .errors(new ArrayList<>())
            .warnings(new ArrayList<>())
            .build();
    }

    /**
     * Creates an invalid schema validation result with a given error list.
     * 
     * @param errors a list containing validation errors
     * @return a {@code SchemaValidationResult} instance representing an invalid result
     */
    public static SchemaValidationResult invalid(List<ValidationError> errors) {
        return SchemaValidationResult.builder()
            .valid(false)
            .errors(errors)
            .warnings(new ArrayList<>())
            .build();
    }

    /**
     * Returns {@code true} if the validation result contains any warnings
     * 
     * @return {@code true} if {@code warnings} is not {@code null}, 
     *         {@code false} otherwise
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Returns {@code true} if the validation result contains any errors.
     * 
     * @return {@code true} if {@code errors} is not {@code null},
     *         {@code false} otherwise
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Returns the size of the {@code errors} list if {@code errors} is not 
     * {@code null}.
     * 
     * @return the size of the {@code errors} list or 0 if empty.
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Returns the size of the {@code warnings} list if {@code warnings} is not
     * {@code null}.
     * 
     * @return the size of the {@code warnings} list or 0 if empty.
     */
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }

}
