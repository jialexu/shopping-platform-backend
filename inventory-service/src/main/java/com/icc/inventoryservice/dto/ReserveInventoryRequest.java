package com.icc.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ReserveInventoryRequest {
    
    @NotBlank(message = "SKU cannot be blank")
    private String sku;
    
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;
    
    public ReserveInventoryRequest() {}
    
    public ReserveInventoryRequest(String sku, Integer quantity, String orderId) {
        this.sku = sku;
        this.quantity = quantity;
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}