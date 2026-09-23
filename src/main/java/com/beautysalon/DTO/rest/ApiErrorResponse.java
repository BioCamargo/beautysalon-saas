package com.beautysalon.dto.rest;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldValidationError> errors
) {
    public ApiErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    public ApiErrorResponse(int status, String error, String message, String path, List<FieldValidationError> errors) {
        this(status, error, message, path, LocalDateTime.now(), errors);
    }

    public record FieldValidationError(
            String field,
            String message
    ) {}
}
