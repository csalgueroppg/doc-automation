package com.ppg.iicsdoc.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for all REST controllers.
 * 
 * <p>
 * This class intercepts exceptions thrown during request processing and
 * converted into structured {@link ErrorResponse} objects with appropriate
 * HTTP status codes.
 * </p>
 * 
 * <p>
 * Each handler method logs the error and returns a consistent response format
 * improving debuggability and user experience.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link ParsingException} thrown during XML parsing.
     * 
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 400 and detailed error
     *         information.
     */
    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<ErrorResponse> handleParsingException(ParsingException ex) {
        log.error("Parsing error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parsing error")
                .message(ex.getMessage())
                .details(Map.of(
                        "xmlFile", ex.getXmlFile() != null ? ex.getXmlFile() : "unknown",
                        "errors", ex.getErrors()))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles {@link ValidationException} thrown during input validation.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 422 and validation error details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Validation Error")
                .message(ex.getMessage())
                .details(Map.of(
                        "section", ex.getSection() != null ? ex.getSection() : "unknown",
                        "validationErrors", ex.getValidationErrors()))
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handles {@link AIServiceException} thrown during AI service interactions.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 503 and service error details
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(AIServiceException ex) {
        log.error("AI Service error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("AI Service Error")
                .message(ex.getMessage())
                .details(Map.of(
                        "apiEndpoint", ex.getApiEndpoint() != null ? ex.getApiEndpoint() : "unknown",
                        "statusCode", ex.getStatusCode()))
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * Handles {@link DeploymentException} thrown during deployment operations.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 500 and deployment error details
     */
    @ExceptionHandler(DeploymentException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(DeploymentException ex) {
        log.error("AI Service error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("AI Service Error")
                .message(ex.getMessage())
                .details(Map.of(
                        "targetPath", ex.getTargetPath() != null ? ex.getTargetPath() : "unknown"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handles all uncaught exceptions.
     *
     * @param ex the exception instance
     * @return a {@link ResponseEntity} with HTTP 500 and generic error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .details(Map.of("error", ex.getMessage()))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Represents a standardized error response returned by the application's global
     * exception handler.
     * 
     * <p>
     * This structure is used to convey meaningful error information to clients in a
     * consistent format, including timestamp, HTTP status code, error type,
     * message, and optional contextual details.
     * </p>
     * 
     * <p>
     * It is typically returned from {@code @ExceptionHandler} methods in
     * {@link GlobalExceptionHandler}.
     * </p>
     */
    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {

        /**
         * The timestamp when the error occurred.
         */
        private LocalDateTime timestamp;

        /**
         * The HTTP status code associated with the error (e.g., 400, 500).
         */
        private int status;

        /**
         * A short description of the error type (e.g., "Validation Error", "Internal
         * Server Error").
         */
        private String error;

        /**
         * A detailed error message describing the cause or nature of the failure.
         */
        private String message;

        /**
         * A map of additional error details, such as field names, validation messages,
         * or contextual metadata relevant to the failure.
         */
        private Map<String, Object> details;
    }
}
