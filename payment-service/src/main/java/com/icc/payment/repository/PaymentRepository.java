package com.icc.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.icc.payment.entity.Payment;
import com.icc.payment.entity.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByOrderIdAndStatus(String orderId, PaymentStatus status);
    boolean existsByOrderId(String orderId);
}