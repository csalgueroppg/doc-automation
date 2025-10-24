/**
 * Defines domain models representing core concepts in Informatica IICS metadata.
 *
 * <p>This package contains classes and enums that model the structure and semantics
 * of IICS processes, connections, transformations, data flows, and OpenAPI endpoints.
 * These domain objects are used throughout the documentation generation pipeline to
 * encapsulate parsed metadata in a structured and type-safe manner.
 *
 * <p>Key model types include:
 * <ul>
 *   <li>{@link com.ppg.iicsdoc.model.domain.ParsedMetadata} – Aggregates all extracted metadata from an IICS XML file</li>
 *   <li>{@link com.ppg.iicsdoc.model.domain.Connection}, {@link com.ppg.iicsdoc.model.domain.Transformation}, {@link com.ppg.iicsdoc.model.domain.DataFlow} – Represent individual components of an IICS process</li>
 *   <li>{@link com.ppg.iicsdoc.model.domain.OpenAPIEndpoint}, {@link com.ppg.iicsdoc.model.domain.Parameter}, {@link com.ppg.iicsdoc.model.domain.Response} – Model RESTful API metadata</li>
 *   <li>{@link com.ppg.iicsdoc.model.domain.ProcessType}, {@link com.ppg.iicsdoc.model.domain.ConnectionType}, {@link com.ppg.iicsdoc.model.domain.TransformationType}, {@link com.ppg.iicsdoc.model.domain.AuthenticationType}, {@link com.ppg.iicsdoc.model.domain.ParameterLocation}, {@link com.ppg.iicsdoc.model.domain.HttpMethod} – Enumerations for metadata classification</li>
 * </ul>
 *
 * <p>These models are designed to be immutable or safely mutable, and are used
 * as inputs to services such as diagram generation, markdown rendering, and deployment.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
package com.ppg.iicsdoc.model.domain;