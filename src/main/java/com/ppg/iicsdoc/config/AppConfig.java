package com.ppg.iicsdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;

/**
 * @class AppConfig Configuration class for the spring boot application.
 *
 * @author Carlos Salguero
 * @version 1.0.0
 */
@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "iics.parser")
    public ParserProperties parserProperties() {
        return new ParserProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "iics.ai")
    public AIProperties aiProperties() {
        return new AIProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "iics.deployment")
    public DeploymentProperties deploymentProperties() {
        return new DeploymentProperties();
    }

    @Data 
    public static class ParserProperties {
        private long maxFileSizeBytes = 100_000_000;
        private boolean validateSchema = true;
        private boolean strictMode = false;
    }

    @Data
    public static class AIProperties {
        private String apiUrl;
        private String apiKey;
        private String model = "claude-3-5-sonnet-20241022";
        private int timeoutSeconds = 30;
        private int maxRetries = 3;
    }

    @Data 
    public static class DeploymentProperties {
        private String docusuarusPath;
        private String gitRepoUrl;
        private String gitBranch = "main";
        private boolean autoCommit = true;
    }
}
