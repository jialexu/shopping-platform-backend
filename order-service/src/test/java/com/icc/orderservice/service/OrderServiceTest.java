package com.icc.orderservice.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.icc.orderservice.client.InventoryServiceClient;
import com.icc.orderservice.client.ItemServiceClient;
import com.icc.orderservice.dto.CreateOrderItemRequest;
import com.icc.orderservice.dto.CreateOrderRequest;
import com.icc.orderservice.dto.ItemResponse;
import com.icc.orderservice.entity.Order;
import com.icc.orderservice.entity.OrderStatus;
import com.icc.orderservice.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemServiceClient itemServiceClient;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @Mock
    private OrderEventService orderEventService;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest createOrderRequest;
    private ItemResponse itemResponse;
    private InventoryServiceClient.InventoryResponse inventoryResponse;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Setup test data
        CreateOrderItemRequest item = new CreateOrderItemRequest("SKU-001", 2);
        createOrderRequest = new CreateOrderRequest("user-123", Arrays.asList(item), "123 Test St");

        itemResponse = new ItemResponse("item-1", "Test Item", new BigDecimal("10.00"), "UPC-001");
        
        inventoryResponse = new InventoryServiceClient.InventoryResponse("SKU-001", 10, 0);
    }

    @Test
    void createOrder_ShouldCreateOrderSuccessfully() {
        // Given
        when(itemServiceClient.getItem("SKU-001")).thenReturn(itemResponse);
        when(inventoryServiceClient.getInventory("SKU-001")).thenReturn(inventoryResponse);
        when(inventoryServiceClient.reserveInventory(any())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId("order-123");
            return order;
        });
        when(orderRepository.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderService.createOrder(createOrderRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-123");
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);

        verify(itemServiceClient).getItem("SKU-001");
        verify(inventoryServiceClient).getInventory("SKU-001");
        verify(inventoryServiceClient).reserveInventory(any());
        verify(orderRepository).save(any(Order.class));
        verify(orderEventService).publishOrderCreated(any());
        verify(orderEventService).publishOrderInventoryReserved(any());
    }

    @Test
    void createOrder_ShouldThrowException_WhenInsufficientInventory() {
        // Given
        InventoryServiceClient.InventoryResponse insufficientInventory = 
                new InventoryServiceClient.InventoryResponse("SKU-001", 1, 0);
        
        when(itemServiceClient.getItem("SKU-001")).thenReturn(itemResponse);
        when(inventoryServiceClient.getInventory("SKU-001")).thenReturn(insufficientInventory);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error validating item");

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventService, never()).publishOrderCreated(any());
    }

    @Test
    void getOrder_ShouldReturnOrder_WhenOrderExists() {
        // Given
        Order order = new Order();
        order.setId("order-123");
        order.setUserId("user-123");
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));

        // When
        Optional<Order> result = orderService.getOrder("order-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("order-123");
        assertThat(result.get().getUserId()).isEqualTo("user-123");
    }

    @Test
    void getOrder_ShouldReturnEmpty_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrder("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void cancelOrder_ShouldCancelOrder_WhenOrderExists() {
        // Given
        Order order = new Order();
        order.setId("order-123");
        order.setUserId("user-123");
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setItems(Arrays.asList()); // Empty items for simplicity

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));
        when(orderRepository.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.cancelOrder("order-123");

        // Then
        verify(orderRepository).findById("order-123");
        verify(orderRepository).update(any(Order.class));
        verify(orderEventService).publishOrderCancelled(any());
    }

    @Test
    void cancelOrder_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository, never()).update(any(Order.class));
        verify(orderEventService, never()).publishOrderCancelled(any());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_WhenOrderExists() {
        // Given
        Order order = new Order();
        order.setId("order-123");
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));
        when(orderRepository.update(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        orderService.updateOrderStatus("order-123", OrderStatus.PAID);

        // Then
        verify(orderRepository).findById("order-123");
        verify(orderRepository).update(argThat(o -> o.getStatus() == OrderStatus.PAID));
    }
}