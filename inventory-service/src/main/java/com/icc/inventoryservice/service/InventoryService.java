package com.icc.inventoryservice.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icc.inventoryservice.dto.InventoryResponse;
import com.icc.inventoryservice.dto.ReleaseInventoryRequest;
import com.icc.inventoryservice.dto.ReserveInventoryRequest;
import com.icc.inventoryservice.model.Inventory;
import com.icc.inventoryservice.model.InventoryEvent;
import com.icc.inventoryservice.repository.InventoryEventRepository;
import com.icc.inventoryservice.repository.InventoryRepository;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private InventoryEventRepository inventoryEventRepository;
    
    public InventoryResponse getInventory(String sku) {
        logger.info("Getting inventory for SKU: {}", sku);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySku(sku);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            return new InventoryResponse(
                inventory.getSku(),
                inventory.getAvailable(),
                inventory.getReserved(),
                inventory.getTotal()
            );
        } else {
            logger.warn("Inventory not found for SKU: {}", sku);
            return new InventoryResponse(sku, 0, 0, 0);
        }
    }
    
    @Transactional
    public boolean reserveInventory(ReserveInventoryRequest request) {
        logger.info("Reserving {} units for SKU: {}, Order: {}", 
                   request.getQuantity(), request.getSku(), request.getOrderId());
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySku(request.getSku());
        
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            
            if (inventory.getAvailable() >= request.getQuantity()) {
                // Reserve inventory
                inventory.setAvailable(inventory.getAvailable() - request.getQuantity());
                inventory.setReserved(inventory.getReserved() + request.getQuantity());
                inventory.setUpdatedAt(LocalDateTime.now());
                
                inventoryRepository.save(inventory);
                
                // Log event
                InventoryEvent event = new InventoryEvent(
                    request.getSku(), 
                    -request.getQuantity(), 
                    request.getOrderId(), 
                    "RESERVE"
                );
                inventoryEventRepository.save(event);
                
                logger.info("Successfully reserved {} units for SKU: {}", 
                           request.getQuantity(), request.getSku());
                return true;
            } else {
                logger.warn("Insufficient inventory for SKU: {}. Available: {}, Requested: {}", 
                           request.getSku(), inventory.getAvailable(), request.getQuantity());
                return false;
            }
        } else {
            logger.warn("Inventory not found for SKU: {}", request.getSku());
            return false;
        }
    }
    
    @Transactional
    public boolean releaseInventory(ReleaseInventoryRequest request) {
        logger.info("Releasing {} units for SKU: {}, Order: {}", 
                   request.getQuantity(), request.getSku(), request.getOrderId());
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySku(request.getSku());
        
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            
            if (inventory.getReserved() >= request.getQuantity()) {
                // Release inventory
                inventory.setReserved(inventory.getReserved() - request.getQuantity());
                inventory.setAvailable(inventory.getAvailable() + request.getQuantity());
                inventory.setUpdatedAt(LocalDateTime.now());
                
                inventoryRepository.save(inventory);
                
                // Log event
                InventoryEvent event = new InventoryEvent(
                    request.getSku(), 
                    request.getQuantity(), 
                    request.getOrderId(), 
                    "RELEASE"
                );
                inventoryEventRepository.save(event);
                
                logger.info("Successfully released {} units for SKU: {}", 
                           request.getQuantity(), request.getSku());
                return true;
            } else {
                logger.warn("Insufficient reserved inventory for SKU: {}. Reserved: {}, Requested: {}", 
                           request.getSku(), inventory.getReserved(), request.getQuantity());
                return false;
            }
        } else {
            logger.warn("Inventory not found for SKU: {}", request.getSku());
            return false;
        }
    }
    
    public void createInitialInventory(String sku, Integer quantity) {
        logger.info("Creating initial inventory for SKU: {} with quantity: {}", sku, quantity);
        
        Inventory inventory = new Inventory(sku, quantity, 0, LocalDateTime.now());
        inventoryRepository.save(inventory);
        
        // Log event
        InventoryEvent event = new InventoryEvent(sku, quantity, null, "INITIAL");
        inventoryEventRepository.save(event);
        
        logger.info("Initial inventory created for SKU: {}", sku);
    }
}