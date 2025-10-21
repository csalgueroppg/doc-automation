package com.ppg.iicsdoc.model.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

import lombok.Builder;

/**
 * Represents the complete parsed metadata extracted from an IICS XML file.
 *
 * <p>
 * This is an internal domain model that abstracts and normalizes the XML 
 * content into a structured format suitable for downstream processing, 
 * validation, or transformation.
 * </p>
 *
 * <p>
 * It is intentionally decoupled from the raw XML schema to support flexible
 * usage.
 * </p>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20 
 */
@Data
@Builder
public class ParsedMetadata {
    
    /** The name of the process defined in the IICS metadata. */
    private String processName;

    /** The type of process (e.g., mapping, taskflow) */
    private ProcessType processType;

    /** The version of the process or metadata definition */
    private String version;

    /** A human-readable description of the process. */
    private String description;

    /** The author or creator of the metadata. */
    private String author;

    /** The date the metadata was originally created. */
    private LocalDate created;

    /** The date the metadata was last modified. */
    private LocalDate modified;

    /** A list of connections referenced in the process. */
    private List<Connection> connections;

    /** A list of transformations applied within the process. */
    private List<Transformation> transformations;

    /** A list of OpenAPI endpoints exposed or consumed by the process. */
    private List<OpenAPIEndpoint> openApiEndpoints;

    /** The data flow configuration from source to target. */
    private DataFlow dataFlow;

    /** 
     * A flexible map for storing additional metadata properties not
     * explicitly modeled. 
     */
    private Map<String, Object> additionalProperties;
}
