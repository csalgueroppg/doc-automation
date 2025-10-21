package com.ppg.iicsdoc.exception;

import lombok.Getter;

/**
 * Exception thrown when an AI service call fails.
 * 
 * <p>
 * This exception captures additional context about the failure, including the
 * target API endpoint and HTTP status code, if available. It is intended to
 * support diagnostics and error reporting for external AI integrations.
 * </p>
 * 
 * <p>
 * The {@link #getMessage()} method is overridden to include endpoint and status
 * code details in the formatted output.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Getter
public class AIServiceException extends RuntimeException {

    /** 
     * The API endpoint that was called when the failure occured.
     * May be {@code null}.
     */
    private final String apiEndpoint;

    /** 
     * The HTTP status code returned by the AI service.
     * Defaults to {@code -1} if unavailable.
     */
    private final int statusCode;

    /**
     * Constructs an exception with a message only.
     *
     * @param message the error message
     */
    public AIServiceException(String message) {
        super(message);
        this.apiEndpoint = null;
        this.statusCode = -1;
    }

    /**
     * Constructs an exception with a message and underlying cause.
     *
     * @param message the error message
     * @param cause   the root cause of the failure
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.apiEndpoint = null;
        this.statusCode = -1;
    }

    /**
     * Constructs an exception with a message, API endpoint, and status code.
     *
     * @param message     the error message
     * @param apiEndpoint the endpoint that was called
     * @param statusCode  the HTTP status code returned
     */
    public AIServiceException(String message, String apiEndpoint, int statusCode) {
        super(message);
        this.apiEndpoint = apiEndpoint;
        this.statusCode = statusCode;
    }

    /**
     * Returns the formatted error message including endpoint and status code
     * details.
     *
     * @return the detailed error message
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (apiEndpoint != null) {
            sb.append(" [Endpoint: ").append(apiEndpoint).append("]");
        }

        if (statusCode > 0) {
            sb.append(" [Status Code: ").append(statusCode).append("]");
        }

        return sb.toString();
    }
}
