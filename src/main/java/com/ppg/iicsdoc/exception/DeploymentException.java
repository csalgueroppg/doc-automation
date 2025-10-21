package com.ppg.iicsdoc.exception;

import lombok.Getter;

/**
 * Exception thrown when a deployment operation fails.
 * 
 * <p>
 * This exception captures contextual information about the failure,
 * including the target path involved in the deployment. It is
 * intended for use in scenarios such as publishing documentation,
 * pushing to a Git repository, or writing deployment directory.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Getter
public class DeploymentException extends RuntimeException {

    /**
     * The target path involved in the failed deployment.
     * May be {@code null}.
     */
    private final String targetPath;

    /**
     * Constructs a {@code DeploymentException} with a message only.
     *
     * @param message the error message describing the failure
     */
    public DeploymentException(String message) {
        super(message);
        this.targetPath = null;
    }

    /**
     * Constructs a {@code DeploymentException} with a message and underlying cause.
     *
     * @param message the error message describing the failure
     * @param cause   the root cause of the exception
     */
    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
        this.targetPath = null;
    }

    /**
     * Constructs a {@code DeploymentException} with a message, target path, and
     * underlying cause.
     *
     * @param message    the error message describing the failure
     * @param targetPath the path involved in the deployment operation
     * @param cause      the root cause of the exception
     */
    public DeploymentException(String message, String targetPath, Throwable cause) {
        super(message, cause);
        this.targetPath = targetPath;
    }
}
