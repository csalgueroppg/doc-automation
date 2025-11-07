package com.ppg.iicsdoc.deployer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.exception.DeploymentException;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentStrategy;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * Deployment handler for pushing documentation to a Git repository.
 * 
 * <p>
 * This implementation clones the target repository, stages documentation
 * files, commits changes, and pushes them to the remote branch. It delegates
 * file writing to {@link LocalFileSystemHandler} and supports rollback via
 * Git or file restoration.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * DeploymentConfig config = DeploymentConfig.git(
 *         "https://github.com/example/docs.git",
 *         "main",
 *         "/tmp/docs");
 * 
 * MarkdownDocument doc = new MarkdownDocument("guide.md", "# Integration Guide");
 * DeploymentHandler handler = new GitDeploymentHandler(new LocalFileSystemHandler());
 * 
 * DeploymentResult result = handler.deploy(doc, config);
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
@Slf4j
@Component
public class GitDeploymentHandler implements DeploymentHandler {

    /** LocalFileSystemHandler instance for delegation */
    private final LocalFileSystemHandler fileSystemHandler;

    /**
     * Constructs a new {@code GitDeploymentHandler} with a delegated local
     * file system handler.
     * 
     * @param fileSystemHandler the handler used to write files before
     *                          committing to Git.
     */
    public GitDeploymentHandler(LocalFileSystemHandler fileSystemHandler) {
        this.fileSystemHandler = fileSystemHandler;
    }

    @Override
    public DeploymentResult deploy(
            MarkdownDocument document,
            DeploymentConfig config) {
        log.info("Deploying document to Git repository: {}", document.getFilename());

        Git git = null;
        Path tempRepoPath = null;

        try {
            if (!validateConfig(config)) {
                return DeploymentResult.failure(
                        "Invalid Git configuration",
                        DeploymentStrategy.GIT_REPOSITORY);
            }

            tempRepoPath = createTempDirectory();
            git = cloneRepository(config, tempRepoPath);

            checkoutBranch(git, config.getGitBranch());

            Path targetPath = tempRepoPath.resolve(config.getTargetPath());
            Files.createDirectories(targetPath);

            Path targetFile = targetPath.resolve(document.getFilename());
            Files.writeString(targetFile, document.getContent());

            git.add().addFilepattern(
                    config.getTargetPath() + "/" + document.getFilename())
                    .call();

            if (config.isAutoCommit()) {
                String commitMessage = config.getCommitMessage() != null
                        ? config.getCommitMessage()
                        : "docs: Add documentation for " + document.getTitle();

                git.commit()
                        .setMessage(commitMessage)
                        .call();

                pushChanges(git, config);
                log.info("Successfully deployed and pushed to Git: {}",
                        config.getGitRepoUrl());
            } else {
                log.info("Document deployed to local Git repo (not commited)");
            }

            return DeploymentResult.success(
                    config.getGitRepoUrl() + "/" + config.getTargetPath(),
                    List.of(document.getFilename()),
                    DeploymentStrategy.GIT_REPOSITORY);
        } catch (Exception e) {
            log.error("Failed to deploy to Git repository", e);
            throw new DeploymentException(
                    "Failed to deploy to Git repository",
                    config.getGitRepoUrl(),
                    e);
        } finally {
            if (git != null) {
                git.close();
            }

            if (tempRepoPath != null) {
                try {
                    deleteDirectory(tempRepoPath.toFile());
                } catch (IOException e) {
                    log.warn("Failed to clean up temp directory: {}",
                            tempRepoPath, e);
                }
            }
        }
    }

