package com.ppg.iicsdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;

/**
 * Central configuration class for the Spring Boot Application.
 * 
 * <p>
 * Defines beans for core infrastructure components such as {@link WebClient}
 * and binds external configuration properties to structured POJOs.
 * </p>
 * 
 * <p>
 * Configuration properties are loaded from the application environment using
 * the {@code iics.*} prefix and mapped to dedicated propery classes.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Configuration
public class AppConfig {

    /**
     * Creates a {@link WebClient} bean with an increased in-memory buffer
     * size.
     * 
     * <p>
     * This client is suitable for handling large payloads, such as file
     * uploads or AI responses, up to 16 MB.
     * </p>
     * 
     * @param builder the WebClient builder injected by Spring
     * @return a configured {@link WebClient} instance
     */
    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    /**
     * Binds configuration properties under {@code iics.parser} to
     * {@link ParserProperties}.
     * 
     * @return a populated {@link ParserProperties} bean
     */
    @Bean
    @ConfigurationProperties(prefix = "iics.parser")
    ParserProperties parserProperties() {
        return new ParserProperties();
    }

    /**
     * Binds configuration properties under {@code iics.ai} to
     * {@link AIProperties}.
     *
     * @return a populated {@link AIProperties} bean
     */
    @Bean
    @ConfigurationProperties(prefix = "iics.ai")
    AIProperties aiProperties() {
        return new AIProperties();
    }

    /**
     * Binds configuration properties under {@code iics.deployment}
     * to {@link DeploymentProperties}.
     *
     * @return a populated {@link DeploymentProperties} bean
     */
    @Bean
    @ConfigurationProperties(prefix = "iics.deployment")
    DeploymentProperties deploymentProperties() {
        return new DeploymentProperties();
    }

    /**
     * Configuration properties for parser behavior.
     * <p>
     * Loaded from {@code iics.parser.*}.
     */
    @Data
    public static class ParserProperties {
        /** Maximum allowed file size in bytes. Default is 100MB. */
        private long maxFileSizeBytes = 100_000_000;

        /** Whether to validate schema during parsing. */
        private boolean validateSchema = true;

        /** Enables strict parsing mode. */
        private boolean strictMode = false;
    }

    /**
     * Configuration properties for AI integration.
     * <p>
     * Loaded from {@code iics.ai.*}.
     */
    @Data
    public static class AIProperties {
        /** Base URL for the AI API endpoint. */
        private String apiUrl;

        /** API key used for authentication. */
        private String apiKey;

        /** Default model identifier to use for AI requests. */
        private String model = "claude-3-5-sonnet-20241022";

        /** Timeout for AI requests in seconds. */
        private int timeoutSeconds = 30;

        /** Maximum number of retry attempts for failed requests. */
        private int maxRetries = 3;
    }

    /**
     * Configuration properties for deployment automation.
     * <p>
     * Loaded from {@code iics.deployment.*}.
     */
    @Data
    public static class DeploymentProperties {
        /** Path to the Docusaurus documentation directory. */
        private String docusuarusPath;

        /** Git repository URL for deployment. */
        private String gitRepoUrl;

        /** Git branch to use for commits. Default is {@code main}. */
        private String gitBranch = "main";

        /** Whether to automatically commit changes during deployment. */
        private boolean autoCommit = true;
    }

}
