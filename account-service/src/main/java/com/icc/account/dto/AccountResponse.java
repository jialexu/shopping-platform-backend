package com.icc.account.dto;

import com.icc.account.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User account response")
public record AccountResponse(
        
        @Schema(description = "User ID", example = "1")
        Long id,
        
        @Schema(description = "User email", example = "user@example.com")
        String email,
        
        @Schema(description = "Username", example = "john_doe")
        String username,
        
        @Schema(description = "Shipping address", example = "123 Main St, City, State 12345")
        String shippingAddress,
        
        @Schema(description = "Billing address", example = "123 Main St, City, State 12345")
        String billingAddress,
        
        @Schema(description = "Payment method", example = "Credit Card")
        String paymentMethod
) {
    public static AccountResponse from(User user) {
        return new AccountResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getShippingAddress(),
                user.getBillingAddress(),
                user.getPaymentMethod()
        );
    }
}
