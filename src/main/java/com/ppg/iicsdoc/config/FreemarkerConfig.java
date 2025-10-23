package com.ppg.iicsdoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import freemarker.template.TemplateExceptionHandler;

/**
 * Configuration for {@link freemarker.template.Configuration}.
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-23
 */
@Configuration
public class FreemarkerConfig {
   
    @Bean
    public freemarker.template.Configuration freemarkerConfiguration() {
        freemarker.template.Configuration config = new freemarker.template.Configuration(
            freemarker.template.Configuration.VERSION_2_3_32
        );

        config.setClassForTemplateLoading(this.getClass(), "/templates");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setFallbackOnNullLoopVariable(false);
        
        return config;
    }
}
