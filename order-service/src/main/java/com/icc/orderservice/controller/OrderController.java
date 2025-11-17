package com.icc.orderservice.controller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.icc.orderservice.dto.CreateOrderRequest;
import com.icc.orderservice.dto.UpdateOrderRequest;
import com.icc.orderservice.entity.Order;
import com.icc.orderservice.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided items")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating order: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves an order by its ID")
    public ResponseEntity<?> getOrder(
            @Parameter(description = "Order ID") @PathVariable String id) {
        try {
            Optional<Order> order = orderService.getOrder(id);
            if (order.isPresent()) {
                return ResponseEntity.ok(order.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error retrieving order: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order", description = "Updates an existing order")
    public ResponseEntity<?> updateOrder(
            @Parameter(description = "Order ID") @PathVariable String id,
            @Valid @RequestBody UpdateOrderRequest request) {
        try {
            Order order = orderService.updateOrder(id, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error updating order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating order: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel order", description = "Cancels an existing order")
    public ResponseEntity<?> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error cancelling order: " + e.getMessage());
        }
    }
}