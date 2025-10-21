package com.ppg.iicsdoc.model.domain;

/**
 * Defines the possible locations for parameters in an API request.
 * 
 * <p>
 * This enum is typically used to describe where a parameter is expected to 
 * appear in an HTTP request, such as in the query string, path, headers, or
 * body.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum ParameterLocation {

    /** Parameter is passed in the query string of the URL. */
    QUERY,

    /** Parameter is embedded directly in the URL path. */
    PATH,

    /** Parameter is included in the HTTP request headers. */
    HEADER,

    /** Parameter is sent via HTTP cookies. */
    COOKIE,

    /** Parameter is part of the request body payload. */
    BODY    
}
