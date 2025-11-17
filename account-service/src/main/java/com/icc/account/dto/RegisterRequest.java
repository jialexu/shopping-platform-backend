package com.icc.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new user account")
public record RegisterRequest(
        
        @Schema(description = "User email address", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        
        @Schema(description = "Username", example = "john_doe")
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @Schema(description = "Password", example = "SecurePass123!")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,
        
        @Schema(description = "Shipping address", example = "123 Main St, City, State 12345")
        String shippingAddress,
        
        @Schema(description = "Billing address", example = "123 Main St, City, State 12345")
        String billingAddress,
        
        @Schema(description = "Payment method", example = "Credit Card - ending in 1234")
        String paymentMethod
) {}
