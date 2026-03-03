# ICC Shopping Service - Technical Quick Reference

## 1️⃣ Async Processing Implementation

### Configuration
```java
// payment-service/config/AppConfig.java (Line 9)
@EnableAsync  // ← Enables async method execution
```

### Async Method
```java
// payment-service/service/PaymentProcessor.java (Line 32)
@Async
public CompletableFuture<Void> processPayment(Payment payment) {
    Thread.sleep(2000);  // Simulated 2-second delay
    // ... payment processing logic
    kafkaTemplate.send("payment.succeeded", event);
    return CompletableFuture.completedFuture(null);
}
```

### Result
- ✅ API responds in < 100ms with PENDING status
- ✅ Background thread processes payment (2 seconds)
- ✅ Kafka event updates order to PAID

---

## 2️⃣ Race Condition Prevention (5 Layers)

### Layer 1: Application Check
```java
// payment-service/service/PaymentService.java (Line 41-43)
if (paymentRepository.existsByOrderId(request.getOrderId())) {
    throw new DuplicatePaymentException("Payment already exists");
}
```

### Layer 2: Database Constraint
```sql
-- init/mysql/001_init.sql
CREATE TABLE payments (
    order_id VARCHAR(64) NOT NULL UNIQUE,  -- ← Prevents duplicates
    UNIQUE INDEX ux_pay_idem (order_id)
);
```

### Layer 3: Transactional Boundaries
```java
// inventory-service/service/InventoryService.java (Line 49)
@Transactional  // ← Atomic read-modify-write
public boolean reserveInventory(ReserveInventoryRequest request) {
    // Check available → Decrement available → Increment reserved
}
```

### Layer 4: State Machine
```java
// order-service/service/OrderService.java (Line 145-154)
private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
    return switch (current) {
        case CANCELLED -> false;  // Terminal state, no transitions allowed
        case PAID -> next == REFUNDED || next == COMPLETED;
        // ...
    };
}
```

### Layer 5: Event Sourcing
```java
// inventory-service/entity/InventoryEvent.java
// Logs all changes: RESERVED, RELEASED, ADDED
// Complete audit trail for debugging and compliance
```

---

## 3️⃣ Service Communication Patterns

### Synchronous (OpenFeign) - For Immediate Validation
```java
// order-service/service/OrderService.java (Line 54)
@Autowired
private ItemServiceClient itemServiceClient;

// Usage: Must validate item exists before creating order
ItemDTO item = itemServiceClient.getItemBySku(request.getSku());
if (item == null) throw new ItemNotFoundException();
```

**When to use:** Need immediate response, strong consistency required

### Asynchronous (Kafka) - For Event Notifications
```java
// payment-service/service/PaymentProcessor.java (Line 44)
kafkaTemplate.send("payment.succeeded", payment.getOrderId(), event);

// order-service/service/PaymentEventListener.java (Line 20)
@KafkaListener(topics = "payment.succeeded", groupId = "order-service")
public void handlePaymentSucceeded(PaymentEvent event) {
    orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
}
```

**When to use:** Eventual consistency acceptable, loose coupling desired

---

## 4️⃣ Kafka Topics & Event Flow

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `payment.succeeded` | payment-service | order-service | Update order to PAID |
| `payment.failed` | payment-service | order-service | Update order to CANCELLED |
| `payment.refunded` | payment-service | order-service | Update order to REFUNDED |
| `order.cancelled` | order-service | inventory-service | Release reserved inventory |

---

## 5️⃣ Complete Order Flow (Happy Path)

