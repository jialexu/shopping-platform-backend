package com.icc.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderItemRequest {
    @NotBlank(message = "SKU cannot be blank")
    private String sku;
    
    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public CreateOrderItemRequest() {}

    public CreateOrderItemRequest(String sku, Integer quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}