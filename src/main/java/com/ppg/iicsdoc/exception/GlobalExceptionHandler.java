package com.ppg.iicsdoc.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;


/**
 * Global exception handler for all controllers
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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
                "errors", ex.getErrors()
            ))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

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
                "validationErrors", ex.getValidationErrors()
            ))
            .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

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
                "statusCode": ex.getStatusCode()
            ))
            .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(DeploymentException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(DeploymentException ex) {
        log.error("AI Service error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("AI Service Error")
            .message(ex.getMessage())
            .details(Map.of(
                "targetPath", ex.getTargetPath() != null ? ex.getTargetPath() : "unknown"
            ))
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

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

    @lombok.Data
    @lombok.Builder 
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private HttpStatus status;
        private String error;
        private String message;
        private Map<String, Object> details;
    }
}
