package com.ppg.iicsdoc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ppg.iicsdoc.api.interceptor.LoggingInterceptor;

/**
 * Configures the application's Spring MVC interceptors.
 *
 * <p>
 * This configuration registers custom request interceptors that are applied
 * to specific URL patterns. Interceptors allow the application to perform
 * cross-cutting concerns such as logging, authentication, or rate limiting
 * before and after controller method execution.
 * </p>
 *
 * <h2>Registered interceptors</h2>
 * <ul>
 * <li>{@link LoggingInterceptor} — logs incoming requests and outgoing
 * responses,
 * including HTTP method, URI, status code, and execution duration.</li>
 * </ul>
 *
 * <h2>Interceptor scope</h2>
 * The {@code LoggingInterceptor} is applied to all endpoints under
 * {@code /api/**}, ensuring all API traffic is monitored and logged without 
 * affecting other routes (e.g., static resources or actuator endpoints).
 *
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * -> GET /api/users from 192.168.0.15
 * <- GET /api/users - Status: 200 - Duration: 42 ms
 * </pre>
 *
 * @see com.example.interceptor.LoggingInterceptor
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see org.springframework.web.servlet.HandlerInterceptor
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    /**
     * Registers application-wide interceptors with the Spring MVC interceptor
     * registry.
     *
     * <p>
     * In this configuration, the {@link LoggingInterceptor} is applied only to
     * request paths matching the pattern {@code /api/**}.
     * </p>
     *
     * @param registry the {@link InterceptorRegistry} used to register interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
