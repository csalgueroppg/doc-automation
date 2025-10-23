/**
 * Provides data models for AI-related requests and responses.
 * <p>
 * This package contains classes that represent the structure of payloads
 * exchanged with AI services,
 * including request configurations, message formats, and response metadata.
 * </p>
 *
 * <p>
 * Key classes include:
 * </p>
 * <ul>
 * <li>{@link com.ppg.iicsdoc.model.ai.AIRequest} – Represents a request to an
 * AI model, including model parameters and message history.</li>
 * <li>{@link com.ppg.iicsdoc.model.ai.AIResponse} – Represents the structured
 * response from an AI model, including content and usage statistics.</li>
 * <li>{@link com.ppg.iicsdoc.model.ai.AIResponse.Content} – Represents
 * individual content blocks within an AI response.</li>
 * <li>{@link com.ppg.iicsdoc.model.ai.AIResponse.Usage} – Contains token usage
 * details for the AI interaction.</li>
 * </ul>
 *
 * <p>
 * This package is designed to support serialization and deserialization of JSON
 * payloads,
 * typically used in RESTful API communication with AI services.
 * </p>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
package com.ppg.iicsdoc.model.ai;
