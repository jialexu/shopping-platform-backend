package com.icc.orderservice.repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.icc.orderservice.entity.Order;
import com.icc.orderservice.entity.OrderItem;
import com.icc.orderservice.entity.OrderStatus;

import jakarta.annotation.PostConstruct;

@Repository
public class OrderRepository {

    @Autowired
    private CqlSession cqlSession;

    private PreparedStatement insertOrderStatement;
    private PreparedStatement updateOrderStatement;
    private PreparedStatement findOrderByIdStatement;
    private PreparedStatement deleteOrderStatement;
    private PreparedStatement insertOrderItemStatement;
    private PreparedStatement findOrderItemsByOrderIdStatement;

    @PostConstruct
    public void prepare() {
        insertOrderStatement = cqlSession.prepare(
                "INSERT INTO orders (id, user_id, total_amount, status, created_at, updated_at, shipping_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");

        updateOrderStatement = cqlSession.prepare(
                "UPDATE orders SET total_amount = ?, status = ?, updated_at = ?, shipping_address = ? WHERE id = ?");

        findOrderByIdStatement = cqlSession.prepare(
                "SELECT * FROM orders WHERE id = ?");

        deleteOrderStatement = cqlSession.prepare(
                "DELETE FROM orders WHERE id = ?");

        insertOrderItemStatement = cqlSession.prepare(
                "INSERT INTO order_items (order_id, sku, qty, unit_price) VALUES (?, ?, ?, ?)");

        findOrderItemsByOrderIdStatement = cqlSession.prepare(
                "SELECT * FROM order_items WHERE order_id = ?");
    }

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(UUID.randomUUID().toString());
            order.setCreatedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());

        cqlSession.execute(insertOrderStatement.bind(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt().toInstant(ZoneOffset.UTC),
                order.getUpdatedAt().toInstant(ZoneOffset.UTC),
                order.getShippingAddress()
        ));

        // Save order items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrderId(order.getId());
                cqlSession.execute(insertOrderItemStatement.bind(
                        item.getOrderId(),
                        item.getSku(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ));
            }
        }

        return order;
    }

    public Order update(Order order) {
        order.setUpdatedAt(LocalDateTime.now());

        cqlSession.execute(updateOrderStatement.bind(
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getUpdatedAt().toInstant(ZoneOffset.UTC),
                order.getShippingAddress(),
                order.getId()
        ));

        return order;
    }

    public Optional<Order> findById(String id) {
        ResultSet resultSet = cqlSession.execute(findOrderByIdStatement.bind(id));
        Row row = resultSet.one();

        if (row == null) {
            return Optional.empty();
        }

        Order order = new Order();
        order.setId(row.getString("id"));
        order.setUserId(row.getString("user_id"));
        order.setTotalAmount(row.getBigDecimal("total_amount"));
        order.setStatus(OrderStatus.valueOf(row.getString("status")));
        order.setCreatedAt(LocalDateTime.ofInstant(row.getInstant("created_at"), ZoneOffset.UTC));
        order.setUpdatedAt(LocalDateTime.ofInstant(row.getInstant("updated_at"), ZoneOffset.UTC));
        order.setShippingAddress(row.getString("shipping_address"));

        // Load order items
        order.setItems(findOrderItemsByOrderId(id));

        return Optional.of(order);
    }

    public void deleteById(String id) {
        cqlSession.execute(deleteOrderStatement.bind(id));
    }

    private List<OrderItem> findOrderItemsByOrderId(String orderId) {
        ResultSet resultSet = cqlSession.execute(findOrderItemsByOrderIdStatement.bind(orderId));
        List<OrderItem> items = new ArrayList<>();

        for (Row row : resultSet) {
            OrderItem item = new OrderItem();
            item.setOrderId(row.getString("order_id"));
            item.setSku(row.getString("sku"));
            item.setQuantity(row.getInt("qty"));
            item.setUnitPrice(row.getBigDecimal("unit_price"));
            items.add(item);
        }

        return items;
    }
}