package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a single field within a data schema, entity, or structure.
 * 
 * <p>
 * This model is used to describe the name, type, and constraints of a field,
 * and is commonly applied in scenarios such as schema validation, form
 * generation, or metadata-driven processing.
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class Field {

    /** The name of the field (e.g., "email", "createdAt") */
    private String name;

    /** The data type of the field (e.g., "String", "Integer", "Date") */
    private String type;

    /** A human-readable description of the field's purpose or usage. */
    private String description;

    /** Indicates whether the field is required for validation or processing. */
    private boolean required;
}
