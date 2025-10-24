package com.ppg.iicsdoc.deployer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.exception.DeploymentException;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentStrategy;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * Deployment handler that writes markdown documents to the local file
 * system.
 * 
 * <p>
 * This implementation supports backup creation, overwrite control, and
 * post-deployment validation. It can deploy individual documents or batches,
 * and supports rollback by restoring backups or deleting deployed files.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * DeploymentConfig config = DeploymentConfig.localFileSystem("/docs/output");
 * MarkdownDocument doc = new MarkdownDocument("intro.md", "# Welcome to the docs");
 * DeploymentHandler handler = new LocalFileSystemHandler();
 * DeploymentResult result = handler.deploy(doc, config);
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
@Slf4j
@Component
public class LocalFileSystemHandler implements DeploymentHandler {

    /** Timestamp formatter for backup files. */
    private static final DateTimeFormatter BACKUP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public DeploymentResult deploy(
            MarkdownDocument document,
            DeploymentConfig config) {
        log.info("Deploying document to local filesystem: {}",
                document.getFilename());

        try {
            if (!validateConfig(config)) {
                return DeploymentResult.failure(
                        "Invalid deployment configuration",
                        DeploymentStrategy.LOCAL_FILESYSTEM);
            }

            Path targetDir = Paths.get(config.getTargetPath());
            Path targetFile = targetDir.resolve(document.getFilename());

            Files.createDirectories(targetDir);
            if (config.isCreateBackup() && Files.exists(targetFile)) {
                createBackup(targetFile);
            }

            Files.writeString(targetFile, document.getContent());
            log.info("Document deployed to: {}", targetFile.toAbsolutePath());

            if (config.isValidateAfterDeployment()) {
                validateDeployment(targetFile, document);
            }

            return DeploymentResult.success(
                    targetFile.toAbsolutePath().toString(),
                    List.of(document.getFilename()),
                    DeploymentStrategy.LOCAL_FILESYSTEM);
        } catch (IOException e) {
            log.error("Failed to deploy document", e);
            throw new DeploymentException(
                    "Failed to deploy document to filesystem",
                    config.getTargetPath(),
                    e);
        }
    }

    @Override
    public DeploymentResult deployBatch(
            List<MarkdownDocument> documents,
            DeploymentConfig config) {
        log.info("Deploying {} documents to local filesystem", documents.size());

        List<String> deployedFiles = new ArrayList<>();
        Path targetDir = Paths.get(config.getTargetPath());

        try {
            Files.createDirectories(targetDir);
            for (MarkdownDocument doc : documents) {
                DeploymentResult result = deploy(doc, config);
                if (result.isSuccess()) {
                    deployedFiles.add(doc.getFilename());
                } else {
                    log.warn("Failed to deploy: {}", doc.getFilename());
                }
            }

            log.info("Successfully deployed {}/{} documents",
                    deployedFiles.size(), documents.size());

            return DeploymentResult.success(
                    targetDir.toAbsolutePath().toString(),
                    deployedFiles,
                    DeploymentStrategy.LOCAL_FILESYSTEM);
        } catch (Exception e) {
            log.error("Backup deployment failed", e);
            throw new DeploymentException(
                    "Batch deployment failed",
                    config.getTargetPath(),
                    e);
        }
    }

    @Override
    public boolean validateConfig(DeploymentConfig config) {
        if (config == null) {
            log.error("Deployment config is null");
            return false;
        }

        if (config.getTargetPath() == null || config.getTargetPath().isEmpty()) {
            log.error("Target path is required");
            return false;
        }

        Path targetPath = Paths.get(config.getTargetPath());
        if (Files.exists(targetPath) && !Files.isWritable(targetPath)) {
            log.error("Target path is not writable: {}", targetPath);
            return false;
        }

        return true;
    }

    @Override
    public boolean rollback(DeploymentResult deploymentResult) {
        log.info("Rolling back deployment: {}", deploymentResult.getTargetPath());
        if (deploymentResult == null || !deploymentResult.isSuccess()) {
            log.warn("Noting to rollback");
            return false;
        }

        try {
            for (String fileName : deploymentResult.getDeployedFiles()) {
                Path targetFile = Paths.get(deploymentResult.getTargetPath(), fileName);
                Path backupFile = findLatestBackup(targetFile);

                if (backupFile != null && Files.exists(backupFile)) {
                    Files.copy(backupFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Restored from backup: {}", fileName);
                } else {
                    Files.deleteIfExists(targetFile);
                    log.info("Deleted deployed file: {}", fileName);
                }
            }

            return true;
        } catch (IOException e) {
            log.error("Rollback failed", e);
            return false;
        }
    }

    // Helper methods
    /**
     * Creates a timestamped backup of the specified file.
     * 
     * @param file the file to backup
     * @throws IOException if the backup operation fails
     */
    private void createBackup(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(BACKUP_FORMATTER);
        String backupFileName = file.getFileName()
                .toString() + ".backup_" + timestamp;

        Path backupFile = file.getParent().resolve(backupFileName);
        Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);

        log.debug("Created backup: {}", backupFile.getFileName());
    }

    /**
     * Finds the most recent backup file for the given target file.
     * 
     * @param file the original file
     * @return the path to the latest backup file, or {@code null} if none found
     * @throws IOException if an error occurs while searching for backups
     */
    private Path findLatestBackup(Path file) throws IOException {
        Path dir = file.getParent();
        String baseName = file.getFileName().toString();

        return Files.list(dir)
                .filter(p -> p.getFileName().toString().startsWith(baseName + ".backup_"))
                .max((p1, p2) -> p1.getFileName().toString().compareTo(p2.getFileName().toString()))
                .orElse(null);
    }

    /**
     * Validates that the deployed file matches the original document content.
     * 
     * @param targetFile the deployed file path
     * @param document   the original markdown document
     * @throws IOException         if reading the file fails
     * @throws DeploymentException if validation fails
     */
    private void validateDeployment(
            Path targetFile,
            MarkdownDocument document) throws IOException {
        if (!Files.exists(targetFile)) {
            throw new DeploymentException("Deployed file does not exist: " +
                    targetFile.toString());
        }

        long fileSize = Files.size(targetFile);
        if (fileSize == 0) {
            throw new DeploymentException("Deployed file is empty: " +
                    targetFile.toString());
        }

        String deployedContent = Files.readString(targetFile);
        if (!deployedContent.equals(document.getContent())) {
            throw new DeploymentException(
                    "Deployed content does not match source: " +
                            targetFile.toString());
        }

        log.debug("Deployment validated successfully");
    }
}