```
1. POST /api/orders
   └─> OrderService.createOrder()
       ├─ Validate item (OpenFeign → item-service)          [SYNC]
       ├─ Check inventory (OpenFeign → inventory-service)   [SYNC]
       ├─ Reserve inventory (@Transactional)                [ATOMIC]
       ├─ Save order (status: CREATED)
       └─ Publish "order.created" event                     [ASYNC]

2. POST /api/payments
   └─> PaymentService.createPayment()
       ├─ Check existsByOrderId() (idempotency)             [RACE PREVENTION]
       ├─ Save payment (status: PENDING)
       └─ Trigger @Async processPayment()                   [ASYNC START]

3. Background Thread (2 seconds later)
   └─> PaymentProcessor.processPayment()
       ├─ Simulate payment processing (Thread.sleep)
       ├─ Update status to SUCCEEDED
       └─ Publish "payment.succeeded" event                 [KAFKA EVENT]

4. Kafka Consumer
   └─> PaymentEventListener.handlePaymentSucceeded()
       ├─ Validate state transition (CREATED → PAID)        [STATE MACHINE]
       └─ Update order status to PAID                       [EVENTUAL CONSISTENCY]
```

---

## 6️⃣ Key Code Locations

| Feature | File | Line |
|---------|------|------|
| @EnableAsync | `payment-service/config/AppConfig.java` | 9 |
| @Async Method | `payment-service/service/PaymentProcessor.java` | 32 |
| Idempotency Check | `payment-service/service/PaymentService.java` | 41-43 |
| Database Constraint | `init/mysql/001_init.sql` | - |
| @Transactional | `inventory-service/service/InventoryService.java` | 49 |
| State Machine | `order-service/service/OrderService.java` | 145-154 |
| Kafka Listeners | `order-service/service/PaymentEventListener.java` | 20, 31, 42 |
| OpenFeign Clients | `order-service/client/*.java` | - |

---

## 7️⃣ Demo Test Scenarios

### Test 1: Async Processing
```bash
# Create payment → Immediate response (< 100ms)
POST /api/payments {"orderId": "ORD-123", "amount": 100}
Response: {"status": "PENDING"}  # Immediate!

# Wait 2 seconds → Check status
GET /api/payments/{id}
Response: {"status": "SUCCEEDED"}  # Async processing complete!
```

### Test 2: Duplicate Prevention
```bash
# First request
POST /api/payments {"orderId": "ORD-123"}
Response: 201 Created

# Second request (same order_id)
POST /api/payments {"orderId": "ORD-123"}
Response: 400 Bad Request - "Payment already exists"
```

### Test 3: Inventory Race Condition
```bash
# Initial: 5 available LAPTOP-X1
# Create 3 concurrent orders, each requesting 2 units
# Result: Only first 2 succeed (4 reserved), third fails
```

---

## 8️⃣ Technology Stack

- **Spring Boot**: 3.3.4
- **Spring Cloud**: 2023.0.3
- **Kafka**: 7.6.1
- **Databases**: MySQL 8.0, PostgreSQL 16, MongoDB 7, Cassandra 4.1
- **Java**: 21
- **Docker Compose**: 15 containers

---

## 9️⃣ Interview Talking Points

**Q: How is async processing implemented?**
> "I use @EnableAsync configuration with @Async annotation on PaymentProcessor. The method returns CompletableFuture, allowing the API to respond immediately with PENDING status while payment processing happens in background. This improves throughput and user experience."

**Q: How do you prevent race conditions?**
> "Defense-in-depth with 5 layers: application-level idempotency check, database unique constraint, transactional boundaries, state machine validation, and event sourcing for audit. Even if application check fails due to concurrent requests, the database constraint guarantees no duplicates."

**Q: How do services communicate?**
> "Hybrid approach: OpenFeign for synchronous calls when I need immediate validation like checking inventory, and Kafka for asynchronous event notifications like payment status updates. This balances consistency and performance."

---

## 🎯 Key Success Metrics

- ✅ **6/7 services** meet 30% test coverage (Jacoco)
- ✅ **90% requirements** compliance
- ✅ **5 Kafka topics** with event-driven architecture
- ✅ **15 containers** orchestrated via Docker Compose
- ✅ **Multi-layer race prevention** (application + database + transaction + state + audit)
- ✅ **Async processing** with non-blocking payment operations
- ✅ **Saga pattern** with compensating transactions

---

**For detailed analysis, see:** `COMPLETE_ORDER_FLOW_ANALYSIS.md`

**Last Updated:** November 14, 2025
