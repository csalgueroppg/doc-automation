package com.ppg.iicsdoc.model.deployment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration object that defines how a deployment should be executed.
 * 
 * <p>
 * This class encapsulates both general deployment settings and
 * strategy-specific options. It supports multiple deployment strategies such
 * as local file system, Git repository, and Docusaurus documentation site.
 * Each strategy may require different configuration files.
 * </p>
 * 
 * <p>
 * Use the static factory methods to create pre-configured instance for
 * common strategies.
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
public class DeploymentConfig {

    /**
     * The strategy to be used for deployment.
     * 
     * <p>
     * Determines how and where the files will be deployed. Supported
     * strategies include:
     * </p>
     * <ul>
     * <li>{@link DeploymentStrategy#LOCAL_FILESYSTEM}</li>
     * <li>{@link DeploymentStrategy#DOCUSAURUS}</li>
     * <li>{@link DeploymentStrategy#GIT_REPOSITORY}</li>
     * <li>{@link DeploymentStrategy#REMOTE_SFTP}</li>
     * <li>{@link DeploymentStrategy#CLOUD_STORAGE}</li>
     * </ul>
     */
    private DeploymentStrategy strategy;

    /**
     * The target path where files should be deployed.
     * 
     * <p>
     * This path is used by most strategies to determine the destination
     * for generated or copied files. For {@code Git} deployments, this may
     * refer to the local working directory before pushing.
     * </p>
     */
    private String targetPath;

    /**
     * Indicates whether a backup should be created before deployment.
     * 
     * <p>
     * If {@code true}, the system will attempt to preserve existing files
     * before overwriting them.
     * </p>
     */
    private boolean createBackup;

    /**
     * Indicates whether existing files at the target location should
     * be overwritten.
     * 
     * <p>
     * If {@code false}, deployment may fail or skip files that already exists.
     * </p>
     */
    private boolean overwriteExisting;

    /**
     * Indicates whether the deployment should be validated after completion.
     * 
     * <p>
     * Validation may include checking file integrity, configuring remote
     * uploads, or verifying Git commits.
     * </p>
     */
    private boolean validateAfterDeployment;

    // Git-specific configuration options
    /**
     * The URL of the Git repository to deploy to.
     * 
     * <p>
     * Required when using {@link DeploymentStrategy#GIT_REPOSITORY}.
     * </p>
     */
    private String gitRepoUrl;

    /**
     * The branch of the Git repository to deploy to.
     * 
     * <p>
     * Defaults to {@code "main"} or {@code "master"} if not specified.
     * </p>
     */
    private String gitBranch;

    /**
     * Indicates whether changes should be automatically commited after
     * deployment.
     * 
     * <p>
     * If {@code true}, the system will stage and commit changes using
     * the provided commit message.
     * </p>
     */
    private boolean autoCommit;

    /**
     * The commit message to use when auto-committing changes.
     * 
     * <p>
     * Only used if {@code autoCommit} is {@code true}.
     * </p>
     */
    private String commitMessage;

    // Docusaurus-specific config
    /**
     * The path to the Docusaurus documentation folder.
     * 
     * <p>
     * Used when deploying documentation files to a Docusaurus files.
     * </p>
     */
    private String docusaurusDocsPath;

    /**
     * The category under which the documentation should be grouped
     * in Docusaurus.
     * 
     * <p>
     * Helps organize content within the sidebar or navigation structure.
     * </p>
     */
    private String docusaurusCategory;

    /**
     * Creates a default configuration for deploying to the local file system.
     * 
     * <p>
     * This configuration enables backup, overwriting, and post-deployment
     * validation.
     * </p>
     * 
     * @param targetPath the path where the files should be deployed
     * @return a {@code DeploymentConfig} configured for local file system
     *         deployment.
     */
    public static DeploymentConfig localFilesystem(String targetPath) {
        return DeploymentConfig.builder()
                .strategy(DeploymentStrategy.LOCAL_FILESYSTEM)
                .targetPath(targetPath)
                .createBackup(true)
                .overwriteExisting(true)
                .validateAfterDeployment(true)
                .build();
    }

    /**
     * Creates a default configuration for deploying to a Docusaurus
     * documentation folder.
     * 
     * <p>
     * This configuration sets the documentation path and enables backup,
     * overwriting, and validation. The category can be set separately if
     * needed.
     * </p>
     * 
     * @param targetPath the path to the Docusaurus docs directory
     * @return a {@code DeploymentConfig} configured for Docusaurus deployment
     */
    public static DeploymentConfig docusaurus(String targetPath) {
        return DeploymentConfig.builder()
                .strategy(DeploymentStrategy.DOCUSAURUS)
                .docusaurusDocsPath(targetPath)
                .createBackup(true)
                .overwriteExisting(true)
                .validateAfterDeployment(true)
                .build();
    }

    /**
     * Creates a default configuration for deploying to a Git repository.
     * 
     * <p>
     * This configuration sets the repository URL, branch, and local target
     * path. It also enables auto-commit with a default commit message,
     * backup, overwriting, and validation.
     * 
     * @param repoUrl    the URL of the Git repository
     * @param branch     the branch to deploy to
     * @param targetPath the local path where files are prepared before pushing
     * @return a {@code DeploymentConfig} configured for Git deployment
     */
    public static DeploymentConfig git(
            String repoUrl,
            String branch,
            String targetPath) {
        return DeploymentConfig.builder()
                .strategy(DeploymentStrategy.GIT_REPOSITORY)
                .gitRepoUrl(repoUrl)
                .gitBranch(branch)
                .targetPath(targetPath)
                .autoCommit(true)
                .commitMessage("docs: Auto-generated documentation")
                .createBackup(true)
                .overwriteExisting(true)
                .validateAfterDeployment(true)
                .build();
    }
}
