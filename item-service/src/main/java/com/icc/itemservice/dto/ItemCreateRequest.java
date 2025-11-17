package com.icc.itemservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Map;

public class ItemCreateRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    @NotBlank(message = "UPC is required")
    private String upc;
    
    private List<String> pictures;
    
    private Map<String, Object> attributes;
    
    public ItemCreateRequest() {}
    
    public ItemCreateRequest(String name, Double price, String upc, List<String> pictures, Map<String, Object> attributes) {
        this.name = name;
        this.price = price;
        this.upc = upc;
        this.pictures = pictures;
        this.attributes = attributes;
    }
    
    // Getters and setters
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
}