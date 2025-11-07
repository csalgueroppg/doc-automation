package com.ppg.iicsdoc.api.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link HandlerInterceptor} implementation that logs details about incoming
 * HTTP requests and their corresponding responses.
 * 
 * <p>
 * This interceptor logs both:
 * </p>
 * 
 * <ul>
 * <li>the start of each request (HTTP method, URI, and client IP address),
 * and</li>
 * <li>the completion of the request (status code and total execution time in
 * ms)</li>
 * 
 * <p>
 * It helps in tracking request performance, identifying slow endpoints, and
 * troubleshooting client issues during API execution.
 * </p>
 * 
 * <h2>Example Log Output</h2>
 * 
 * <pre>
 * -> GET /api/v3/customers from 192.168.1.5
 * <- GET /api/v3/customers - Status: 200 - Duration: 45 ms
 * </pre>
 * 
 * <p>
 * If a request results in an exception, it is logged at the <b>ERROR</b> level.
 * </p>
 * 
 * <p>
 * This component is automatically detected and registered by Spring due to
 * the {@link Component} annotation.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    /**
     * Called before the target controller method is invoked.
     * 
     * <p>
     * This method records the request start time and logs the HTTP method, URI, and
     * client IP.
     * </p>
     *
     * @param request  the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @param handler  the chosen handler to execute
     * 
     * @return {@code true} to continue the execution chain; {@code false} to abort
     *         request handling
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        log.info("-> {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        return true;
    }

    /**
     * Called after the complete request has finished processing.
     * 
     * <p>
     * Logs the response status code and total request execution duration.
     * If an exception was thrown during processing, it is logged at the error
     * level.
     * </p>
     *
     * @param request  the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @param handler  the handler that was executed
     * @param ex       any exception thrown on handler execution, or {@code null} if
     *                 none occurred
     */
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        log.info("<- {} {} - Status: {} - Duration: {} ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        if (ex != null) {
            log.error("Request failed with exception", ex);
        }
    }
}
