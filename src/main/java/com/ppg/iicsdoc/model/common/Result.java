package com.ppg.iicsdoc.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic result wrapper for operations
 * 
 * @param <T> The type of the result data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private boolean success;
    private T data;
    private String message;
    private String errorCode;

    public static <T> Result<T> success(T data) {
        return new Result<T>(true, data, null, null);
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<T>(true, data, message, null);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<T>(false, null, message, null);
    }

    public static <T> Result<T> failure(String message, String errorCode) {
        return new Result<T>(false, null, message, errorCode);
    }
    
    public boolean isFailure() {
        return !success;
    }
}
