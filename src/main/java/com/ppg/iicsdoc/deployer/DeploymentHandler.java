package com.ppg.iicsdoc.deployer;

import java.util.List;

import com.ppg.iicsdoc.model.deployment.DeploymentResult;
import com.ppg.iicsdoc.model.deployment.DeploymentConfig;
import com.ppg.iicsdoc.model.markdown.MarkdownDocument;

/**
 * Defines the contract for handling deployment options.
 * 
 * <p>
 * Implementations of this interface are responsible for deploying one
 * or more {@link MarkdownDocument} instances using a specified
 * {@link DeploymentConfig}.
 * </p>
 * 
 * <p>
 * This abstraction allows different deployment strategies (e.g., local
 * file system, Git repository, Docusaurus site) to be implemented and
 * plugged into the system interchangeably.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
public interface DeploymentHandler {

    /**
     * Deploys a single {@link MarkdownDocument} using the provided
     * configuration.
     * 
     * <p>
     * This method performs the deployment based on the strategy and options
     * defined in the {@code config}. It returns a {@link DeploymentResult}
     * indicating the outcome of the operation.
     * </p>
     * 
     * @param document the markdown document to deploy
     * @param config   the deployment configuration to use
     * @return a {@link DeploymentResult} representing the result of the
     *         deployment.
     */
    DeploymentResult deploy(MarkdownDocument document, DeploymentConfig config);

    /**
     * Deploys a branch of {@link MarkdownDocument} instances using the
     * provided configuration.
     * 
     * <p>
     * This method is intended for bulk operations where multiple documents
     * are deployed together. The deployment strategy may handle them
     * individually or as a group, depending on its implementation.
     * </p>
     * 
     * @param documents the list of markdown documents to deploy
     * @param config    the deployment configuration to use
     * @return a {@link DeploymentResult} representing the result of the
     *         batch deployment.
     */
    DeploymentResult deployBatch(List<MarkdownDocument> documents, DeploymentConfig config);

    /**
     * Validates the given deployment configuration.
     * 
     * <p>
     * This method checks whether the provided {@link DeploymentConfig} contains
     * all required fields and values for the selected {@link DeploymentStrategy}.
     * </p>
     * 
     * <p>
     * It helps prevent runtime errors by ensuring the configuration is complete
     * and compatible with the handler's capabilities.
     * </p>
     * 
     * @param config the deployment configuration to validate
     * @return {@code true} if the configuration is valid; {@code false} otherwise
     */
    boolean validateConfig(DeploymentConfig config);

    /**
     * Attempts to roll back a previous deployment.
     * 
     * <p>
     * This method is used to undo or revert changes made during a deployment,
     * such as restoring backups, deleting deployed files, or reverting Git commits.
     * </p>
     * 
     * <p>
     * The rollback behavior depends on the strategy and the information available
     * in the {@link DeploymentResult}.
     * </p>
     *
     * @param deploymentResult the result of the deployment to roll back
     * @return {@code true} if the rollback was successful; {@code false} otherwise
     */
    boolean rollback(DeploymentResult deploymentResult);
}
