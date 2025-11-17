package com.icc.itemservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "items")
public class Item {
    
    @Id
    private String id;
    
    private String name;
    
    private Double price;
    
    @Indexed(unique = true)
    private String upc;
    
    private List<String> pictures;
    
    private Map<String, Object> attributes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    public Item() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Item(String name, Double price, String upc, List<String> pictures, Map<String, Object> attributes) {
        this();
        this.name = name;
        this.price = price;
        this.upc = upc;
        this.pictures = pictures;
        this.attributes = attributes;
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