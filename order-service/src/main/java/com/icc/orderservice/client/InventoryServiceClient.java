package com.icc.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.icc.orderservice.dto.InventoryRequest;

@FeignClient(name = "inventory-service", url = "${app.services.inventory-service.url:http://inventory-service:9003}")
public interface InventoryServiceClient {
    
    @GetMapping("/api/inventory/{sku}")
    InventoryResponse getInventory(@PathVariable("sku") String sku);
    
    @PostMapping("/api/inventory/reserve")
    Boolean reserveInventory(@RequestBody InventoryRequest request);
    
    @PostMapping("/api/inventory/release")
    Boolean releaseInventory(@RequestBody InventoryRequest request);
    
    class InventoryResponse {
        private String sku;
        private Integer available;
        private Integer reserved;
        
        public InventoryResponse() {}
        
        public InventoryResponse(String sku, Integer available, Integer reserved) {
            this.sku = sku;
            this.available = available;
            this.reserved = reserved;
        }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Integer getAvailable() { return available; }
        public void setAvailable(Integer available) { this.available = available; }
        
        public Integer getReserved() { return reserved; }
        public void setReserved(Integer reserved) { this.reserved = reserved; }
    }
}