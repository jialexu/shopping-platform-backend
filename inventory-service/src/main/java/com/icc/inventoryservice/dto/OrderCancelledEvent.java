package com.icc.inventoryservice.dto;

public class OrderCancelledEvent {
    
    private String orderId;
    private String sku;
    private Integer quantity;
    
    public OrderCancelledEvent() {}
    
    public OrderCancelledEvent(String orderId, String sku, Integer quantity) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
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
}