    @Override
    public DeploymentResult deployBatch(
            List<MarkdownDocument> documents,
            DeploymentConfig config) {
        log.info("Deploying {} documents to Git repository", documents.size());

        Git git = null;
        Path tempRepoPath = null;
        List<String> deployedFiles = new ArrayList<>();

        try {
            tempRepoPath = createTempDirectory();
            git = cloneRepository(config, tempRepoPath);
            checkoutBranch(git, config.getGitBranch());

            Path targetPath = tempRepoPath.resolve(config.getTargetPath());
            Files.createDirectories(targetPath);

            for (MarkdownDocument doc : documents) {
                try {
                    Path targetFile = targetPath.resolve(doc.getFilename());
                    Files.writeString(targetFile, doc.getContent());

                    git.add().addFilepattern(
                            config.getTargetPath() + "/" + doc.getFilename())
                            .call();

                    deployedFiles.add(doc.getFilename());
                } catch (Exception e) {
                    log.warn(
                            "Failed to deploy document: {}",
                            doc.getFilename(),
                            e);
                }
            }

            if (config.isAutoCommit() && !deployedFiles.isEmpty()) {
                String commitMessage = config.getCommitMessage() != null
                        ? config.getCommitMessage()
                        : "docs: Add documentation for %d processes".formatted(
                        deployedFiles.size());

                git.commit()
                        .setMessage(commitMessage)
                        .call();

                pushChanges(git, config);
                log.info("Successfully deployed and pushed to Git: {}",
                        config.getGitRepoUrl());
            }

            return DeploymentResult.success(
                    config.getGitRepoUrl() + "/" + config.getTargetPath(),
                    deployedFiles,
                    DeploymentStrategy.GIT_REPOSITORY);
        } catch (Exception e) {
            log.error("Batch deployment to Git failed", e);
            throw new DeploymentException(
                    "Batch deployment to Git failed",
                    config.getGitRepoUrl(),
                    e);
        } finally {
            if (git != null) {
                git.close();
            }

            if (tempRepoPath != null) {
                try {
                    deleteDirectory(tempRepoPath.toFile());
                } catch (IOException e) {
                    log.warn("Failed to clean up temp directory: {}",
                            tempRepoPath, e);
                }
            }
        }
    }

    @Override
    public boolean validateConfig(DeploymentConfig config) {
        if (config == null) {
            log.error("Deployment config is null");
            return false;
        }

        if (config.getGitRepoUrl() == null || config.getGitRepoUrl().isEmpty()) {
            log.error("Git repository URL is required");
            return false;
        }

        if (config.getGitBranch() == null || config.getGitBranch().isEmpty()) {
            log.error("Git branch is required");
            return false;
        }

        if (config.getTargetPath() == null || config.getTargetPath().isEmpty()) {
            log.error("Target path within repository is required");
            return false;
        }

        return true;
    }

    @Override
    public boolean rollback(DeploymentResult result) {
        fileSystemHandler.rollback(result);
        return false;
    }

    // Helper methods
    /**
     * Clones a Git repository to the specified local directory.
     * 
     * <p>
     * This method uses the configuration provided to locate the remote
     * repository and clone the specified branch into the given target path.
     * </p>
     * 
     * @param config     the deployment configuration containing Git repo details
     * @param targetPath the local directory where the repo should be cloned
     * @return a {@link Git} instance representing the cloned repository
     * @throws GitAPIException if the cloning operation fails.
     */
    private Git cloneRepository(DeploymentConfig config, Path targetPath) throws GitAPIException {
        log.info("Cloning repository: {}", config.getGitRepoUrl());
        return Git.cloneRepository()
                .setURI(config.getGitRepoUrl())
                .setDirectory(targetPath.toFile())
                .setBranch(config.getGitBranch())
                .call();
    }

    /**
     * Checks out the specified branch in the given Git repository.
     * 
     * <p>
     * This method assumes the branch exists in the cloned repository.
     * </p>
     * 
     * @param git    the {@link Git} instance representing the repository
     * @param branch the name of the branch to check out
     * @throws GitAPIException if the checkout operation fails
     */
    private void checkoutBranch(Git git, String branch) throws GitAPIException {
        git.checkout()
                .setName(branch)
                .call();

        log.debug("Checked out branch: {}", branch);
    }

    /**
     * Pushes committed changes to the remote Git repository.
     * 
     * <p>
     * This method currently does not support authentication and assumes that
     * credentials are managed externally (e.g., via Git configuration or
     * SSH keys).
     * </p>
     * 
     * @param git    the {@link Git} instance representing the repository
     * @param config the deployment configuration
     * @throws GitAPIException if the push operation fails
     */
    private void pushChanges(Git git, DeploymentConfig config) throws GitAPIException {
        git.push().call();
        log.info("Pushed changes to remote repository");
    }

    /**
     * Creates a temporary directory from intermediate Git operations.
     * 
     * <p>
     * This directory is typically used for cloning repositories before
     * deploying content.
     * </p>
     * 
     * @return the path to the newly created temporary directory
     * @throws IOException if the directory cannot be created
     */
    private Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("iics-doc-gen-git-");
    }

    /**
     * Recursively deletes a directory and all its contents.
     * 
     * <p>
     * This method is used to clean up temporary directories after Git
     * operations.
     * </p>
     * 
     * @param directory the directory to delete
     * @throws IOException if any file or subdirectory cannot be deleted
     */
    private void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }

        if (!directory.delete()) {
            throw new IOException("Failed to delete: " + directory);
        }
    }
}
