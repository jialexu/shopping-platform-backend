package com.icc.orderservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icc.orderservice.client.InventoryServiceClient;
import com.icc.orderservice.client.ItemServiceClient;
import com.icc.orderservice.dto.CreateOrderRequest;
import com.icc.orderservice.dto.InventoryRequest;
import com.icc.orderservice.dto.ItemResponse;
import com.icc.orderservice.dto.UpdateOrderRequest;
import com.icc.orderservice.entity.Order;
import com.icc.orderservice.entity.OrderItem;
import com.icc.orderservice.entity.OrderStatus;
import com.icc.orderservice.event.OrderEvent;
import com.icc.orderservice.repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemServiceClient itemServiceClient;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @Autowired
    private OrderEventService orderEventService;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        logger.info("Creating order for user: {}", request.getUserId());

        // Validate items and get prices
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            try {
                // Get item details from item service
                ItemResponse item = itemServiceClient.getItemByUpc(itemRequest.getSku());
                
                // Check inventory availability
                var inventory = inventoryServiceClient.getInventory(itemRequest.getSku());
                if (inventory.getAvailable() < itemRequest.getQuantity()) {
                    throw new RuntimeException("Insufficient inventory for SKU: " + itemRequest.getSku());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setSku(itemRequest.getSku());
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setUnitPrice(item.getPrice());
                orderItems.add(orderItem);

                totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            } catch (RuntimeException e) {
                logger.error("Error validating item {}: {}", itemRequest.getSku(), e.getMessage());
                throw new RuntimeException("Error validating item: " + itemRequest.getSku());
            }
        }

        // Create order
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(request.getShippingAddress());
        order.setItems(orderItems);

        order = orderRepository.save(order);

        // Reserve inventory
        try {
            for (OrderItem item : order.getItems()) {
                InventoryRequest inventoryRequest = new InventoryRequest(
                        item.getSku(), item.getQuantity(), order.getId());
                try {
                    Boolean reserved = inventoryServiceClient.reserveInventory(inventoryRequest);
                    if (!reserved) {
                        throw new RuntimeException("Failed to reserve inventory for SKU: " + item.getSku());
                    }
                } catch (Exception e) {
                    // Handle Feign exceptions (409 conflicts) as reservation failure
                    logger.error("Failed to reserve inventory for SKU {}: {}", item.getSku(), e.getMessage());
                    throw new RuntimeException("Failed to reserve inventory for SKU: " + item.getSku());
                }
            }

            // Publish events
            OrderEvent createdEvent = new OrderEvent(order.getId(), order.getUserId(), "ORDER_CREATED", order);
            orderEventService.publishOrderCreated(createdEvent);

            OrderEvent reservedEvent = new OrderEvent(order.getId(), order.getUserId(), "INVENTORY_RESERVED", order);
            orderEventService.publishOrderInventoryReserved(reservedEvent);

            // Update status to PENDING_PAYMENT
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepository.update(order);

        } catch (RuntimeException e) {
            logger.error("Error reserving inventory for order {}: {}", order.getId(), e.getMessage());
            // Cancel the order and release any reserved inventory
            cancelOrder(order.getId());
            throw new RuntimeException("Failed to create order due to inventory issues");
        }

        logger.info("Successfully created order: {}", order.getId());
        return order;
    }

    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional
    public Order updateOrder(String orderId, UpdateOrderRequest request) {
        logger.info("Updating order: {}", orderId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        Order order = orderOpt.get();
        
        // Only allow certain status transitions
        if (!isValidStatusTransition(order.getStatus(), request.getStatus())) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + request.getStatus());
        }

        order.setStatus(request.getStatus());
        if (request.getShippingAddress() != null) {
            order.setShippingAddress(request.getShippingAddress());
        }

        return orderRepository.update(order);
    }

    @Transactional
    public void cancelOrder(String orderId) {
        logger.info("Cancelling order: {}", orderId);

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        Order order = orderOpt.get();
        
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel order in status: " + order.getStatus());
        }

        // Release inventory
        for (OrderItem item : order.getItems()) {
            try {
                InventoryRequest inventoryRequest = new InventoryRequest(
                        item.getSku(), item.getQuantity(), order.getId());
                inventoryServiceClient.releaseInventory(inventoryRequest);
            } catch (Exception e) {
                logger.warn("Failed to release inventory for SKU {} in order {}: {}", 
                           item.getSku(), orderId, e.getMessage());
            }
        }

        // Update status and publish event
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);

        OrderEvent cancelledEvent = new OrderEvent(order.getId(), order.getUserId(), "ORDER_CANCELLED", order);
        orderEventService.publishOrderCancelled(cancelledEvent);
    }

    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            orderRepository.update(order);
        }
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case CREATED -> newStatus == OrderStatus.PENDING_PAYMENT || newStatus == OrderStatus.CANCELLED;
            case PENDING_PAYMENT -> newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID -> newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
            default -> false;
        }; // Terminal states
    }
}