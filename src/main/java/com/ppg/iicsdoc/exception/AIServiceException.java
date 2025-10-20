package com.ppg.iicsdoc.exception;

import lombok.Getter;

/** 
 * Exception thrown when AI service calls fail
 */
@Getter
public class AIServiceException extends RuntimeException {
    
    private final String apiEndpoint;
    private final int statusCode;

    public AIServiceException(String message) {
        super(message);

        this.apiEndpoint = null;
        this.statusCode = -1;
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);

        this.apiEndpoint = null;
        this.statusCode = -1;
    }

    public AIServiceException(String message, String apiEndpoint, int statusCode) {
        super(message);

        this.apiEndpoint = apiEndpoint;
        this.statusCode = statusCode;
    }

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

