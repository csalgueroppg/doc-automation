package com.ppg.iicsdoc.model.domain;

/**
 * Defines supported authentication mechanisms for external services
 * integration.
 * 
 * <p>
 * This enum is typically used to configure how requests are authenticated
 * when interacting with APIs, data sources, or third-party services.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public enum AuthenticationType {
    /** No authentication required */
    NONE,

    /** Basic authentication using username and password */
    BASIC,

    /** OAuth 2.0 authentication using access tokens and auth flows */
    OAUTH2,

    /** API key-based authentication, typically passed in headers or query params */
    API_KEY,

    /** JWT authentication using signed tokens for stateless auth */
    JWT
}
