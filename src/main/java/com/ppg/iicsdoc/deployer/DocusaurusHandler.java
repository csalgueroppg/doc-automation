package com.ppg.iicsdoc.deployer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Deployment handler for Docusaurus documentation sites.
 * 
 * <p>
 * This implementation enhances markdown documents with frontmatter
 * metadata, organizes them into categories, and delegates file writing
 * to a {@link LocalFileSystemHandler}.
 * </p>
 * 
 * <p>
 * It supports single and batch deployments, configuration validation, and
 * rollback.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * DeploymentConfig config = DeploymentConfig.docusaurus("/website/docs");
 * config.setDocusaurusCategory("integration-guides");
 * 
 * MarkdownDocument doc = new MarkdownDocument("guide.md", "Integration steps...");
 * DeploymentHandler handler = new DocusaurusHandler(new LocalFileSystemHandler());
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
public class DocusaurusHandler implements DeploymentHandler {

    /** LocalFileSystemHandler instance for delegation. */
    private final LocalFileSystemHandler filesystemHandler;

    /**
     * Constructs a new {@code DocusaurusHandler} with a delegated local
     * file system handler.
     * 
     * @param fileSystemHandler the handler used to write files to disk
     */
    public DocusaurusHandler(LocalFileSystemHandler fileSystemHandler) {
        this.filesystemHandler = fileSystemHandler;
    }

    @Override
    public DeploymentResult deploy(
            MarkdownDocument document,
            DeploymentConfig config) {
        log.info("Deploying document to Docusaurus: {}", document.getFilename());

        try {
            if (!validateConfig(config)) {
                return DeploymentResult.failure(
                        "Invalid Docusaurus configuration",
                        DeploymentStrategy.DOCUSAURUS);
            }

            MarkdownDocument enhanceDoc = enhanceForDocusaurus(document, config);
            Path docsPath = Paths.get(config.getDocusaurusDocsPath());

            if (config.getDocusaurusCategory() != null) {
                docsPath = docsPath.resolve(config.getDocusaurusCategory());
            }

            Files.createDirectories(docsPath);
            createCategoryMetadata(docsPath, config);

            DeploymentConfig fsConfig = DeploymentConfig.builder()
                    .strategy(DeploymentStrategy.LOCAL_FILESYSTEM)
                    .targetPath(docsPath.toString())
                    .createBackup(config.isCreateBackup())
                    .overwriteExisting(config.isOverwriteExisting())
                    .validateAfterDeployment(config.isValidateAfterDeployment())
                    .build();

            DeploymentResult result = filesystemHandler.deploy(enhanceDoc, fsConfig);

            log.info("Successfully deployed to Docusaurus: {}", docsPath);
            return result;
        } catch (IOException e) {
            log.error("Failed to deploy to Docusaurus", e);
            throw new DeploymentException(
                    "Failed to deploy to Docusaurus",
                    config.getDocusaurusDocsPath(),
                    e);
        }
    }

    @Override
    public DeploymentResult deployBatch(
            List<MarkdownDocument> documents,
            DeploymentConfig config) {
        log.info("Deploying {} documents to Docusaurus", documents.size());

        List<String> deployedFiles = new ArrayList<>();
        for (MarkdownDocument doc : documents) {
            try {
                DeploymentResult result = deploy(doc, config);
                if (result.isSuccess()) {
                    deployedFiles.addAll(result.getDeployedFiles());
                }
            } catch (Exception e) {
                log.warn("Failed to deploy document: {}",
                        doc.getFilename(), e);
            }
        }

        Path docsPath = Paths.get(config.getDocusaurusDocsPath());
        if (config.getDocusaurusCategory() != null) {
            docsPath = docsPath.resolve(config.getDocusaurusCategory());
        }

        return DeploymentResult.success(
                docsPath.toString(),
                deployedFiles,
                DeploymentStrategy.DOCUSAURUS);
    }

    @Override
    public boolean validateConfig(DeploymentConfig config) {
        if (config == null) {
            log.error("Deployment config is null");
            return false;
        }

        if (config.getDocusaurusDocsPath() == null ||
                config.getDocusaurusDocsPath().isEmpty()) {
            log.error("Docusuarus doc path is required");
            return false;
        }

        Path docsPath = Paths.get(config.getDocusaurusDocsPath());
        if (Files.exists(docsPath)) {
            if (!Files.isDirectory(docsPath)) {
                log.error("Docusaurus path exists but is not a directory: {}",
                        docsPath);
                return false;
            }

            if (!Files.isWritable(docsPath)) {
                log.error("Docusaurus path is not writable: {}", docsPath);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean rollback(DeploymentResult deploymentResult) {
        return filesystemHandler.rollback(deploymentResult);
    }

    // Helper methods

    /**
     * Enhances a markdown document with Docusaurus frontmatter metadata.
     * 
     * <p>
     * Adds fields such as {@code id}, {@code title}, {@code sidebar_label},
     * and optionally {@code sidebar_position}.
     * </p>
     * 
     * @param document the original markdown document
     * @param config   the deployment configuration
     * @return a new {@link MarkdownDocument} with enhanced content
     */
    private MarkdownDocument enhanceForDocusaurus(
            MarkdownDocument document,
            DeploymentConfig config) {
        String content = document.getContent();
        if (!content.startsWith("---")) {
            StringBuilder frontMatter = new StringBuilder();

            frontMatter.append("---\n");
            frontMatter.append("id: ")
                    .append(generateId(document.getFilename()))
                    .append("\n");
            frontMatter.append("title: ")
                    .append(document.getTitle())
                    .append("\n");
            frontMatter.append("sidebar_label: ")
                    .append(document.getTitle())
                    .append("\n");

            if (config.getDocusaurusCategory() != null) {
                frontMatter.append("sidebar_position: auto\n");
            }

            frontMatter.append("---\n\n");
            content = frontMatter.toString() + content;
        }

        return MarkdownDocument.builder()
                .filename(document.getFilename())
                .title(document.getTitle())
                .content(content)
                .generatedAt(document.getGeneratedAt())
                .metadata(document.getMetadata())
                .build();
    }

    /**
     * Crates a Docusaurus category metadata file if it doesn't already exists.
     * 
     * <p>
     * The metadata file defines the label, position, and link type for the
     * category.
     * </p>
     * 
     * @param categoryPath the path to the category folder
     * @param config       the deployment configuration
     * @throws IOException if writing the metadata file fails
     */

    private void createCategoryMetadata(Path categoryPath, DeploymentConfig config) throws IOException {
        String categoryName = config.getDocusaurusCategory();
        if (categoryName == null || categoryName.isEmpty()) {
            log.debug("No category provided; skipping metadata creation.");
            return;
        }

        Path metadataFile = categoryPath.resolve("_category.json");
        if (Files.exists(metadataFile)) {
            log.debug("Category metadata already exists: {}", metadataFile);
            return;
        }

        String categoryLabel = categoryName.substring(0, 1).toUpperCase() +
                categoryName.substring(1).replace("-", " ");

        String metadata = (
                """
                {
                  "label": "%s",
                  "position": 2,
                  "link": {
                    "type": "generated-index",
                    "description": "Auto generated IICS process documentation"
                  }
                }\
                """).formatted(
                categoryLabel);

        Files.writeString(metadataFile, metadata);
        log.info("Created category metadata: {}", metadataFile);
    }

    /**
     * Generates a Docusaurus-friendly document ID from the filename.
     * 
     * @param filename the original filename
     * @return a normalized ID string
     */
    private String generateId(String filename) {
        return filename
                .replace(".md", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-");
    }
}
