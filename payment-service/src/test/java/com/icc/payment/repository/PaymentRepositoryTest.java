package com.icc.payment.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.icc.payment.entity.Payment;
import com.icc.payment.entity.PaymentStatus;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment1;
    private Payment payment2;

    @Test
    void findByOrderId_Success() {
        // Setup test data
        payment1 = new Payment();
        payment1.setOrderId("ORDER-001");
        payment1.setAmount(new BigDecimal("100.00"));
        payment1.setStatus(PaymentStatus.PENDING);
        payment1.setPaymentMethod("CREDIT_CARD");

        payment2 = new Payment();
        payment2.setOrderId("ORDER-002");
        payment2.setAmount(new BigDecimal("200.00"));
        payment2.setStatus(PaymentStatus.SUCCEEDED);
        payment2.setPaymentMethod("DEBIT_CARD");

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
        
        // When
        Optional<Payment> result = paymentRepository.findByOrderId("ORDER-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("ORDER-001", result.get().getOrderId());
        assertEquals(new BigDecimal("100.00"), result.get().getAmount());
    }

    @Test
    void findByOrderId_NotFound() {
        // When
        Optional<Payment> result = paymentRepository.findByOrderId("ORDER-999");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByStatus_Success() {
        // When
        List<Payment> results = paymentRepository.findByStatus(PaymentStatus.PENDING);

        // Then
        assertEquals(1, results.size());
        assertEquals("ORDER-001", results.get(0).getOrderId());
        assertEquals(PaymentStatus.PENDING, results.get(0).getStatus());
    }

    @Test
    void findByOrderIdAndStatus_Success() {
        // When
        List<Payment> results = paymentRepository.findByOrderIdAndStatus("ORDER-001", PaymentStatus.PENDING);

        // Then
        assertEquals(1, results.size());
        assertEquals("ORDER-001", results.get(0).getOrderId());
        assertEquals(PaymentStatus.PENDING, results.get(0).getStatus());
    }

    @Test
    void existsByOrderId_True() {
        // When
        boolean exists = paymentRepository.existsByOrderId("ORDER-001");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByOrderId_False() {
        // When
        boolean exists = paymentRepository.existsByOrderId("ORDER-999");

        // Then
        assertFalse(exists);
    }
}