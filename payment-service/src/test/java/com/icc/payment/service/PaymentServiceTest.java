package com.icc.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

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

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_Success() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("ORDER-001");
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod("CREDIT_CARD");
        paymentRequest.setDescription("Test payment");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId("ORDER-001");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        when(paymentRepository.existsByOrderId(anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.createPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals("ORDER-001", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(PaymentStatus.PENDING, response.getStatus());

        verify(paymentRepository).existsByOrderId("ORDER-001");
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentProcessor).processPayment(any(Payment.class));
    }

    @Test
    void createPayment_DuplicateOrder() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("ORDER-001");
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setPaymentMethod("CREDIT_CARD");
        
        when(paymentRepository.existsByOrderId(anyString())).thenReturn(true);

        // When & Then
        DuplicatePaymentException exception = assertThrows(DuplicatePaymentException.class, () -> {
            paymentService.createPayment(paymentRequest);
        });
        
        assertNotNull(exception);
        verify(paymentRepository).existsByOrderId("ORDER-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_Success() {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId("ORDER-001");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.PENDING);
        
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest();
        request.setStatus(PaymentStatus.SUCCEEDED);
        request.setProviderRef("PAY_123456");

        Payment updatedPayment = new Payment();
        updatedPayment.setId(1L);
        updatedPayment.setOrderId("ORDER-001");
        updatedPayment.setAmount(new BigDecimal("100.00"));
        updatedPayment.setStatus(PaymentStatus.SUCCEEDED);
        updatedPayment.setProviderRef("PAY_123456");
        updatedPayment.setCreatedAt(LocalDateTime.now());
        updatedPayment.setUpdatedAt(LocalDateTime.now());

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        // When
        PaymentResponse response = paymentService.updatePaymentStatus(1L, request);

        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCEEDED, response.getStatus());
        assertEquals("PAY_123456", response.getProviderRef());

        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
        verify(kafkaTemplate).send(eq("payment.succeeded"), eq("ORDER-001"), any());
    }

    @Test
    void updatePaymentStatus_PaymentNotFound() {
        // Given
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest();
        request.setStatus(PaymentStatus.SUCCEEDED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.updatePaymentStatus(1L, request);
        });
        
        assertNotNull(exception);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPayment_Success() {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId("ORDER-001");
        payment.setAmount(new BigDecimal("100.00"));
        
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPayment(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORDER-001", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPayment_NotFound() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.getPayment(1L);
        });
        
        assertNotNull(exception);
        verify(paymentRepository).findById(1L);
    }

    @Test
    void getPaymentByOrderId_Success() {
        // Given
        Payment foundPayment = new Payment();
        foundPayment.setId(1L);
        foundPayment.setOrderId("ORDER-001");
        foundPayment.setAmount(new BigDecimal("100.00"));
        foundPayment.setStatus(PaymentStatus.PENDING);
        
        when(paymentRepository.findByOrderId("ORDER-001")).thenReturn(Optional.of(foundPayment));

        // When
        PaymentResponse response = paymentService.getPaymentByOrderId("ORDER-001");

        // Then
        assertNotNull(response);
        assertEquals("ORDER-001", response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());

        verify(paymentRepository).findByOrderId("ORDER-001");
    }

    @Test
    void refundPayment_Success() {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId("ORDER-001");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.SUCCEEDED);
        
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");

        Payment refundedPayment = new Payment();
        refundedPayment.setId(1L);
        refundedPayment.setOrderId("ORDER-001");
        refundedPayment.setAmount(new BigDecimal("100.00"));
        refundedPayment.setStatus(PaymentStatus.REFUNDED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentProcessor.processRefund(any(Payment.class), any(BigDecimal.class))).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedPayment);

        // When
        PaymentResponse response = paymentService.refundPayment(1L, refundRequest);

        // Then
        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.getStatus());

        verify(paymentRepository).findById(1L);
        verify(paymentProcessor).processRefund(any(Payment.class), eq(new BigDecimal("50.00")));
        verify(paymentRepository).save(any(Payment.class));
        verify(kafkaTemplate).send(eq("payment.refunded"), eq("ORDER-001"), any());
    }

    @Test
    void refundPayment_InvalidState() {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);
        
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(new BigDecimal("50.00"));
        refundRequest.setReason("Customer request");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        InvalidPaymentStateException exception = assertThrows(InvalidPaymentStateException.class, () -> {
            paymentService.refundPayment(1L, refundRequest);
        });
        
        assertNotNull(exception);
        verify(paymentRepository).findById(1L);
        verify(paymentProcessor, never()).processRefund(any(Payment.class), any(BigDecimal.class));
    }

    @Test
    void refundPayment_ExceedsOriginalAmount() {
        // Given
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.SUCCEEDED);
        
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(new BigDecimal("150.00")); // More than original 100.00
        refundRequest.setReason("Customer request");

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When & Then
        InvalidPaymentStateException exception = assertThrows(InvalidPaymentStateException.class, () -> {
            paymentService.refundPayment(1L, refundRequest);
        });
        
        assertNotNull(exception);

        verify(paymentRepository).findById(1L);
        verify(paymentProcessor, never()).processRefund(any(Payment.class), any(BigDecimal.class));
    }
}