/**
 * Provides AI-powered services for generating and validating Mermaid diagrams
 * from Informatica IICS metadata.
 *
 * <p>This package includes components that interact with AI agents to produce
 * visual representations of IICS processes and API endpoints. It supports prompt
 * generation, diagram validation, and resilience mechanisms for reliable communication
 * with external AI services.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.ppg.iicsdoc.ai.AIAgentService} – Core service for generating Mermaid diagrams using AI</li>
 *   <li>{@link com.ppg.iicsdoc.ai.PromptBuilder} – Utility for constructing structured prompts from metadata</li>
 *   <li>{@link com.ppg.iicsdoc.ai.MermaidValidator} – Validator for Mermaid diagram syntax and structure</li>
 * </ul>
 *
 * <p>Designed for use within a Spring application context, these components are
 * registered as services or beans and support dependency injection.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-21
 */
package com.ppg.iicsdoc.ai;