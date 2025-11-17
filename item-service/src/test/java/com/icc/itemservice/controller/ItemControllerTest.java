package com.icc.itemservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icc.itemservice.dto.ItemCreateRequest;
import com.icc.itemservice.dto.ItemUpdateRequest;
import com.icc.itemservice.dto.ItemResponse;
import com.icc.itemservice.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
class ItemControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ItemService itemService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ItemCreateRequest createRequest;
    private ItemUpdateRequest updateRequest;
    private ItemResponse itemResponse;
    
    @BeforeEach
    void setUp() {
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
        
        itemResponse = new ItemResponse();
        itemResponse.setId("test-id");
        itemResponse.setName("Test Item");
        itemResponse.setPrice(19.99);
        itemResponse.setUpc("123456789");
        itemResponse.setPictures(Arrays.asList("image1.jpg", "image2.jpg"));
        itemResponse.setAttributes(Map.of("color", "red", "size", "M"));
        itemResponse.setCreatedAt(LocalDateTime.now());
        itemResponse.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void createItem_Success() throws Exception {
        // Given
        when(itemService.createItem(any(ItemCreateRequest.class))).thenReturn(itemResponse);
        
        // When & Then
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.name").value(itemResponse.getName()))
                .andExpect(jsonPath("$.price").value(itemResponse.getPrice()))
                .andExpect(jsonPath("$.upc").value(itemResponse.getUpc()));
    }
    
    @Test
    void updateItem_Success() throws Exception {
        // Given
        when(itemService.updateItem(eq("test-id"), any(ItemUpdateRequest.class))).thenReturn(itemResponse);
        
        // When & Then
        mockMvc.perform(put("/api/items/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.name").value(itemResponse.getName()));
    }
    
    @Test
    void getItemById_Success() throws Exception {
        // Given
        when(itemService.getItemById("test-id")).thenReturn(itemResponse);
        
        // When & Then
        mockMvc.perform(get("/api/items/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.name").value(itemResponse.getName()));
    }
    
    @Test
    void getItemByUpc_Success() throws Exception {
        // Given
        when(itemService.getItemByUpc("123456789")).thenReturn(itemResponse);
        
        // When & Then
        mockMvc.perform(get("/api/items/by-upc/123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.upc").value(itemResponse.getUpc()));
    }
    
    @Test
    void getAllItems_Success() throws Exception {
        // Given
        List<ItemResponse> items = Arrays.asList(itemResponse);
        Page<ItemResponse> itemPage = new PageImpl<>(items, PageRequest.of(0, 10), 1);
        when(itemService.getAllItems(any())).thenReturn(itemPage);
        
        // When & Then
        mockMvc.perform(get("/api/items")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(itemResponse.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void deleteItem_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/items/test-id"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void createItem_ValidationError() throws Exception {
        // Given - invalid request with null name
        createRequest.setName(null);
        
        // When & Then
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }
}