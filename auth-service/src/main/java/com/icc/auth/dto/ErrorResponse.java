package com.icc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response")
public record ErrorResponse(
        @Schema(description = "Error message")
        String message,
        
        @Schema(description = "HTTP status code")
        int status,
        
        @Schema(description = "Timestamp")
        long timestamp
) {
    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status, System.currentTimeMillis());
    }
}
