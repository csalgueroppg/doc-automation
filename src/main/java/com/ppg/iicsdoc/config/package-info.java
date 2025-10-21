/**
 * Contains application-level configuration classes for Spring Boot.
 * <p>
 * This package defines and registers beans related to:
 * <ul>
 *   <li><b>Infrastructure setup</b> — such as {@link org.springframework.web.reactive.function.client.WebClient}</li>
 *   <li><b>Externalized configuration</b> — using {@code @ConfigurationProperties} to bind application settings</li>
 *   <li><b>Asynchronous processing</b> — via a custom {@link java.util.concurrent.Executor} for parallel task execution</li>
 * </ul>
 * <p>
 * These configurations are loaded at application startup and support modular, maintainable, and testable architecture.
 */
package com.ppg.iicsdoc.config;