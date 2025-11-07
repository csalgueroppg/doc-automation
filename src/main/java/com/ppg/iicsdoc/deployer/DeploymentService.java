package com.ppg.iicsdoc.deployer;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.config.AppConfig;
import com.ppg.iicsdoc.exception.DeploymentException;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentStrategy;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

import lombok.extern.slf4j.Slf4j;

/**
 * Control service for managing documentation deployment operations.
 * 
 * <p>
 * This service coordinates deployment across multiple strategies, including:
 * </p>
 * <ul>
 * <li>{@link DeploymentStrategy#LOCAL_FILESYSTEM}</li>
 * <li>{@link DeploymentStrategy#DOCUSAURUS}</li>
 * <li>{@link DeploymentStrategy#GIT_REPOSITORY}</li>
 * </ul>
 * 
 * <p>
 * It delegates actual deployment logic to strategy-specific handlers and
 * provides unified methods for deploying single documents, batches,
 * validating configurations, and rolling back failed deployments.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * MarkdownDocument doc = new MarkdownDocument("intro.md", "# Welcome");
 * DeploymentConfig config = DeploymentConfig.localFilesystem("./docs");
 *
 * DeploymentService service = ...; // Injected via Spring
 * DeploymentResult result = service.deploy(doc, config);
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
@Slf4j
@Service
public class DeploymentService {

    private final LocalFileSystemHandler filesystemHandler;
    private final DocusaurusHandler docusaurusHandler;
    private final GitDeploymentHandler gitHandler;
    private final AppConfig.DeploymentProperties deploymentProperties;

    /**
     * Constructs a new {@code DeploymentService} with the required handlers
     * and configuration.
     * 
     * @param filesystemHandler    handler for local file system deployments
     * @param docusaurusHandler    handler for Docusaurus deployments
     * @param gitHandler           handler for Git repository deployments
     * @param deploymentProperties application-level deployment configuration
     */
    public DeploymentService(
            LocalFileSystemHandler filesystemHandler,
            DocusaurusHandler docusaurusHandler,
            GitDeploymentHandler gitHandler,
            AppConfig.DeploymentProperties deploymentProperties) {
        this.filesystemHandler = filesystemHandler;
        this.docusaurusHandler = docusaurusHandler;
        this.gitHandler = gitHandler;
        this.deploymentProperties = deploymentProperties;
    }

    public DeploymentResult deploy(MarkdownDocument document) {
        return deploy(document, createDefaultConfig());
    }

    /**
     * Deploys a single markdown document using the specified configuration.
     * 
     * @param document the markdown document to display
     * @param config   the deployment configuration
     * @return a {@link DeploymentResult} indicating success or failure
     */
    public DeploymentResult deploy(
            MarkdownDocument document,
            DeploymentConfig config) {
        log.info("Deploying document: {} using strategy: {}",
                document.getFilename(), config.getStrategy());

        long startTime = System.currentTimeMillis();

        try {
            DeploymentHandler handler = getHandler(config.getStrategy());
            DeploymentResult result = handler.deploy(document, config);

            long duration = System.currentTimeMillis() - startTime;
            log.info(
                    "Deployment completed in {} ms. Success: {}",
                    duration,
                    result.isSuccess());

            return result;
        } catch (Exception e) {
            log.error("Deployment failed", e);
            return DeploymentResult.failure(
                    "Deployment failed: " + e.getMessage(),
                    config.getStrategy());
        }
    }

    /**
     * Deploys a batch of markdown documents using the default configuration.
     * 
     * <p>
     * The default configuration is determined from application properties.
     * </p>
     *
     * @param documents the list of markdown documents to deploy
     * @return a {@link DeploymentResult} summarizing the batch deployment
     */

    public DeploymentResult deployBatch(List<MarkdownDocument> documents) {
        DeploymentConfig config = createDefaultConfig();
        return deployBatch(documents, config);
    }

    /**
     * Deploys a batch of markdown documents using the specified configuration.
     *
     * @param documents the list of markdown documents to deploy
     * @param config    the deployment configuration
     * @return a {@link DeploymentResult} summarizing the batch deployment
     */

    public DeploymentResult deployBatch(
            List<MarkdownDocument> documents,
            DeploymentConfig config) {
        log.info("Deploying {} documents using strategy: {}",
                documents.size(), config.getStrategy());

        long startTime = System.currentTimeMillis();

        try {
            DeploymentHandler handler = getHandler(config.getStrategy());
            DeploymentResult result = handler.deployBatch(documents, config);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch deployment completed in {} ms. Deployed {}/{}",
                    duration,
                    result.getDeployedFileCount(),
                    documents.size());

            return result;
        } catch (Exception e) {
            log.error("Batch deployment failed", e);
            return DeploymentResult.failure(
                    "Deployment failed: " + e.getMessage(),
                    config.getStrategy());
        }
    }

    /**
     * Attempts to roll back a previous deployment.
     *
     * @param result the result of the deployment to roll back
     * @return {@code true} if rollback was successful; {@code false} otherwise
     */
    public boolean rollback(DeploymentResult result) {
        log.info("Rolling back deployment: {}", result.getTargetPath());

        try {
            DeploymentHandler handler = getHandler(result.getStrategy());
            return handler.rollback(result);
        } catch (Exception e) {
            log.error("Rollback failed", e);
            return false;
        }
    }

    /**
     * Validates the given deployment configuration.
     *
     * @param config the deployment configuration to validate
     * @return {@code true} if the configuration is valid; {@code false} otherwise
     */
    public boolean validateConfig(DeploymentConfig config) {
        try {
            DeploymentHandler handler = getHandler(config.getStrategy());
            return handler.validateConfig(config);
        } catch (Exception e) {
            log.error("Failed to validate config", e);
            return false;
        }
    }

    /**
     * Resolves the appropriate {@link DeploymentHandler} based on the strategy.
     *
     * @param strategy the deployment strategy
     * @return the corresponding handler
     * @throws DeploymentException if the strategy is unsupported
     */
    private DeploymentHandler getHandler(DeploymentStrategy strategy) {
        return switch (strategy) {
            case LOCAL_FILESYSTEM -> filesystemHandler;
            case DOCUSAURUS -> docusaurusHandler;
            case GIT_REPOSITORY -> gitHandler;
            default -> throw new DeploymentException(
                    "Unsupported deployment strategy: " + strategy,
                    null);
        };
    }

    /**
     * Creates a default deployment configuration based on application properties.
     * 
     * <p>
     * If a Docusaurus path is configured, it returns a Docusaurus config;
     * otherwise, it defaults to local file system deployment.
     * </p>
     *
     * @return a default {@link DeploymentConfig}
     */
    private DeploymentConfig createDefaultConfig() {
        String docusaurusPath = deploymentProperties.getDocusuarusPath();
        if (docusaurusPath != null && !docusaurusPath.isEmpty()) {
            return DeploymentConfig.docusaurus(docusaurusPath);
        }

        return DeploymentConfig.localFilesystem("./generated-docs");
    }
}
