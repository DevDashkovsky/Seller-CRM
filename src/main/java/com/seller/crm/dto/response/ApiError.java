package com.seller.crm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldError> fieldErrors
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path,
            null);
    }

    public static ApiError ofValidation(String path, List<FieldError> fieldErrors) {
        return new ApiError(
            LocalDateTime.now(),
            400,
            "Bad Request",
            "Validation failed",
            path,
            fieldErrors
        );
    }
}
