package com.icc.orderservice.event;

import java.time.LocalDateTime;

public class OrderEvent {
    private String orderId;
    private String userId;
    private String eventType;
    private LocalDateTime timestamp;
    private Object data;

    public OrderEvent() {}

    public OrderEvent(String orderId, String userId, String eventType, Object data) {
        this.orderId = orderId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}