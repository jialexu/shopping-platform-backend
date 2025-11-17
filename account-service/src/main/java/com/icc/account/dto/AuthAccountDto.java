package com.icc.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Internal response with user authentication details")
public record AuthAccountDto(
        
        @Schema(description = "User ID", example = "1")
        Long id,
        
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @Schema(description = "Hashed password")
        String passwordHash
) {}
