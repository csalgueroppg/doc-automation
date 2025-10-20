package com.ppg.iicsdoc.exception;

import java.util.List;
import lombok.Getter;

/**
 * Exception thrown then XML parsing fails.
 */
@Getter
public class ParsingException extends RuntimeException {

    private final String xmlFile;
    private final List<String> errors;

    public ParsingException(String message) {
        super(message);

        this.xmlFile = null;
        this.errors = List.of();
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);

        this.xmlFile = null;
        this.errors = List.of();
    }

    public ParsingException(
            String message,
            String xmlFile,
            List<String> errors
    ) {
        super(message);

        this.xmlFile = xmlFile;
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (xmlFile != null) {
            sb.append(" [File: ").append(xmlFile).append("]");
        }

        if (errors != null && !errors.isEmpty()) {
            sb.append("\nErrors:\n");
            errors.forEach(error -> sb.append(" - ").append(error).append("\n"));
        }

        return sb.toString();
    }
}
