package com.ppg.iicsdoc.model.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

import lombok.Builder;

/**
 * Complete parsed metadata from IICS XML file
 * This is the internal domain model (not tied to XML structure)
 */
@Data
@Builder
public class ParsedMetadata {
    
    // Process information
    private String processName;
    private ProcessType processType;
    private String version;

    // Metadata
    private String description;
    private String author;
    private LocalDate created;
    private LocalDate modified;

    // Components
    private List<Connection> connections;
    private List<Transformation> transformations;
    private List<OpenAPIEndpoint> openApiEndpoints;
    private DataFlow dataFlow;

    // Additional metadata
    private Map<String, Object> additionalProperties;
}
