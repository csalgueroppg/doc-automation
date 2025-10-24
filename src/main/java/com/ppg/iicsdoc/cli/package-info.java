/**
 * Provides command-line interface (CLI) support for the IICS Documentation Generator.
 *
 * <p>This package contains the {@link com.ppg.iicsdoc.cli.DocumentationGeneratorCLI} class,
 * which enables headless execution of the documentation generation pipeline via terminal commands.
 * It parses command-line arguments, orchestrates metadata parsing, diagram generation using AI,
 * Markdown documentation creation, and deployment to configured targets.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Support for input and output path configuration via CLI flags</li>
 *   <li>Help message rendering for usage guidance</li>
 *   <li>Integration with Spring Boot profiles to isolate CLI execution from other application modes</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * java -jar iics-doc-gen.jar --input=process.xml --output=./docs
 * }</pre>
 *
 * <p>Environment variables:
 * <ul>
 *   <li>{@code AI_API_KEY} – API key for AI-based diagram generation</li>
 *   <li>{@code DOCUSAURUS_PATH} – Default deployment path for documentation</li>
 * </ul>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
package com.ppg.iicsdoc.cli;