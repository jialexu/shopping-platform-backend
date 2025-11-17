package com.icc.inventoryservice.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.icc.inventoryservice.dto.InventoryResponse;
import com.icc.inventoryservice.dto.ReleaseInventoryRequest;
import com.icc.inventoryservice.dto.ReserveInventoryRequest;
import com.icc.inventoryservice.model.Inventory;
import com.icc.inventoryservice.repository.InventoryEventRepository;
import com.icc.inventoryservice.repository.InventoryRepository;

class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventRepository inventoryEventRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private ReserveInventoryRequest reserveRequest;
    private ReleaseInventoryRequest releaseRequest;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testInventory = new Inventory("TEST-SKU", 100, 20, LocalDateTime.now());
        reserveRequest = new ReserveInventoryRequest("TEST-SKU", 10, "ORDER-123");
        releaseRequest = new ReleaseInventoryRequest("TEST-SKU", 10, "ORDER-123");
    }

    @Test
    void getInventory_WhenInventoryExists_ShouldReturnInventoryResponse() {
        // Given
        when(inventoryRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testInventory));

        // When
        InventoryResponse response = inventoryService.getInventory("TEST-SKU");

        // Then
        assertNotNull(response);
        assertEquals("TEST-SKU", response.getSku());
        assertEquals(100, response.getAvailable());
        assertEquals(20, response.getReserved());
        assertEquals(120, response.getTotal());
        verify(inventoryRepository).findBySku("TEST-SKU");
    }

    @Test
    void getInventory_WhenInventoryNotExists_ShouldReturnZeroInventory() {
        // Given
        when(inventoryRepository.findBySku("UNKNOWN-SKU")).thenReturn(Optional.empty());

        // When
        InventoryResponse response = inventoryService.getInventory("UNKNOWN-SKU");

        // Then
        assertNotNull(response);
        assertEquals("UNKNOWN-SKU", response.getSku());
        assertEquals(0, response.getAvailable());
        assertEquals(0, response.getReserved());
        assertEquals(0, response.getTotal());
    }

    @Test
    void reserveInventory_WhenSufficientInventory_ShouldReserveSuccessfully() {
        // Given
        when(inventoryRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.reserveInventory(reserveRequest);

        // Then
        assertTrue(result);
        assertEquals(90, testInventory.getAvailable());
        assertEquals(30, testInventory.getReserved());
        verify(inventoryRepository).save(testInventory);
        verify(inventoryEventRepository).save(any());
    }

    @Test
    void reserveInventory_WhenInsufficientInventory_ShouldFailReservation() {
        // Given
        ReserveInventoryRequest largeRequest = new ReserveInventoryRequest("TEST-SKU", 150, "ORDER-123");
        when(inventoryRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.reserveInventory(largeRequest);

        // Then
        assertFalse(result);
        assertEquals(100, testInventory.getAvailable()); // Should remain unchanged
        verify(inventoryRepository, never()).save(any());
        verify(inventoryEventRepository, never()).save(any());
    }

    @Test
    void reserveInventory_WhenInventoryNotFound_ShouldFailReservation() {
        // Given
        when(inventoryRepository.findBySku("UNKNOWN-SKU")).thenReturn(Optional.empty());
        ReserveInventoryRequest unknownRequest = new ReserveInventoryRequest("UNKNOWN-SKU", 10, "ORDER-123");

        // When
        boolean result = inventoryService.reserveInventory(unknownRequest);

        // Then
        assertFalse(result);
        verify(inventoryRepository, never()).save(any());
        verify(inventoryEventRepository, never()).save(any());
    }

    @Test
    void releaseInventory_WhenSufficientReservedInventory_ShouldReleaseSuccessfully() {
        // Given
        when(inventoryRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.releaseInventory(releaseRequest);

        // Then
        assertTrue(result);
        assertEquals(110, testInventory.getAvailable());
        assertEquals(10, testInventory.getReserved());
        verify(inventoryRepository).save(testInventory);
        verify(inventoryEventRepository).save(any());
    }

    @Test
    void releaseInventory_WhenInsufficientReservedInventory_ShouldFailRelease() {
        // Given
        ReleaseInventoryRequest largeRelease = new ReleaseInventoryRequest("TEST-SKU", 30, "ORDER-123");
        when(inventoryRepository.findBySku("TEST-SKU")).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.releaseInventory(largeRelease);

        // Then
        assertFalse(result);
        assertEquals(100, testInventory.getAvailable()); // Should remain unchanged
        verify(inventoryRepository, never()).save(any());
        verify(inventoryEventRepository, never()).save(any());
    }

    @Test
    void createInitialInventory_ShouldCreateInventorySuccessfully() {
        // When
        inventoryService.createInitialInventory("NEW-SKU", 50);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryEventRepository).save(any());
    }
}