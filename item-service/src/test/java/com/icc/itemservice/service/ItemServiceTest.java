package com.icc.itemservice.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.icc.itemservice.dto.ItemCreateRequest;
import com.icc.itemservice.dto.ItemResponse;
import com.icc.itemservice.dto.ItemUpdateRequest;
import com.icc.itemservice.entity.Item;
import com.icc.itemservice.exception.DuplicateUpcException;
import com.icc.itemservice.exception.ItemNotFoundException;
import com.icc.itemservice.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    
    @Mock
    private ItemRepository itemRepository;
    
    @InjectMocks
    private ItemService itemService;
    
    private Item testItem;
    private ItemCreateRequest createRequest;
    private ItemUpdateRequest updateRequest;
    
    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId("test-id");
        testItem.setName("Test Item");
        testItem.setPrice(19.99);
        testItem.setUpc("123456789");
        testItem.setPictures(Arrays.asList("image1.jpg", "image2.jpg"));
        testItem.setAttributes(Map.of("color", "red", "size", "M"));
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setUpdatedAt(LocalDateTime.now());
        
        createRequest = new ItemCreateRequest();
        createRequest.setName("Test Item");
        createRequest.setPrice(19.99);
        createRequest.setUpc("123456789");
        createRequest.setPictures(Arrays.asList("image1.jpg", "image2.jpg"));
        createRequest.setAttributes(Map.of("color", "red", "size", "M"));
        
        updateRequest = new ItemUpdateRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setPrice(29.99);
        updateRequest.setPictures(Arrays.asList("updated_image.jpg"));
        updateRequest.setAttributes(Map.of("color", "blue", "size", "L"));
    }
    
    @Test
    void createItem_Success() {
        // Given
        when(itemRepository.existsByUpc(createRequest.getUpc())).thenReturn(false);
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        
        // When
        ItemResponse response = itemService.createItem(createRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(testItem.getId(), response.getId());
        assertEquals(testItem.getName(), response.getName());
        assertEquals(testItem.getPrice(), response.getPrice());
        assertEquals(testItem.getUpc(), response.getUpc());
        
        verify(itemRepository).existsByUpc(createRequest.getUpc());
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void createItem_DuplicateUpc_ThrowsException() {
        // Given
        when(itemRepository.existsByUpc(createRequest.getUpc())).thenReturn(true);
        
        // When & Then
        DuplicateUpcException exception = assertThrows(DuplicateUpcException.class, 
            () -> itemService.createItem(createRequest));
        
        assertEquals("Item with UPC " + createRequest.getUpc() + " already exists", exception.getMessage());
        verify(itemRepository).existsByUpc(createRequest.getUpc());
        verify(itemRepository, never()).save(any(Item.class));
    }
    
    @Test
    void updateItem_Success() {
        // Given
        when(itemRepository.findById(testItem.getId())).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        
        // When
        ItemResponse response = itemService.updateItem(testItem.getId(), updateRequest);
        
        // Then
        assertNotNull(response);
        verify(itemRepository).findById(testItem.getId());
        verify(itemRepository).save(any(Item.class));
    }
    
    @Test
    void updateItem_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        
        // When & Then
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
            () -> itemService.updateItem("non-existent-id", updateRequest));
        
        assertEquals("Item not found with id: non-existent-id", exception.getMessage());
        verify(itemRepository).findById("non-existent-id");
        verify(itemRepository, never()).save(any(Item.class));
    }
    
    @Test
    void getItemById_Success() {
        // Given
        when(itemRepository.findById(testItem.getId())).thenReturn(Optional.of(testItem));
        
        // When
        ItemResponse response = itemService.getItemById(testItem.getId());
        
        // Then
        assertNotNull(response);
        assertEquals(testItem.getId(), response.getId());
        assertEquals(testItem.getName(), response.getName());
        verify(itemRepository).findById(testItem.getId());
    }
    
    @Test
    void getItemById_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        
        // When & Then
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
            () -> itemService.getItemById("non-existent-id"));
        
        assertEquals("Item not found with id: non-existent-id", exception.getMessage());
        verify(itemRepository).findById("non-existent-id");
    }
    
    @Test
    void getItemByUpc_Success() {
        // Given
        when(itemRepository.findByUpc(testItem.getUpc())).thenReturn(Optional.of(testItem));
        
        // When
        ItemResponse response = itemService.getItemByUpc(testItem.getUpc());
        
        // Then
        assertNotNull(response);
        assertEquals(testItem.getUpc(), response.getUpc());
        verify(itemRepository).findByUpc(testItem.getUpc());
    }
    
    @Test
    void getItemByUpc_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findByUpc("non-existent-upc")).thenReturn(Optional.empty());
        
        // When & Then
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
            () -> itemService.getItemByUpc("non-existent-upc"));
        
        assertEquals("Item not found with UPC: non-existent-upc", exception.getMessage());
        verify(itemRepository).findByUpc("non-existent-upc");
    }
    
    @Test
    void getAllItems_Success() {
        // Given
        List<Item> items = Arrays.asList(testItem);
        Page<Item> itemPage = new PageImpl<>(items);
        Pageable pageable = PageRequest.of(0, 10);
        
        when(itemRepository.findAll(pageable)).thenReturn(itemPage);
        
        // When
        Page<ItemResponse> response = itemService.getAllItems(pageable);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(testItem.getId(), response.getContent().get(0).getId());
        verify(itemRepository).findAll(pageable);
    }
    
    @Test
    void deleteItem_Success() {
        // Given
        when(itemRepository.existsById(testItem.getId())).thenReturn(true);
        
        // When
        itemService.deleteItem(testItem.getId());
        
        // Then
        verify(itemRepository).existsById(testItem.getId());
        verify(itemRepository).deleteById(testItem.getId());
    }
    
    @Test
    void deleteItem_NotFound_ThrowsException() {
        // Given
        when(itemRepository.existsById("non-existent-id")).thenReturn(false);
        
        // When & Then
        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
            () -> itemService.deleteItem("non-existent-id"));
        
        assertEquals("Item not found with id: non-existent-id", exception.getMessage());
        verify(itemRepository).existsById("non-existent-id");
        verify(itemRepository, never()).deleteById(any(String.class));
    }
}