package com.icc.orderservice.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class CreateOrderRequest {
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<CreateOrderItemRequest> items;
    
    @NotBlank(message = "Shipping address cannot be blank")
    private String shippingAddress;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String userId, List<CreateOrderItemRequest> items, String shippingAddress) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CreateOrderItemRequest> getItems() { return items; }
    public void setItems(List<CreateOrderItemRequest> items) { this.items = items; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
}