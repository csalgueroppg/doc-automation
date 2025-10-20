package com.ppg.iicsdoc.exception;

import lombok.Getter;

@Getter
public class DeploymentException extends RuntimeException {
    
    private final String targetPath;

    public DeploymentException(String message) {
        super(message);

        this.targetPath = null;
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);

        this.targetPath = null;
    }

    public DeploymentException(String message, String targetPath, Throwable cause) {
        super(message, cause);

        this.targetPath = targetPath;
    }
}
