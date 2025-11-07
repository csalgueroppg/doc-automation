package com.ppg.iicsdoc.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for collecting, aggregating, and reporting performance
 * metrics for operations executed within the application.
 * 
 * <p>
 * This service provides lightweight in-memory tracking of execution durations
 * using a {@link java.util.concurrent.ConcurrentHashMap}, enabling developers
 * to measure average, minimum, and maximum durations across multiple runs of
 * identified operations.
 * </p>
 * 
 * <p>
 * Metrics are tracked per operation name, and can be recorded explicitly via
 * {@link #record(String, Duration) or automatically measured using
 * {@link #time(String, Supplier)}}.
 * </p>
 * 
 * <p>
 * The generated statistics are aggregated and can be exported in a
 * Markdown-formatted report via {@link #getStatsReport()} for use in logs,
 * dashboards, or performance summaries.
 * </p>
 * 
 * <3>Example Usage</h3>
 * 
 * <pre>{@code
 * @Autowired
 * private PerformanceMetricsService metrics;
 *
 * public void processFile(String filePath) {
 *     metrics.time("fileProcessing", () -> {
 *         // Execute task
 *         readFile(filePath);
 *         validateData();
 *         transformAndSave();
 *         return null;
 *     });
 * }
 * }</pre>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-11-07
 */
@Slf4j
@Service
public class PerformanceMetricsService {

    /** In-memory map storing performance metrics per operation name. */
    private final ConcurrentHashMap<String, MetricStats> metrics = new ConcurrentHashMap<>();

    /**
     * Represents the aggregated performance statistics for a specific operation.
     * 
     * <p>
     * Contains counters and duration summaries used to compute average,
     * minimum, and maximum execution times.
     * </p>
     */
    @Data
    @AllArgsConstructor
    public static class MetricStats {
        private AtomicLong count;
        private AtomicLong totalDurationMs;
        private AtomicLong minDurationMs;
        private AtomicLong maxDurationMs;
    }

    /**
     * Records the duration of a completed operation.
     *
     * @param operation the operation identifier (e.g. "dataValidation", "apiCall")
     * @param duration  the {@link Duration} representing how long the operation
     *                  took
     */
    public void record(String operation, Duration duration) {
        long durationMs = duration.toMillis();
        metrics.computeIfAbsent(operation, k -> new MetricStats(
                new AtomicLong(0),
                new AtomicLong(0),
                new AtomicLong(Long.MAX_VALUE),
                new AtomicLong(0)));

        MetricStats stats = metrics.get(operation);
        stats.getCount().incrementAndGet();
        stats.getTotalDurationMs().addAndGet(durationMs);
        stats.getMinDurationMs().updateAndGet(min -> Math.min(min, durationMs));
        stats.getMaxDurationMs().updateAndGet(max -> Math.max(max, durationMs));
    }

    /**
     * Executes the given {@link Supplier} while automatically measuring and
     * recording
     * its execution time under the specified operation name.
     *
     * @param <T>       the type of result returned by the operation
     * @param operation the name of the operation (used as a metric key)
     * @param supplier  the code block or lambda expression to execute and measure
     * @return the result returned by the supplied operation
     */
    public <T> T time(String operation, Supplier<T> supplier) {
        Instant start = Instant.now();
        try {
            return supplier.get();
        } finally {
            record(operation, Duration.between(start, Instant.now()));
        }
    }

    /**
     * Generates a Markdown-formatted report containing the aggregated
     * performance metrics for all recorded operations.
     *
     * <p>
     * The report includes operation name, invocation count, average,
     * minimum, and maximum duration (in milliseconds).
     * </p>
     *
     * @return a formatted Markdown report as a {@link String}
     */
    public String getStatsReport() {
        StringBuilder report = new StringBuilder();

        report.append("# Performance Metrics\n\n");
        report.append("| Operation | Count | Avg (ms) | Min (ms) | Max (ms) |\n");
        report.append("|-----------|-------|----------|----------|----------|\n");

        metrics.forEach((operation, stats) -> {
            long avg = stats.getTotalDurationMs().get() / stats.getCount().get();

            report.append(String.format("| %s | %d | %d | %d | %d |\n",
                    operation,
                    stats.getCount().get(),
                    avg,
                    stats.getMinDurationMs().get(),
                    stats.getMaxDurationMs().get()));
        });

        return report.toString();
    }

    /**
     * Clears all collected performance metrics from memory.
     * 
     * <p>
     * Useful for resetting the metrics between tests or after reporting intervals.
     * </p>
     */
    public void clear() {
        metrics.clear();
    }
}
