package com.icc.payment.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.icc.payment.entity.Payment;
import com.icc.payment.entity.PaymentStatus;
import com.icc.payment.repository.PaymentRepository;

@Component
public class PaymentProcessor {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public CompletableFuture<Void> processPayment(Payment payment) {
        try {
            // Process payment with external gateway
            Thread.sleep(2000);
            boolean success = processWithGateway(payment);
            
            if (success) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setProviderRef("PAY_" + System.currentTimeMillis());
                
                PaymentEvent event = new PaymentEvent();
                event.setPaymentId(payment.getId());
                event.setOrderId(payment.getOrderId());
                event.setAmount(payment.getAmount());
                event.setStatus(PaymentStatus.SUCCEEDED);
                event.setEventType("PAYMENT_SUCCEEDED");
                kafkaTemplate.send("payment.succeeded", payment.getOrderId(), event);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                
                PaymentEvent event = new PaymentEvent();
                event.setPaymentId(payment.getId());
                event.setOrderId(payment.getOrderId());
                event.setAmount(payment.getAmount());
                event.setStatus(PaymentStatus.FAILED);
                event.setEventType("PAYMENT_FAILED");
                kafkaTemplate.send("payment.failed", payment.getOrderId(), event);
            }
            
            paymentRepository.save(payment);

        } catch (InterruptedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            Thread.currentThread().interrupt();
        }
        
        return CompletableFuture.completedFuture(null);
    }

    public boolean processRefund(Payment payment, BigDecimal refundAmount) {
        try {
            Thread.sleep(1000);
            return processRefundWithGateway(payment, refundAmount);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean processWithGateway(Payment payment) {
        // Integrate with payment gateway API
        return Math.random() < 0.9;
    }

    private boolean processRefundWithGateway(Payment payment, BigDecimal refundAmount) {
        // Integrate with refund gateway API
        return true;
    }
}