package com.icc.payment.dto;

import com.icc.payment.entity.PaymentStatus;

public class PaymentStatusUpdateRequest {
    private PaymentStatus status;
    private String providerRef;
    private String description;

    // Constructors
    public PaymentStatusUpdateRequest() {}

    public PaymentStatusUpdateRequest(PaymentStatus status, String providerRef, String description) {
        this.status = status;
        this.providerRef = providerRef;
        this.description = description;
    }

    // Getters and Setters
    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getProviderRef() {
        return providerRef;
    }

    public void setProviderRef(String providerRef) {
        this.providerRef = providerRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}