package com.icc.itemservice.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.icc.itemservice.dto.ItemCreateRequest;
import com.icc.itemservice.dto.ItemResponse;
import com.icc.itemservice.dto.ItemUpdateRequest;
import com.icc.itemservice.entity.Item;
import com.icc.itemservice.exception.DuplicateUpcException;
import com.icc.itemservice.exception.ItemNotFoundException;
import com.icc.itemservice.repository.ItemRepository;

@Service
public class ItemService {
    
    private final ItemRepository itemRepository;
    
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }
    
    public ItemResponse createItem(ItemCreateRequest request) {
        // Check if UPC already exists
        if (itemRepository.existsByUpc(request.getUpc())) {
            throw new DuplicateUpcException("Item with UPC " + request.getUpc() + " already exists");
        }
        
        Item item = new Item(
            request.getName(),
            request.getPrice(),
            request.getUpc(),
            request.getPictures(),
            request.getAttributes()
        );
        
        Item savedItem = itemRepository.save(item);
        return convertToResponse(savedItem);
    }
    
    public ItemResponse updateItem(String id, ItemUpdateRequest request) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        
        if (request.getName() != null) {
            item.setName(request.getName());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        if (request.getPictures() != null) {
            item.setPictures(request.getPictures());
        }
        if (request.getAttributes() != null) {
            item.setAttributes(request.getAttributes());
        }
        
        item.setUpdatedAt(LocalDateTime.now());
        
        Item savedItem = itemRepository.save(item);
        return convertToResponse(savedItem);
    }
    
    public ItemResponse getItemById(String id) {
        Item item = itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        return convertToResponse(item);
    }
    
    public ItemResponse getItemByUpc(String upc) {
        Item item = itemRepository.findByUpc(upc)
            .orElseThrow(() -> new ItemNotFoundException("Item not found with UPC: " + upc));
        return convertToResponse(item);
    }
    
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable)
            .map(this::convertToResponse);
    }
    
    public void deleteItem(String id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }
    
    private ItemResponse convertToResponse(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getPrice(),
            item.getUpc(),
            item.getPictures(),
            item.getAttributes(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}