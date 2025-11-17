package com.icc.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.icc.orderservice.event.OrderEvent;

@Service
public class OrderEventService {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderEvent event) {
        logger.info("Publishing order created event for order: {}", event.getOrderId());
        kafkaTemplate.send("order.created", event.getOrderId(), event);
    }

    public void publishOrderInventoryReserved(OrderEvent event) {
        logger.info("Publishing order inventory reserved event for order: {}", event.getOrderId());
        kafkaTemplate.send("order.inventory_reserved", event.getOrderId(), event);
    }

    public void publishOrderCancelled(OrderEvent event) {
        logger.info("Publishing order cancelled event for order: {}", event.getOrderId());
        kafkaTemplate.send("order.cancelled", event.getOrderId(), event);
    }
}