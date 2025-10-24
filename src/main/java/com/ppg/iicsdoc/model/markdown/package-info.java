/**
 * Defines models for representing Markdown-based documentation generated
 * from IICS metadata and diagrams.
 *
 * <p>This package contains classes that encapsulate the structure, metadata,
 * and content sections of a Markdown document. These models are used to
 * organize and transport documentation artifacts throughout the generation
 * and deployment pipeline.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.ppg.iicsdoc.model.markdown.MarkdownDocument} – Represents a complete Markdown document, including content and metadata</li>
 *   <li>{@link com.ppg.iicsdoc.model.markdown.DocumentMetadata} – Holds metadata such as title, author, and creation date</li>
 *   <li>{@link com.ppg.iicsdoc.model.markdown.DocumentSection} – Represents individual sections of a Markdown document, such as headers, diagrams, or tables</li>
 * </ul>
 *
 * <p>These models are designed to be serializable and compatible with templating
 * engines and deployment strategies such as Docusaurus.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
package com.ppg.iicsdoc.model.markdown;