package com.ppg.iicsdoc.model.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a possible error from the schema validation.
 * 
 * <p>
 * Contains different error metadata such as error classification, code,
 * human-friendly message, xml associated data such as xpath, line number, and
 * column number, and finally a possible suggestion for the error.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /** Error severity classification. */
    private ErrorSeverity severity;

    /** Associated error code */
    private String code;

    /** Human-friendly error message */
    private String message;

    /** XPath where the error occurred */
    private String xpath;

    /** XML line number where the error occurred */
    private int lineNumber;

    /** XML column number where the error occurred */
    private int columnNumber;

    /** Error suggestion fix */
    private String suggestion;

    /**
     * Enumeration representing different error categories.
     * 
     * <p>
     * Available categories:
     * </p>
     * 
     * <ul>
     * <li>Fatal: cannot proceed with parsing</li>
     * <li>Error: validation failed but parsing might succeed</li>
     * <li>Warning: Potential issues but valid</li>
     * <li>Info: Informational message</li>
     * </ul>
     * 
     * @version 1.0.0
     * @since 2025-10-28
     */
    public enum ErrorSeverity {
        FATAL,
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Creates a fatal {@code ValidationError} instance.
     * 
     * @param code    error code associated with the fatal error
     * @param message error message associated with the fatal error
     * @return a {@code ValidationError} representing a fatal
     */
    public static ValidationError fatal(String code, String message) {
        return ValidationError.builder()
                .severity(ErrorSeverity.FATAL)
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Creates an error {@code ValidationError} instance.
     * 
     * <p>
     * Associates stack information such as line number and column information
     * </p>
     * 
     * @param code    error code associated with the error
     * @param message error message associated with the error
     * @return a {@code ValidationError} representing an error
     */
    public static ValidationError error(
            String code,
            String message,
            int line,
            int column) {
        return ValidationError.builder()
                .severity(ErrorSeverity.ERROR)
                .code(code)
                .message(message)
                .lineNumber(line)
                .columnNumber(column)
                .build();
    }

    /**
     * Creates a warning {@code ValidationError} instance.
     * 
     * @param code    error code associated with the warning
     * @param message error message associated with the warning
     * @return a {@code ValidationError} representing a warning
     */
    public static ValidationError warning(String code, String message) {
        return ValidationError.builder()
                .severity(ErrorSeverity.WARNING)
                .code(code)
                .message(message)
                .build();
    }
}
