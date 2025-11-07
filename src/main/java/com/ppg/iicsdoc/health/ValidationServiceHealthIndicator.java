package com.ppg.iicsdoc.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.ppg.iicsdoc.config.ApplicationProperties;

import lombok.RequiredArgsConstructor;

/**
 * Custom {@link HealthIndicator} that reports the operational status of
 * the application's validation service.
 * 
 * <p>
 * This component integrates with Spring Boot Actuator to expose validation
 * health information via the {@code /actuator/health} endpoint. It
 * provides a lightweight way to check the validation logic or supporting
 * dependencies are functioning as expected.
 * </p>
 * 
 * <h2>Reported Health Details</h2>
 * <ul>
 * <li>{@code status: "UP"} — when the validation subsystem is operational</li>
 * <li>{@code status: "DOWN"} — if an exception occurs during validation
 * check</li>
 * <li>{@code details.validation} — textual indicator ("operational" or error
 * info)</li>
 * </ul>
 * 
 * <h2>Example Response from Actuator</h2>
 * <pre>
 * {
 *   "status": "UP",
 *   "components": {
 *     "validationService": {
 *       "status": "UP",
 *       "details": {
 *         "validation": "operational"
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * @see org.springframework.boot.actuate.health.HealthIndicator
 * @see org.springframework.boot.actuate.health.Health
 * @see org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Component
@RequiredArgsConstructor
public class ValidationServiceHealthIndicator implements HealthIndicator {

    private final ApplicationProperties applicationProperties;

    /**
     * Performs a simple validation subsystem health check.
     *
     * <p>
     * This implementation always returns {@link Health#up()} unless
     * an exception occurs during the check, in which case it returns
     * {@link Health#down()} with the exception message included in the
     * health details.
     * </p>
     *
     * @return a {@link Health} object representing the current health status
     */
    @Override
    public Health health() {
        try {
            if (!applicationProperties.getValidation().isEnabled()) {
                return Health.down()
                    .withDetail("validation", "disabled via configuration")
                    .build();
            }

            String schemaPath = applicationProperties.getValidation().getSchemaPath();
            return Health.up()
                    .withDetail("validation", "operational")
                    .withDetail("schemaPath", schemaPath)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
