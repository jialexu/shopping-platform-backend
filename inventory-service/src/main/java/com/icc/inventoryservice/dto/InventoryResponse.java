package com.icc.inventoryservice.dto;

public class InventoryResponse {
    
    private String sku;
    private Integer available;
    private Integer reserved;
    private Integer total;
    
    public InventoryResponse() {}
    
    public InventoryResponse(String sku, Integer available, Integer reserved, Integer total) {
        this.sku = sku;
        this.available = available;
        this.reserved = reserved;
        this.total = total;
    }
    
    // Getters and Setters
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public Integer getAvailable() {
        return available;
    }
    
    public void setAvailable(Integer available) {
        this.available = available;
    }
    
    public Integer getReserved() {
        return reserved;
    }
    
    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
}