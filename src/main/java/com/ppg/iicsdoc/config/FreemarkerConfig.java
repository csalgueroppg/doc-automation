package com.ppg.iicsdoc.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.TemplateExceptionHandler;

/**
 * Configures the global FreeMarker templating engine used for dynamic
 * content generation such as reports, documentation, or code scaffolding.
 *
 * <p>
 * This configuration defines how FreeMarker templates are loaded,
 * cached, and processed within the application. It customizes template
 * loading behavior, caching strategy, and exception handling to ensure
 * predictable rendering both in development and production environments.
 * </p>
 *
 * <h2>Key features</h2>
 * <ul>
 * <li>Loads templates from the classpath under {@code /templates}</li>
 * <li>Uses UTF-8 as the default character encoding</li>
 * <li>Caches up to 250 templates with MRU (Most Recently Used) eviction</li>
 * <li>Rethrows template exceptions for better debugging</li>
 * <li>Optimized for HTML output with
 * {@link freemarker.core.HTMLOutputFormat}</li>
 * </ul>
 *
 * <h2>Example usage</h2>
 * 
 * <pre>{@code
 * @Autowired
 * private freemarker.template.Configuration freemarkerConfig;
 *
 * Template template = freemarkerConfig.getTemplate("example.ftl");
 * StringWriter writer = new StringWriter();
 * template.process(Map.of("name", "Carlos"), writer);
 * System.out.println(writer);
 * }</pre>
 *
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 * 
 * @see freemarker.template.Configuration
 * @see freemarker.cache.ClassTemplateLoader
 * @see freemarker.template.TemplateExceptionHandler
 */
@Configuration
public class FreemarkerConfig {

    /**
     * Creates and configures a {@link freemarker.template.Configuration} bean
     * used to load and render FreeMarker templates from the application classpath.
     *
     * <p>
     * This setup uses a {@link ClassTemplateLoader} to locate templates under
     * {@code /templates}, with UTF-8 encoding and HTML-safe output formatting.
     * Template caching is enabled with a one-hour update delay and MRU eviction
     * policy.
     * </p>
     *
     * @return the fully configured FreeMarker
     *         {@link freemarker.template.Configuration}
     * @throws IOException if the template loader fails to initialize
     */
    @Bean
    freemarker.template.Configuration freemarkerConfiguration() throws IOException {
        freemarker.template.Configuration config = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_32);

        config.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates"));
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setTemplateUpdateDelayMilliseconds(3600000);
        config.setCacheStorage(new freemarker.cache.MruCacheStorage(50, 250));
        config.setWhitespaceStripping(true);
        config.setOutputFormat(freemarker.core.HTMLOutputFormat.INSTANCE);
        config.setAutoFlush(false);

        return config;
    }
}
