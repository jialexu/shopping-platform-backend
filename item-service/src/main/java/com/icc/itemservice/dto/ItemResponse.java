package com.icc.itemservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ItemResponse {
    
    private String id;
    
    private String name;
    
    private Double price;
    
    private String upc;
    
    private List<String> pictures;
    
    private Map<String, Object> attributes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public ItemResponse() {}
    
    public ItemResponse(String id, String name, Double price, String upc, 
                       List<String> pictures, Map<String, Object> attributes,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.upc = upc;
        this.pictures = pictures;
        this.attributes = attributes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public String getUpc() {
        return upc;
    }
    
    public void setUpc(String upc) {
        this.upc = upc;
    }
    
    public List<String> getPictures() {
        return pictures;
    }
    
    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }
    
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}