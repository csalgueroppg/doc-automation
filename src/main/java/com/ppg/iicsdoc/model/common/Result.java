package com.ppg.iicsdoc.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic wrapper for representing the outcome of the application.
 * 
 * <p>
 * This class encapsulates both success and failure results in a unified
 * structure making it suitable for service-layer responses, controller
 * outputs, or any operation that may return data along with status and
 * error context.
 * </p>
 * 
 * @param <T> The type of the result data
 * 
 * @author Carlos Salguero
 * @version 1.0.0
 * @since 2025-10-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** Indicates whether the operation was successful. */
    private boolean success;

    /**
     * The result data returned by the operation, if successful.
     * May be {@code null} in failure scenarios.
     */
    private T data;

    /** A human-readable message describing the result or error. */
    private String message;

    /** An optional error code for categorizing failure types. */
    private String errorCode;

    /**
     * Creates a successful result with the given data.
     *
     * @param data the result payload
     * @param <T>  the type of the result payload
     * @return a {@code Result} instance representing success
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null);
    }

    /**
     * Creates a successful result with the given data and message.
     *
     * @param data    the result payload
     * @param message a descriptive message
     * @param <T>     the type of the result payload
     * @return a {@code Result} instance representing success
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message, null);
    }

    /**
     * Creates a failure result with the given message.
     *
     * @param message a descriptive error message
     * @param <T>     the type of the result payload
     * @return a {@code Result} instance representing failure
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, null, message, null);
    }

    /**
     * Creates a failure result with the given message and error code.
     *
     * @param message   a descriptive error message
     * @param errorCode an optional error code for categorization
     * @param <T>       the type of the result payload
     * @return a {@code Result} instance representing failure
     */
    public static <T> Result<T> failure(String message, String errorCode) {
        return new Result<>(false, null, message, errorCode);
    }

    /**
     * Returns {@code true} if the result represents a failure.
     *
     * @return {@code true} if {@code success} is {@code false}, otherwise
     *         {@code false}
     */
    public boolean isFailure() {
        return !success;
    }

}
