/**
 * This package contains domain model classes representing the core entities
 * and data structures used throughout the application.
 * 
 * <p>
 * These classes define the structure and semantics of:
 * </p>
 * 
 * <ul>
 *   <li>Authentication and connection types ({@link com.ppg.iicsdoc.model.domain.AuthenticationType}, {@link com.ppg.iicsdoc.model.domain.Connection}, {@link com.ppg.iicsdoc.model.domain.ConnectionType})</li>
 *   <li>Data flow components ({@link com.ppg.iicsdoc.model.domain.DataFlow}, {@link com.ppg.iicsdoc.model.domain.DataSource}, {@link com.ppg.iicsdoc.model.domain.DataTarget})</li>
 *   <li>API interaction models ({@link com.ppg.iicsdoc.model.domain.OpenAPIEndpoint}, {@link com.ppg.iicsdoc.model.domain.HttpMethod}, {@link com.ppg.iicsdoc.model.domain.Parameter}, {@link com.ppg.iicsdoc.model.domain.ParameterLocation}, {@link com.ppg.iicsdoc.model.domain.Response})</li>
 *   <li>Metadata and transformation logic ({@link com.ppg.iicsdoc.model.domain.ParsedMetadata}, {@link com.ppg.iicsdoc.model.domain.Transformation}, {@link com.ppg.iicsdoc.model.domain.TransformationType})</li>
 *   <li>Field definitions and processing types ({@link com.ppg.iicsdoc.model.domain.Field}, {@link com.ppg.iicsdoc.model.domain.ProcessType})</li>
 * </ul>
 * 
 * <p>
 * These models serve as the foundation for data exchange, configuration, and
 * transformation within the system.
 * </p>
 */
package com.ppg.iicsdoc.model.domain;