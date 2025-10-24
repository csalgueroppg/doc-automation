/**
 * Provides deployment strategies and services for publishing generated documentation.
 *
 * <p>This package defines the core deployment infrastructure for the IICS Documentation Generator.
 * It includes abstractions and implementations for deploying {@link com.ppg.iicsdoc.model.MarkdownDocument}
 * instances to various targets such as:
 * <ul>
 *   <li>Local filesystem directories</li>
 *   <li>Docusaurus documentation sites</li>
 *   <li>Remote Git repositories</li>
 * </ul>
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.ppg.iicsdoc.deployer.DeploymentHandler} – Strategy interface for deployment operations</li>
 *   <li>{@link com.ppg.iicsdoc.deployer.DeploymentService} – Central service coordinating deployment across strategies</li>
 *   <li>Strategy-specific handlers implementing {@code DeploymentHandler}</li>
 * </ul>
 *
 * <p>Deployment operations include:
 * <ul>
 *   <li>Single and batch document deployment</li>
 *   <li>Configuration validation</li>
 *   <li>Rollback support for failed deployments</li>
 * </ul>
 *
 * <p>This package is designed for extensibility, allowing new deployment strategies to be added
 * with minimal changes to the core service.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-24
 */
package com.ppg.iicsdoc.deployer;