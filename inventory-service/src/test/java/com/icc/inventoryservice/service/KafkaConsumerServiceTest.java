package com.icc.inventoryservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icc.inventoryservice.dto.OrderCancelledEvent;

class KafkaConsumerServiceTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private OrderCancelledEvent testEvent;
    private String testMessage;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testEvent = new OrderCancelledEvent("ORDER-123", "TEST-SKU", 5);
        testMessage = "{\"orderId\":\"ORDER-123\",\"sku\":\"TEST-SKU\",\"quantity\":5}";
    }

    @Test
    void handleOrderCancelled_WhenValidMessage_ShouldReleaseInventory() throws JsonProcessingException {
        // Given
        when(objectMapper.readValue(testMessage, OrderCancelledEvent.class)).thenReturn(testEvent);
        when(inventoryService.releaseInventory(any())).thenReturn(true);

        // When
        kafkaConsumerService.handleOrderCancelled(testMessage);

        // Then
        verify(objectMapper).readValue(testMessage, OrderCancelledEvent.class);
        verify(inventoryService).releaseInventory(any());
    }

    @Test
    void handleOrderCancelled_WhenInvalidMessage_ShouldHandleException() throws JsonProcessingException {
        // Given
        when(objectMapper.readValue(anyString(), eq(OrderCancelledEvent.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When
        kafkaConsumerService.handleOrderCancelled("invalid json");

        // Then
        verify(objectMapper).readValue("invalid json", OrderCancelledEvent.class);
        verify(inventoryService, never()).releaseInventory(any());
    }

    @Test
    void handleOrderCancelled_WhenReleaseInventoryFails_ShouldLogError() throws JsonProcessingException {
        // Given
        when(objectMapper.readValue(testMessage, OrderCancelledEvent.class)).thenReturn(testEvent);
        when(inventoryService.releaseInventory(any())).thenReturn(false);

        // When
        kafkaConsumerService.handleOrderCancelled(testMessage);

        // Then
        verify(inventoryService).releaseInventory(any());
    }
}