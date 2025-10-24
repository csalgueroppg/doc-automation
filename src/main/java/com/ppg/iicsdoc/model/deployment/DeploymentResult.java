package com.ppg.iicsdoc.model.deployment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the outcome of a deployment operation.
 * 
 * <p>
 * This class contains metadata about the deployment process, including
 * whether it succeeded, where the files where deployed, which files were
 * involved, when the deployment occurred, and the strategy used. It also
 * includes a human-readable message describing the result.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentResult {

    /**
     * {@code true} if the deployment was successful;
     * {@code false} otherwise.
     */
    private boolean success;

    /** The destination path where the deployed files were placed. */
    private String targetPath;

    /**
     * A list of file paths that were deployed.
     * 
     * <p>
     * May be empty or {@code null} if no files were deployed.
     * </p>
     */
    private List<String> deployedFiles;

    /** The timestamp indicating when the deployment was performed. */
    private LocalDateTime deployedAt;

    /** A human-readable message describing the result of the deployment. */
    private String message;

    /** The strategy used to perform the deployment. */
    private DeploymentStrategy strategy;

    /**
     * Creates a {@code DeploymentResult} representing a successful
     * deployment.
     * 
     * @param targetPath the path where the files were deployed
     * @param files      the list of deployed files paths
     * @param strategy   the deployment strategy used
     * @return a {@code DeploymentResult} indicating success
     */
    public static DeploymentResult success(
            String targetPath,
            List<String> files,
            DeploymentStrategy strategy) {
        return DeploymentResult.builder()
                .success(true)
                .targetPath(targetPath)
                .deployedFiles(files)
                .deployedAt(LocalDateTime.now())
                .message("Deployment successful")
                .strategy(strategy)
                .build();
    }

    /**
     * Creates a {@code DeploymentResult} representing a failed
     * deployment.
     * 
     * @param message  a message describing the reason for failure
     * @param strategy the deployment strategy that was attempted
     * @return a {@code DeploymentResult} indicating failure
     */
    public static DeploymentResult failure(
            String message,
            DeploymentStrategy strategy) {
        return DeploymentResult.builder()
                .success(false)
                .message(message)
                .deployedAt(LocalDateTime.now())
                .strategy(strategy)
                .deployedFiles(new ArrayList<>())
                .build();
    }

    /**
     * Returns the number of files that were deployed.
     * 
     * @return number of deployed files, or {@code 0} if none were deployed.
     */
    public int getDeployedFileCount() {
        return deployedFiles != null ? deployedFiles.size() : 0;
    }
}
