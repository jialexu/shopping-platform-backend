package com.icc.itemservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.icc.itemservice.dto.ItemCreateRequest;
import com.icc.itemservice.dto.ItemResponse;
import com.icc.itemservice.dto.ItemUpdateRequest;
import com.icc.itemservice.service.ItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item Management", description = "APIs for managing items")
public class ItemController {
    
    private final ItemService itemService;
    
    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    @PostMapping
    @Operation(summary = "Create new item", description = "Creates a new item in the system")
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemCreateRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Updates an existing item by ID")
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "Item ID") @PathVariable String id,
            @Valid @RequestBody ItemUpdateRequest request) {
        ItemResponse response = itemService.updateItem(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves an item by its ID")
    public ResponseEntity<ItemResponse> getItemById(
            @Parameter(description = "Item ID") @PathVariable String id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-upc/{upc}")
    @Operation(summary = "Get item by UPC", description = "Retrieves an item by its UPC code")
    public ResponseEntity<ItemResponse> getItemByUpc(
            @Parameter(description = "UPC code") @PathVariable("upc") String upc) {
        ItemResponse response = itemService.getItemByUpc(upc);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test/{value}")
    @Operation(summary = "Test path variable", description = "Test endpoint for debugging path variables")
    public ResponseEntity<String> testPathVariable(
            @Parameter(description = "Test value") @PathVariable("value") String value) {
        return ResponseEntity.ok("Received value: " + value);
    }
    
    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieves paginated list of all items")
    public ResponseEntity<Page<ItemResponse>> getAllItems(
            @Parameter(description = "Pagination information") Pageable pageable) {
        Page<ItemResponse> response = itemService.getAllItems(pageable);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Deletes an item by its ID")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "Item ID") @PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}