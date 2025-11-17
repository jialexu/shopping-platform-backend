package com.icc.orderservice.event;

public class PaymentEvent {
    private String orderId;
    private String paymentId;
    private String eventType;
    private String status;
    private String message;

    public PaymentEvent() {}

    public PaymentEvent(String orderId, String paymentId, String eventType, String status, String message) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}