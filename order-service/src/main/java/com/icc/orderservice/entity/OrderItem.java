package com.icc.orderservice.entity;

import java.math.BigDecimal;

public class OrderItem {
    private String orderId;
    private String sku;
    private Integer quantity;
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(String orderId, String sku, Integer quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}