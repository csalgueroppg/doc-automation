package com.ppg.iicsdoc.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures HTTP response compression for the embedded Tomcat server.
 *
 * <p>
 * This configuration ensures that responses such as JSON, XML, and text-based
 * payloads are automatically compressed using GZIP when supported by the
 * client.
 * </p>
 * 
 * <p>
 * Enabling compression helps reduce network bandwidth and improve performance
 * for large responses or high-traffic environments.
 * </p>
 *
 * <h2>Example HTTP header exchange</h2>
 * 
 * <pre>
 *   Request:
 *     Accept-Encoding: gzip, deflate
 *
 *   Response:
 *     Content-Encoding: gzip
 * </pre>
 *
 * <h2>Default behavior:</h2>
 * <ul>
 * <li>Compression is enabled globally for all responses.</li>
 * <li>Compression type: {@code gzip}</li>
 * <li>Applies automatically to {@code text/*}, {@code application/json}, and
 * {@code application/xml} content types.</li>
 * </ul>
 *
 * <p>
 * For additional tuning, you can set the following in
 * {@code application.yml} or {@code application.properties}:
 * 
 * <pre>
 * server.compression.enabled=true
 * server.compression.min-response-size=1024
 * server.compression.mime-types=text/html,text/xml,text/plain,application/json
 * </pre>
 *
 * @see org.springframework.boot.web.server.Compression
 * @see org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
public class CompressionConfig {

    /**
     * Customizes the embedded Tomcat server to enable HTTP response compression.
     *
     * <p>
     * This bean programmatically activates Spring Boot’s built-in
     * {@link Compression} support and attaches it to the
     * {@link TomcatServletWebServerFactory}.
     * </p>
     *
     * @return a {@link WebServerFactoryCustomizer} that enables GZIP compression
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> compressionCustomizer() {
        return factory -> {
            Compression compression = new Compression();

            compression.setEnabled(true);
            factory.setCompression(compression);
        };
    }
}
