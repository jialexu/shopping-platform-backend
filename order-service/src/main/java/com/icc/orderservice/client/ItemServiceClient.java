package com.icc.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.icc.orderservice.dto.ItemResponse;

@FeignClient(name = "item-service", url = "${app.services.item-service.url:http://item-service:9002}")
public interface ItemServiceClient {
    
    @GetMapping("/api/items/{id}")
    ItemResponse getItem(@PathVariable("id") String id);
    
    @GetMapping("/api/items/by-upc/{upc}")
    ItemResponse getItemByUpc(@PathVariable("upc") String upc);
}