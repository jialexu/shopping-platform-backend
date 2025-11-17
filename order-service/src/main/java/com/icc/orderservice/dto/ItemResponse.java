package com.icc.orderservice.dto;

import java.math.BigDecimal;

public class ItemResponse {
    private String id;
    private String name;
    private BigDecimal price;
    private String upc;

    public ItemResponse() {}

    public ItemResponse(String id, String name, BigDecimal price, String upc) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.upc = upc;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }
}