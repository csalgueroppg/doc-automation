/**
 * Contains models and configuration classes related to deployment strategies
 * for the IICS Documentation Generator.
 *
 * <p>This package defines the core data structures used to configure, execute,
 * and report on documentation deployment operations. It supports multiple
 * deployment targets such as local filesystem and static site generators.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.ppg.iicsdoc.model.deployment.DeploymentConfig} – Encapsulates deployment settings such as target path, strategy, and flags</li>
 *   <li>{@link com.ppg.iicsdoc.model.deployment.DeploymentResult} – Represents the outcome of a deployment operation, including success status and deployed files</li>
 *   <li>{@link com.ppg.iicsdoc.model.deployment.DeploymentStrategy} – Enum defining supported deployment strategies (e.g., LOCAL_FILESYSTEM, DOCUSAURUS)</li>
 * </ul>
 *
 * <p>These models are used throughout the documentation pipeline to ensure
 * consistent and configurable deployment behavior.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
package com.ppg.iicsdoc.model.deployment;