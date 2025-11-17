package com.icc.inventoryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.icc.inventoryservice.dto.InventoryResponse;
import com.icc.inventoryservice.dto.ReleaseInventoryRequest;
import com.icc.inventoryservice.dto.ReserveInventoryRequest;
import com.icc.inventoryservice.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/inventory", produces = "application/json")
@Tag(name = "Inventory", description = "Inventory management API")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    @GetMapping("/{sku}")
    @Operation(summary = "Get inventory by SKU", description = "Retrieve inventory information for a specific SKU")
    public ResponseEntity<InventoryResponse> getInventory(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        InventoryResponse inventory = inventoryService.getInventory(sku);
        return ResponseEntity.ok(inventory);
    }
    
    @PostMapping(value = "/reserve", produces = "application/json")
    @Operation(summary = "Reserve inventory", description = "Reserve inventory for an order")
    public ResponseEntity<Boolean> reserveInventory(
            @Valid @RequestBody ReserveInventoryRequest request) {
        boolean reserved = inventoryService.reserveInventory(request);
        
        if (reserved) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(false);
        }
    }
    
    @PostMapping(value = "/release", produces = "application/json")
    @Operation(summary = "Release inventory", description = "Release reserved inventory")
    public ResponseEntity<Boolean> releaseInventory(
            @Valid @RequestBody ReleaseInventoryRequest request) {
        boolean released = inventoryService.releaseInventory(request);
        
        if (released) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(false);
        }
    }
    
    @PostMapping("/{sku}/init")
    @Operation(summary = "Initialize inventory", description = "Create initial inventory for a SKU")
    public ResponseEntity<String> initializeInventory(
            @Parameter(description = "Product SKU") @PathVariable String sku,
            @Parameter(description = "Initial quantity") @RequestParam Integer quantity) {
        inventoryService.createInitialInventory(sku, quantity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Initial inventory created successfully");
    }
}