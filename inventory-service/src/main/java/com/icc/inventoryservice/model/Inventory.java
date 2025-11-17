package com.icc.inventoryservice.model;

import java.time.LocalDateTime;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("inventory_by_sku")
public class Inventory {
    
    @PrimaryKey
    private String sku;
    
    @Column("available")
    private Integer available;
    
    @Column("reserved")
    private Integer reserved;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    public Inventory() {}
    
    public Inventory(String sku, Integer available, Integer reserved, LocalDateTime updatedAt) {
        this.sku = sku;
        this.available = available;
        this.reserved = reserved;
        this.updatedAt = updatedAt;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getTotal() {
        return available + reserved;
    }
}