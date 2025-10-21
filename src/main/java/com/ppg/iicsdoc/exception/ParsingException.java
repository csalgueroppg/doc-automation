package com.ppg.iicsdoc.exception;

import java.util.List;

import lombok.Getter;

/**
 * Exception thrown then XML parsing fails.
 * 
 * <p>
 * This exception captures the name of the XML file being parsed and a list
 * of validation or structural errors encountered during parsing. It is
 * intended for use in scenarios where detailed feedback is needed for
 * malformed or invalid XML input.
 * </p>
 * 
 * <p>
 * The {@link #getMessage()} method is overriden to include the file name
 * and a formatted list of errors in the output message.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Getter
public class ParsingException extends RuntimeException {

    /**
     * The name or path of the XML file that failed to parse.
     * May be {@code null}.
     */
    private final String xmlFile;

    /**
     * A list of parsing or validation errors encountered.
     * May be empty but not never {@code null}.
     */
    private final List<String> errors;

    /**
     * Constructs a {@code ParsingException} with a message only.
     *
     * @param message the error message describing the failure
     */
    public ParsingException(String message) {
        super(message);
        this.xmlFile = null;
        this.errors = List.of();
    }

    /**
     * Constructs a {@code ParsingException} with a message and underlying cause.
     *
     * @param message the error message describing the failure
     * @param cause   the root cause of the exception
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
        this.xmlFile = null;
        this.errors = List.of();
    }

    /**
     * Constructs a {@code ParsingException} with a message, XML file name, and list
     * of errors.
     *
     * @param message the error message describing the failure
     * @param xmlFile the name or path of the XML file being parsed
     * @param errors  the list of parsing or validation errors
     */
    public ParsingException(String message, String xmlFile, List<String> errors) {
        super(message);
        this.xmlFile = xmlFile;
        this.errors = errors;
    }

    /**
     * Returns the formatted error message including file name and error details.
     *
     * @return the detailed error message
     */

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
