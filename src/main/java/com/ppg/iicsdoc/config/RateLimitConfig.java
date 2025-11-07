package com.ppg.iicsdoc.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Configures a global {@link Bucket} instance used for API rate limiting.
 *
 * <p>
 * This configuration defines how many requests a client is allowed to perform
 * within a fixed time window using the {@link io.github.bucket4j.Bucket4j}
 * library. The rate limiter works as a token bucket: each request consumes one
 * token, and tokens are refilled periodically according to the defined
 * bandwidth rules.
 * </p>
 *
 * <h2>Default policy</h2>
 * <ul>
 * <li>Maximum capacity: 100 tokens</li>
 * <li>Refill rate: 100 tokens every 1 minute</li>
 * <li>One token is consumed per incoming request</li>
 * </ul>
 *
 * <p>
 * This configuration supports integration with servlet filters or interceptors,
 * such as {@code RateLimitFilter}, to enforce rate limits per client, user, or
 * API key.
 * </p>
 *
 * <h2>Example usage</h2>
 * 
 * <pre>{@code
 * @Autowired
 * private Bucket rateLimiterBucket;
 *
 * if (rateLimiterBucket.tryConsume(1)) {
 *     // Proceed with request
 * } else {
 *     // Reject request due to rate limit
 * }
 * }</pre>
 *
 * @see io.github.bucket4j.Bandwidth
 * @see io.github.bucket4j.Refill
 * @see io.github.bucket4j.Bucket
 * @see com.example.filter.RateLimitFilter
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Configuration
public class RateLimitConfig {

    /**
     * Creates and configures a {@link Bucket} bean that defines the application's
     * request rate-limiting policy.
     *
     * <p>
     * The configured bucket allows up to 100 requests per minute. Each incoming
     * request consumes one token, and once the bucket is empty, further requests
     * are rejected until the next refill period.
     * </p>
     *
     * @return a configured {@link Bucket} instance representing the global rate
     *         limiter
     */
    @Bean
    public Bucket rateLimiterBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
