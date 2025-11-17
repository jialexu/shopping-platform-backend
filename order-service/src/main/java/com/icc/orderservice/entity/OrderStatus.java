package com.icc.orderservice.entity;

public enum OrderStatus {
    CREATED,
    PENDING_PAYMENT,
    PAID,
    COMPLETED,
    CANCELLED,
    REFUNDED
}