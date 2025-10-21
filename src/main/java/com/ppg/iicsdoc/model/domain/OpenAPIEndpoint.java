package com.ppg.iicsdoc.model.domain;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a single endpoint definition in an OpenAPI specification.
 * 
 * <p>
 * This model captures metadata about a RESTful operation, including its path,
 * HTTP method, parameters, responses, and descriptive tags. It is typically
 * for documentation generation, API introspection, or dynamic client 
 * scaffolding.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@Builder
public class OpenAPIEndpoint {

    /** The relative path of the endpoint (e.g., {@code /users/{id}}) */
    private String path;

    /** The HTTP method used to invoke the endpint (e.g., GET, POST) */
    private HttpMethod method;

    /** A unique identifier for the operation, used for code generation or ref */
    private String operationId;

    /** A short summary of what the endpoint does. */
    private String summary;

    /** A detailed description of the endpoint's behavior and purpose. */
    private String description;

    /** A list of input parameters accepted by the endpoint. */
    private List<Parameter> parameters;

    /** 
     * A map of possible responses keyed by HTTP status
     * (e.g., {@code "200"}, {@code "400"}) 
     */
    private Map<String, Response> responses;

    /** A list of tags used to group or categorize endpoint. */
    private List<String> tags;
}