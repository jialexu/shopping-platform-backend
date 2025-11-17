package com.icc.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icc.payment.dto.PaymentRequest;
import com.icc.payment.dto.PaymentResponse;
import com.icc.payment.dto.PaymentStatusUpdateRequest;
import com.icc.payment.dto.RefundRequest;
import com.icc.payment.entity.Payment;
import com.icc.payment.entity.PaymentStatus;
import com.icc.payment.exception.DuplicatePaymentException;
import com.icc.payment.exception.InvalidPaymentStateException;
import com.icc.payment.exception.PaymentNotFoundException;
import com.icc.payment.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentProcessor paymentProcessor;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                         KafkaTemplate<String, Object> kafkaTemplate,
                         PaymentProcessor paymentProcessor) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentProcessor = paymentProcessor;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        // Check for duplicate order payment (idempotency)
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new DuplicatePaymentException("Payment for order " + request.getOrderId() + " already exists");
        }

        // Create payment entity
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setDescription(request.getDescription());
        payment.setStatus(PaymentStatus.PENDING);

        // Save payment
        payment = paymentRepository.save(payment);

        // Process payment asynchronously
        paymentProcessor.processPayment(payment);

        return convertToResponse(payment);
    }

    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(request.getStatus());
        payment.setProviderRef(request.getProviderRef());
        if (request.getDescription() != null) {
            payment.setDescription(request.getDescription());
        }

        payment = paymentRepository.save(payment);

        // Send Kafka event for manual status changes (not from processor)
        if (oldStatus != request.getStatus()) {
            sendPaymentStatusEvent(payment, oldStatus);
        }

        return convertToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));
        return convertToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
        return convertToResponse(payment);
    }

    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new InvalidPaymentStateException("Cannot refund payment that is not in SUCCEEDED state");
        }

        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentStateException("Refund amount cannot exceed original payment amount");
        }

        // Process refund
        boolean refundSuccess = paymentProcessor.processRefund(payment, request.getAmount());
        
        if (refundSuccess) {
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setDescription("Refunded: " + request.getReason());
            payment = paymentRepository.save(payment);

            // Send refund event
            sendRefundEvent(payment);
        }

        return convertToResponse(payment);
    }

    private void sendPaymentStatusEvent(Payment payment, PaymentStatus oldStatus) {
        if (payment.getStatus() == PaymentStatus.SUCCEEDED && oldStatus != PaymentStatus.SUCCEEDED) {
            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(payment.getId());
            event.setOrderId(payment.getOrderId());
            event.setAmount(payment.getAmount());
            event.setStatus(payment.getStatus());
            event.setEventType("PAYMENT_SUCCEEDED");
            kafkaTemplate.send("payment.succeeded", payment.getOrderId(), event);
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            PaymentEvent event = new PaymentEvent();
            event.setPaymentId(payment.getId());
            event.setOrderId(payment.getOrderId());
            event.setAmount(payment.getAmount());
            event.setStatus(payment.getStatus());
            event.setEventType("PAYMENT_FAILED");
            kafkaTemplate.send("payment.failed", payment.getOrderId(), event);
        }
    }

    private void sendRefundEvent(Payment payment) {
        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(payment.getId());
        event.setOrderId(payment.getOrderId());
        event.setAmount(payment.getAmount());
        event.setStatus(payment.getStatus());
        event.setEventType("PAYMENT_REFUNDED");

        kafkaTemplate.send("payment.refunded", payment.getOrderId(), event);
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getProviderRef(),
                payment.getPaymentMethod(),
                payment.getDescription(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}