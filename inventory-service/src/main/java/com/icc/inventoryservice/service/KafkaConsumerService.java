package com.icc.inventoryservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icc.inventoryservice.dto.OrderCancelledEvent;
import com.icc.inventoryservice.dto.ReleaseInventoryRequest;

@Service
public class KafkaConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order.cancelled", groupId = "inventory-service-group")
    public void handleOrderCancelled(String message) {
        logger.info("Received order cancelled event: {}", message);
        
        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            
            // Release reserved inventory
            ReleaseInventoryRequest releaseRequest = new ReleaseInventoryRequest(
                event.getSku(),
                event.getQuantity(),
                event.getOrderId()
            );
            
            boolean released = inventoryService.releaseInventory(releaseRequest);
            
            if (released) {
                logger.info("Successfully released inventory for cancelled order: {}", event.getOrderId());
            } else {
                logger.error("Failed to release inventory for cancelled order: {}", event.getOrderId());
            }
            
        } catch (JsonProcessingException e) {
            logger.error("Error processing order cancelled event: {}", e.getMessage(), e);
        }
    }
}