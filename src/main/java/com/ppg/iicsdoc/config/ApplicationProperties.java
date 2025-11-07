package com.ppg.iicsdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Centralized configuration class for application-level settings.
 * 
 * <p>
 * This class maps all configuration properties prefixed with {@code iics.*}
 * from {@code application.yml} or {@code application.properties}. It provides
 * typed access to subsystems like validation, parsing, caching, and
 * performance tuning.
 * </p>
 * 
 * <p>
 * Each nested configuration group is validated using Jakarta Bean Validation
 * annotations to ensure correct ranges and defaults at startup.
 * </p>
 * 
 * <h2>Example Configuration (YAML)</h2>
 * 
 * <pre>
 * iics:
 *   version: 1.2.3
 *   validation:
 *     enabled: true
 *     cache-enabled: true
 *     cache-expiry-minutes: 120
 *     schema-path: /schemas/iics-process-complete.xsd
 *   parser:
 *     use-streaming-parser: true
 *     large-file-threshold-kb: 50000
 *   cache:
 *     enabled: true
 *     type: memory
 *     max-size: 200
 *   performance:
 *     thread-pool-size: 16
 *     timeout-ms: 60000
 * </pre>
 * 
 * <p>
 * To inject these settings into another bea, use:
 * </p>
 * 
 * <pre>
 * &#64;Autowired
 * private ApplicationProperties properties;
 * </pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "iics")
public class ApplicationProperties {

    /** Application version identifier, used for logging and validation metadata. */
    @NotBlank
    private String version = "1.0.0";

    /** Validation system configuration. */
    private ValidationConfig validation = new ValidationConfig();

    /** Parser subsystem configuration. */
    private ParserConfig parser = new ParserConfig();

    /** Cache subsystem configuration. */
    private CacheConfig cache = new CacheConfig();

    /** Performance tuning configuration. */
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * Validation configuration group.
     * 
     * <p>
     * Controls schema validation and caching behavior for XML and
     * process files.
     * </p>
     */
    @Data
    public static class ValidationConfig {
        /** Enables or disables overall schema validation. */
        private boolean enabled = true;

        /** Enables or disables validation result caching. */
        private boolean cacheEnabled = true;

        /** Time-to-live for cached validation results (in minutes). */
        @Min(1)
        @Max(3600)
        private int cacheExpiryMinutes = 60;

        /** Classpath or filesystem path to the XML schema used for validation. */
        private String schemaPath = "/schemas/iics-process-complete.xsd";
    }

    /**
     * Parser configuration group.
     * 
     * <p>
     * Controls how large XML files are parsed and processed.
     * </p>
     */
    @Data
    public static class ParserConfig {
        /**
         * Whether to use streaming (SAX/StAX) parser insstead of DOM for large files.
         */
        private boolean useStreamingParser = false;

        /** File size threshold (in kb) above which the streaming parser is used. */
        @Min(1000)
        private int largeFileThresholdKb = 10000;
    }

    /**
     * Cache configuration group.
     * 
     * <p>
     * Defines how and where application data is cached.
     * </p>
     */
    @Data
    public static class CacheConfig {
        /** Enables or disables caching globally. */
        private boolean enabled = true;

        /**
         * Type of cache implementation (e.g., {@code memory}, {@code redis},
         * {@code caffeine})
         */
        private String type = "memory";

        /** Maximum number of entries that can be stored in the cache. */
        @Min(1)
        @Max(1000)
        private int maxSize = 100;
}

/**
     * Performance configuration group.
     * 
     * <p>
     * Defines thread pool and timeout settings for parallel operations.
     * </p>
     */
    @Data
    public static class PerformanceConfig {
        /** Size of the interval thread pool used for parallel validation or parsing. */
        @Min(1)
        @Max(100)
        private int threadPoolSize = 10;

        /** Maximum time (in ms) that a task can run before timing out. */
        @Min(1000)
        @Max(3000000)
        private int timeoutMs = 30000;
    }
}
