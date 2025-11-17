package com.icc.orderservice.dto;

import com.icc.orderservice.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateOrderRequest {
    @NotNull(message = "Status cannot be null")
    private OrderStatus status;
    
    private String shippingAddress;

    public UpdateOrderRequest() {}

    public UpdateOrderRequest(OrderStatus status, String shippingAddress) {
        this.status = status;
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}