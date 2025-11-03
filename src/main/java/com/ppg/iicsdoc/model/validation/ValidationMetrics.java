package com.ppg.iicsdoc.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metric information associated with schema validation.
 * 
 * <p>
 * Records the time it took for the validation in ms, file metadata, and if the
 * schema is valid or not for the XML being validated.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationMetrics {
    
    /** Duration of the validation in ms */
    private long validationDurationMs;

    /** File size in bytes */
    private long fileSizeBytes;

    /** Element count from the XML */
    private int elementCount;

    /** Attribute count from the XML */
    private int attributeCount;

    /** Indicates whether the schema is well-formed or not */
    private boolean wellFormed;

    /** Indicates whether the schema is valid or not */
    private boolean schemaValid;
}