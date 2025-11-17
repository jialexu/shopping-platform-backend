package com.icc.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update user account information")
public record UpdateRequest(
        
        @Schema(description = "Username", example = "john_doe_updated")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @Schema(description = "Shipping address", example = "456 New St, City, State 67890")
        String shippingAddress,
        
        @Schema(description = "Billing address", example = "456 New St, City, State 67890")
        String billingAddress,
        
        @Schema(description = "Payment method", example = "Credit Card - ending in 5678")
        String paymentMethod
) {}
