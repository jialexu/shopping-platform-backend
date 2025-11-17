package com.icc.itemservice.dto;

import java.util.List;
import java.util.Map;

public class ItemUpdateRequest {
    
    private String name;
    
    private Double price;
    
    private List<String> pictures;
    
    private Map<String, Object> attributes;
    
    public ItemUpdateRequest() {}
    
    public ItemUpdateRequest(String name, Double price, List<String> pictures, Map<String, Object> attributes) {
        this.name = name;
        this.price = price;
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