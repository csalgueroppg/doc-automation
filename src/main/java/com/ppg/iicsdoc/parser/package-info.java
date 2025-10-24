/**
 * Provides services for parsing Informatica IICS XML metadata files.
 *
 * <p>This package contains the {@link com.ppg.iicsdoc.parser.XMLParserService}, which is responsible
 * for reading, validating, and extracting structured metadata from IICS XML exports.
 * The parsed data is converted into {@link com.ppg.iicsdoc.model.ParsedMetadata}, which can be
 * used for documentation generation, visualization, or further processing.
 *
 * <p>Key features include:
 * <ul>
 *   <li>DOM-based XML parsing</li>
 *   <li>Validation of required metadata structures</li>
 *   <li>Extraction of process details, connections, transformations, and OpenAPI endpoints</li>
 * </ul>
 *
 * <p>This package is designed for use within a Spring context and typically registered
 * as a singleton service.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
package com.ppg.iicsdoc.parser;