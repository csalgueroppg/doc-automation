/**
 * Contains custom exception classes and global error handling logic for the application.
 * 
 * <p>
 * This package defines:
 * </p>
 * <ul>
 *   <li><b>Domain-specific exceptions</b> — such as {@code ParsingException}, {@code ValidationException}, {@code AIServiceException}, and {@code DeploymentException}</li>
 *   <li><b>Global exception handling</b> — via {@link com.ppg.iicsdoc.exception.GlobalExceptionHandler} to standardize error responses across all controllers</li>
 *   <li><b>Structured error reporting</b> — using {@code ErrorResponse} to encapsulate timestamp, status, message, and contextual details</li>
 * </ul>
 * 
 * <p>
 * These components ensure consistent, informative, and debuggable error handling throughout the application.
 * </p>
 */
package com.ppg.iicsdoc.exception;