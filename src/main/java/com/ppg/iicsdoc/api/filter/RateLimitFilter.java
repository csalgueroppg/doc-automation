package com.ppg.iicsdoc.api.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

/**
 * {@link Filter} implementation that enforces a rate limit on incoming HTTP
 * requests.
 * 
 * <p>
 * This filter uses a {@link Bucket} from the Bucket4j library to track token
 * consumption and determine whether a request should be allowed or rejected
 * based on the configured rate.
 * </p>
 * 
 * <p>
 * It sets the following HTTP headers for responses:
 * </p>
 * 
 * <ul>
 * <li><b>X-Rate-Limit-Remaining</b> - the number of tokens left in the bucket
 * after consuming this request</li>
 * <li><b>X-Rate-Limit-Retry-After-Seconds</b> - the number of seconds until the
 * token is available if the request is rejected</li>
 * </ul>
 * 
 * <p>
 * If a request exceeds the rate limit, the response status is set to
 * {@link HttpStatus#TOO_MANY_REQUESTS}.
 * </p>
 * 
 * <p>
 * Example Usage
 * </p>
 * 
 * <pre>{@code
 * &#64;Bean
 * public Bucket rateLimiterBucket() {
 *     Refill refill = Refill.intervaly(10, Duration.ofSeconds(1));
 *     Bandwidth limit = Bandwitdh.classic(10, refill);
 * 
 *     return Bucket.builder()
 *             .addLimit(limit)
 *             .build(build);
 * }
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Component
public class RateLimitFilter implements Filter {

    /**
     * Bucket4j rate limiter bucket. This bucket defines the allowed rate
     * and refill strategy. Is is injected by Spring.
     */
    @Autowired
    private Bucket rateLimiterBucket;

    /**
     * Filters incoming requests and enforces rate limiting.
     * 
     * <p>
     * If the request can be consumed from the bucket, it proceeds down the filter
     * chain. Otherwise, it responds with HTTP 429 (Too Many Requests) and sets 
     * the retry header.
     * </p>
     *
     * @param request  the incoming {@link ServletRequest}, expected to be an
     *                 {@link HttpServletRequest}
     * @param response the outgoing {@link ServletResponse}, expected to be an
     *                 {@link HttpServletResponse}
     * @param chain    the {@link FilterChain} to pass the request/response to the
     *                 next filter
     * @throws IOException      if an I/O error occurs while writing the response
     * @throws ServletException if the request cannot be processed
     */
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        ConsumptionProbe probe = rateLimiterBucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            httpResponse.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.addHeader("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

            httpResponse.getWriter().write("Rate limit exceeded. Please try again later");
        }
    }
}
