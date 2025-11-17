package com.icc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request")
public record LoginRequest(
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @Schema(description = "User password", example = "password123")
        String password
) {}
