package com.ppg.iicsdoc.model.deployment;

/**
 * Defines the available strategies for deploying documentation or files.
 * 
 * <p>
 * Each strategy represents a distinct method of deployment, such as using
 * local file system, a static site generator, or remote storage solutions.
 * </p>
 * 
 * @author Carlos Salguero 
 * @version 1.0.0
 * @since 2025-10-24
 */
public enum DeploymentStrategy {
    /** Deployment to a local directory. */
    LOCAL_FILESYSTEM("Local File System"),

    /** Deployment with compatible files to a docusaurus. */
    DOCUSAURUS("Docusaurus Documentation File"),

    /** Deployment strategy with commit information */
    GIT_REPOSITORY("Git Repository"),

    /** Deployment to a remote SFTP server */
    REMOTE_SFTP("Remote SFTP Server"),

    /** Deployment to cloud storage services. */
    CLOUD_STORAGE("Cloud Storage (S3, Azure Blob)");

    /** Human-friendly display name. */
    private final String displayName;

    /**
     * Constructs a deployment strategy with a human-readable display
     * name.
     * 
     * @param displayName the display name of the deployment strategy
     */
    DeploymentStrategy(String displayName) {
        this.displayName = displayName;
    }

    /** 
     * Returns the human-readable name of the deployment.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return this.displayName;
    }
}