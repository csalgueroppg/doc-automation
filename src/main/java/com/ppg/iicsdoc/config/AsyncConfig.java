package com.ppg.iicsdoc.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures asynchronous task execution for the application.
 * 
 * <p>
 * This configuration enables parallel processing of components such as the
 * parser and AI agent by providing a custom {@link Executor} bean. The
 * executor is backed by a {@link ThreadPoolTaskExecutor} with a fixed-size
 * thread pool and bounded queue.
 * </p>
 * 
 * <p>
 * Logging is enabled to confirm initialization parameters at startup.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Returns a custom {@link Executor} for handling asynchronous method
     * execution.
     * 
     * <p>
     * The executor is configured with:
     * </p>
     * <ul>
     * <li>Core pool size: 4 threads</li>
     * <li>Maximum pool size: 8 threads</li>
     * <li>Queue capacity: 50 tasks</li>
     * <li>Thread name prefix: {@code iics-async}</li>
     * <li>Graceful shutdown with 60-second timeout</li>
     * </ul>
     * 
     * <p>
     * This setup ensures responsive parallelism while maintaining control
     * over resource usage.
     * </p>
     * 
     * @return a configured {@link Executor} instance for async processing.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("iics-async");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Async executor initialized with core pool size: {}, max pool size: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize());

        return executor;
    }
}
