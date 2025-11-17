package com.icc.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token response")
public record TokenResponse(
        @Schema(description = "JWT access token")
        String token
) {}
