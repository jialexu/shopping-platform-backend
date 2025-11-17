package com.icc.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.icc.orderservice.entity.OrderStatus;
import com.icc.orderservice.event.PaymentEvent;

@Service
public class PaymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "payment.succeeded", groupId = "order-service")
    public void handlePaymentSucceeded(PaymentEvent event) {
        logger.info("Received payment succeeded event for order: {}", event.getOrderId());
        try {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
            logger.info("Updated order {} status to PAID", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error updating order status for payment succeeded: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void handlePaymentFailed(PaymentEvent event) {
        logger.info("Received payment failed event for order: {}", event.getOrderId());
        try {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
            logger.info("Updated order {} status to CANCELLED due to payment failure", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error updating order status for payment failed: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment.refunded", groupId = "order-service")
    public void handlePaymentRefunded(PaymentEvent event) {
        logger.info("Received payment refunded event for order: {}", event.getOrderId());
        try {
            // Refund means the order was PAID, but now money is returned
            // We mark it as REFUNDED which is distinct from CANCELLED (never paid)
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.REFUNDED);
            logger.info("Updated order {} status to REFUNDED (payment was returned to customer)", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error updating order status for payment refunded: {}", e.getMessage(), e);
        }
    }
}