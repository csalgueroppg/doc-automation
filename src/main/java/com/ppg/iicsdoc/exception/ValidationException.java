package com.ppg.iicsdoc.exception;

import java.util.List;

import lombok.Getter;

/**
 * Exception thrown when validation fails
 */
@Getter
public class ValidationException extends RuntimeException {
   
    private final List<String> validationErrors;
    private final String section;

    public ValidationException(String message, List<String> validationErrors) {
        super(message);

        this.section = null;
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, String section, List<String> validationErrors) {
        super(message);

        this.section = section;
        this.validationErrors = validationErrors;
    }

    @Override 
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (section != null) {
            sb.append( "[Section:").append(section).append("]");
        }

        if (validationErrors != null && !validationErrors.isEmpty()) {
            sb.append("\nValidation Errors:\n");
            validationErrors.forEach(error -> sb.append(" - ").append(error).append("\n"));
        }

        return sb.toString();
    }
}
