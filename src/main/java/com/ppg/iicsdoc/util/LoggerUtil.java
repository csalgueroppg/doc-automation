package com.ppg.iicsdoc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for consistent logging across the application
 */
public class LoggerUtil {

    /**
     * Get logger for a specific class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Log method with entry parameter
     */
    public static void logMethodEntry(
            Logger logger, String methodName, Object... params) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Entering method: ")
                .append(methodName)
                .append("(");

            for (int i = 0; i < params.length; i++) {
                sb.append(params[i]);
                if (i < params.length - 1) {
                    sb.append(", ");
                }
            }

            sb.append(")");
            logger.debug(sb.toString());
        }
    }

    /**
     * Log method exit with result
     */
    public static void logMethodExit(
        Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {} with result: {}", methodName, result);
        }
    }

    /**
     * Log performance metric
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("Performance: {} took {} ms", operation, durationMs);
    }

    /**
     * Log with context (useful for tracking across components)
     */
    public static void logWithContext(
        Logger logger, String message, String context, Object... args) {
        logger.info("[{}] " + message, mergeArrays(new Object[]{context}, args));
    }

    private static Object[] mergeArrays(Object[] first, Object[] second) {
        Object[] result = new Object[first.length + second.length];

        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }
}
