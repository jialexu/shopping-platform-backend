package com.icc.orderservice.dto;

public class InventoryRequest {
    private String sku;
    private Integer quantity;
    private String orderId;

    public InventoryRequest() {}

    public InventoryRequest(String sku, Integer quantity, String orderId) {
        this.sku = sku;
        this.quantity = quantity;
        this.orderId = orderId;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}