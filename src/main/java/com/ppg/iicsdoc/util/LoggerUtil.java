package com.ppg.iicsdoc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for consistent and structured logging
 * across the application.
 * 
 * <p>
 * This class centralizes common logging patterns such as method entry/exit
 * performance metrics, and contextual logging.
 * </p>
 * 
 * <p>
 * All methods are static and designed to be used with SLF4J
 * {@link org.slf4j.Logger} instances.
 * </p>
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
public class LoggerUtil {

    /**
     * Returns a logger instance associated with the specified class.
     * 
     * <p>
     * This is a convenience method for retrieving class-specific loggers.
     * </p>
     * 
     * @param clazz the class for which the logger is to be created
     * @return a logger instance tied to the given class
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Logs the entry point of a method along with its parameters,
     * if debug log is enabled.
     * 
     * <p>
     * This method formats the parameter list and logs a message indicating
     * the method has been entered.
     * </p>
     * 
     * @param logger     the logger to use for output
     * @param methodName the name of the method being entered
     * @param params     the parameters passed to the method
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
     * Logs the exit point of a method along with its return value, if debug
     * log is enabled.
     * 
     * @param logger     the logger to use for output
     * @param methodName the name of the method being used
     * @param result     the result returned from the method
     */
    public static void logMethodExit(
            Logger logger, String methodName, Object result) {
        if (logger.isDebugEnabled()) {
            logger.debug("Exiting method: {} with result: {}", methodName, result);
        }
    }

    /**
     * Logs the duration of an operation in milliseconds.
     * 
     * <p>
     * This method is intended for performance tracking and should be used to
     * log time-sensitive operations.
     * </p>
     * 
     * @param logger     the logger to use for output
     * @param operation  a descriptive name of the operation being measured
     * @param durationMs the duration of the operation in milliseconds
     */
    public static void logPerformance(Logger logger, String operation, long durationMs) {
        logger.info("Performance: {} took {} ms", operation, durationMs);
    }

    /**
     * Log with context (useful for tracking across components)
     * 
     * <p>
     * Logs a message with contextual information prepended.
     * </p>
     * 
     * <p>
     * This is useful for tracing operations across components or threads
     * by tagging logs with a shared context identifier.
     *
     * @param logger  the logger to use for output
     * @param message the message to log
     * @param context a context identifier (e.g., request ID, session ID)
     * @param args    optional arguments to format into the message
     */
    public static void logWithContext(
            Logger logger, String message, String context, Object... args) {
        logger.info("[{}] " + message, mergeArrays(new Object[] { context }, args));
    }

    /**
     * Merges two arrays into a single array.
     * 
     * <p>
     * This method is used internally to combine context and message
     * arguments.
     * </p>
     * 
     * @param first  the first array
     * @param second the second array
     * @return a new array containing all elements from both input arrays
     */
    private static Object[] mergeArrays(Object[] first, Object[] second) {
        Object[] result = new Object[first.length + second.length];

        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);

        return result;
    }
}
