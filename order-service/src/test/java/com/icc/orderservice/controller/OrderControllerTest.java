package com.icc.orderservice.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.icc.orderservice.dto.CreateOrderItemRequest;
import com.icc.orderservice.dto.CreateOrderRequest;
import com.icc.orderservice.entity.Order;
import com.icc.orderservice.entity.OrderStatus;
import com.icc.orderservice.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private CreateOrderRequest createOrderRequest;
    private Order order;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        CreateOrderItemRequest item = new CreateOrderItemRequest("SKU-001", 2);
        createOrderRequest = new CreateOrderRequest("user-123", Arrays.asList(item), "123 Test St");

        order = new Order();
        order.setId("order-123");
        order.setUserId("user-123");
        order.setTotalAmount(new BigDecimal("20.00"));
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress("123 Test St");
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Given
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        // When
        ResponseEntity<?> response = orderController.createOrder(createOrderRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Order.class);
        if (response.getBody() instanceof Order resultOrder) {
            assertThat(resultOrder.getId()).isEqualTo("order-123");
            assertThat(resultOrder.getUserId()).isEqualTo("user-123");
        }
    }

    @Test
    void getOrder_ShouldReturnOrder_WhenOrderExists() {
        // Given
        when(orderService.getOrder("order-123")).thenReturn(Optional.of(order));

        // When
        ResponseEntity<?> response = orderController.getOrder("order-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Order.class);
        if (response.getBody() instanceof Order resultOrder) {
            assertThat(resultOrder.getId()).isEqualTo("order-123");
        }
    }

    @Test
    void getOrder_ShouldReturnNotFound_WhenOrderNotExists() {
        // Given
        when(orderService.getOrder("nonexistent")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = orderController.getOrder("nonexistent");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void cancelOrder_ShouldReturnNoContent() {
        // When
        ResponseEntity<?> response = orderController.cancelOrder("order-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}