package com.ppg.iicsdoc.model.domain;

/**
 * Defines standard HTTP methods used in RESTful communication.
 * 
 * <p>
 * This enum is typically used to specify the type of operation being
 * performed in an HTTP request, such as retrieving, updating, or deleting
 * resources.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum HttpMethod {

    /** Retrieves data from the server without modifying it. */
    GET,

    /** Submits new data to the server, often resulting in resource creation. */
    POST,

    /** Updates an existing resource with new data. */
    PUT,

    /** Deletes a resource from the server. */
    DELETE,

    /** Applies partial updates to an existing resource. */
    PATCH,

    /** Describes the communication options available for a resource. */
    OPTIONS,

    /** Retrieves metadata about a resource without the response body. */
    HEAD
}
