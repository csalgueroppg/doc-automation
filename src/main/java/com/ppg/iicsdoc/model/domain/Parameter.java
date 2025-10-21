package com.ppg.iicsdoc.model.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Represents an input parameter for an API endpoint.
 * 
 * <p>
 * This model captures metadata about a parameter's nam, location, type,
 * and constraints, and is typically used in OpenAPI specifications or 
 * dynamic request builder.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class Parameter {

    /** The name of the parameter (e.g., {@code "userId"}). */
    private String name;

    /** The location of the parameter (e.g., query, path, header). */
    private ParameterLocation in;

    /** The data type of the parameter (e.g., {@code "string"}). */
    private String type;

    /** Indicates whether the parameter is required for the request. */
    private boolean required;

    /** A human-readable description of the parameter's scope. */
    private String description;

    /** The default value to use if the parameter is not provided. */
    private String defaultValue;
}
