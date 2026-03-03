# Implementation Guide - How Features Actually Work

## You Already Know: Java & Spring Basics
## You Need to Learn: How These 4 Features Are Implemented

---

## 1. Async Processing Implementation

### What You See in Your Code:

**Step 1: Enable Async (`AppConfig.java`)**
```java
@Configuration
@EnableAsync  // ← This creates a ThreadPoolTaskExecutor behind the scenes
public class AppConfig {
}
```

**What `@EnableAsync` does:**
- Creates default thread pool: `SimpleAsyncTaskExecutor`
- Intercepts methods marked with `@Async`
- Wraps them in `CompletableFuture` execution
- Returns immediately to caller, method continues in background

**Step 2: Mark Method as Async (`PaymentProcessor.java`)**
```java
@Async  // ← Spring runs this in a separate thread from thread pool
public CompletableFuture<Void> processPayment(Payment payment) {
    Thread.sleep(2000);  // This delay happens in BACKGROUND, not in HTTP thread
    
    // ... payment logic
    
    return CompletableFuture.completedFuture(null);  // ← Must return CompletableFuture
}
```

### The Execution Flow:

```
Thread: http-nio-8082-exec-1 (HTTP Request Handler)
├─ PaymentController.createPayment() starts
├─ PaymentService.createPayment() starts
│  ├─ Save payment with status=PENDING
│  └─ paymentProcessor.processPayment(payment)  ← Method called
│     └─ @Async interception: Wraps call, submits to thread pool
├─ Returns immediately (API response sent)
└─ HTTP thread freed for next request

Thread: task-1 (Async Thread Pool)
└─ processPayment() continues here
   ├─ Thread.sleep(2000)  ← Blocks THIS thread, not HTTP thread
   ├─ Update status to SUCCEEDED/FAILED
   ├─ Save to database
   └─ Publish Kafka event
```

### Test It Right Now:

```bash
# Add this logging to see thread names
logger.info("HTTP Thread: {}", Thread.currentThread().getName());
# Will print: HTTP Thread: http-nio-8082-exec-1

# In PaymentProcessor.processPayment()
logger.info("Async Thread: {}", Thread.currentThread().getName());
# Will print: Async Thread: task-1 (or task-2, task-3, etc.)
```

### Common Questions:

**Q: What if I remove `@EnableAsync`?**
- The method runs synchronously in the HTTP thread
- API response takes 2+ seconds instead of < 100ms
- Thread pool is never used

**Q: What if method doesn't return `CompletableFuture`?**
- Still works, but you can't await the result
- Spring wraps it automatically if return type is void

**Q: How many threads in the pool?**
- Default: Unlimited (creates new thread per task)
- Can configure custom pool:
```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);      // Minimum threads
    executor.setMaxPoolSize(10);      // Maximum threads
    executor.setQueueCapacity(100);   // Queue size before rejection
    executor.initialize();
    return executor;
}

@Async("taskExecutor")  // Use custom pool
public CompletableFuture<Void> processPayment(Payment payment) { ... }
```

---

## 2. Race Condition Prevention (Locking & Idempotency)

### Layer 1: Application-Level Check

**Your Code (`PaymentService.java` line 41):**
```java
if (paymentRepository.existsByOrderId(request.getOrderId())) {
    throw new DuplicatePaymentException("Payment already exists for order");
}
```

**What happens:**
```
Request 1: existsByOrderId("ORD-123") → Query DB → false → Continue
Request 2: existsByOrderId("ORD-123") → Query DB → false (race!) → Continue
```

**Problem:** Both requests pass the check! Need Layer 2...

### Layer 2: Database Unique Constraint

**Your Schema (`init/mysql/001_init.sql`):**
```sql
CREATE TABLE payments (
    order_id VARCHAR(64) NOT NULL UNIQUE,  -- ← Database enforces uniqueness
    UNIQUE INDEX ux_pay_idem (order_id)
);
```

**What happens:**
```
Request 1: INSERT INTO payments (order_id='ORD-123') → SUCCESS
Request 2: INSERT INTO payments (order_id='ORD-123') → DUPLICATE KEY ERROR
```

**Result:** Database guarantees only one payment, even if both pass app check

### Layer 3: @Transactional for Atomic Operations

**Your Code (`InventoryService.java`):**
```java
@Transactional  // ← Database transaction wraps this entire method
public boolean reserveInventory(ReserveInventoryRequest request) {
    // 1. Read
    Inventory inventory = inventoryRepository.findBySku(request.getSku()).orElseThrow();
    
    // 2. Check
    if (inventory.getAvailable() < request.getQuantity()) {
        return false;
    }
    
    // 3. Modify
    inventory.setAvailable(inventory.getAvailable() - request.getQuantity());
    inventory.setReserved(inventory.getReserved() + request.getQuantity());
    
    // 4. Write
    inventoryRepository.save(inventory);  // ← All or nothing
    
    return true;
}
```

**What `@Transactional` does:**
1. Begins database transaction: `BEGIN TRANSACTION`
2. Executes all database operations
3. If success: `COMMIT` (all changes permanent)
4. If exception: `ROLLBACK` (all changes reverted)
5. **Locks the row** during transaction (other transactions wait)

**Without @Transactional:**
```
Thread 1: Read available=5
Thread 2: Read available=5 (race!)
Thread 1: Set available=3 (reserved 2)
Thread 2: Set available=3 (reserved 2) ← OVERWRITES Thread 1!
Result: Only 2 reserved but 4 actually taken (DATA CORRUPTION)
```

**With @Transactional:**
```
Thread 1: BEGIN → Read available=5 (ROW LOCKED)
Thread 2: BEGIN → Read available=5 (BLOCKED, waiting for lock)
Thread 1: Set available=3 → COMMIT (lock released)
Thread 2: Read available=3 (now sees updated value) → Set available=1 → COMMIT
Result: 4 total reserved, data is consistent
```

### Test It Right Now:

**Test 1: Duplicate Payment Prevention**
```bash
# Terminal 1:
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"TEST-RACE-001","amount":100,"customerId":"C1"}'

# Terminal 2 (run IMMEDIATELY):
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"TEST-RACE-001","amount":100,"customerId":"C1"}'

# Expected: First succeeds (201), second fails (400 - DuplicatePaymentException)
```

**Test 2: See Transaction Locking**
```bash
# Check inventory before
docker compose exec -T cassandra cqlsh -e "SELECT * FROM inventory.inventories WHERE sku='LAPTOP-X1';"

# Create order (reserves inventory)
curl -X POST http://localhost:8085/api/orders ...

# Check inventory after (should see available decreased, reserved increased)
```

---

## 3. JWT Authentication Implementation

### How JWT Works in Your Project:

**Step 1: User Logs In (`AuthController.java`)**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 1. Validate username/password against database
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );
    
    // 2. Generate JWT token
    String token = jwtUtil.generateToken(user);
    
    // 3. Return token to client
    return ResponseEntity.ok(new JwtResponse(token));
}
```

**Step 2: Token Generation (`JwtUtil.java`)**
```java
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("username", user.getUsername());
    claims.put("roles", user.getRoles());
    
    return Jwts.builder()
        .setClaims(claims)              // User data
        .setSubject(user.getUsername()) // Principal
        .setIssuedAt(new Date())        // Token created time
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // Sign with secret
        .compact();
}
```

**What the Token Looks Like:**
```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9obiIsInJvbGVzIjpbIlVTRVIiXX0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

Part 1 (Header):     eyJhbGciOiJIUzI1NiJ9
Part 2 (Payload):    eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9obiIsInJvbGVzIjpbIlVTRVIiXX0
Part 3 (Signature):  SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Step 3: Client Sends Token with Requests**
```http
GET /api/payments HTTP/1.1
Host: localhost:8082
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJu...
```

**Step 4: Token Validation (`JwtAuthenticationFilter.java`)**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        // 1. Extract token from Authorization header
        String token = extractTokenFromHeader(request);
        
        // 2. Validate token signature
        if (token != null && jwtUtil.validateToken(token)) {
            
            // 3. Extract user info from token
            String username = jwtUtil.getUsernameFromToken(token);
            
            // 4. Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 5. Set authentication in Spring Security context
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        // 6. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

**Step 5: Token Validation Logic (`JwtUtil.java`)**
```java
public boolean validateToken(String token) {
    try {
        // Parse token and verify signature
        Jwts.parser()
            .setSigningKey(SECRET_KEY)  // Use same secret key
            .parseClaimsJws(token);     // Throws exception if invalid
        
        // Check expiration
        Date expiration = getExpirationFromToken(token);
        return !expiration.before(new Date());
        
    } catch (JwtException e) {
        return false;  // Invalid signature or malformed token
    }
}
```

### Security Flow:

```
1. Client → POST /api/auth/login (username, password)
2. Auth Service validates credentials
3. Auth Service generates JWT token (signed with SECRET_KEY)
4. Client stores token (localStorage, cookie, etc.)

--- Later Requests ---

5. Client → GET /api/payments (Authorization: Bearer TOKEN)
6. Payment Service receives request
7. JwtAuthenticationFilter intercepts
8. Extract token from header
9. Validate signature using SECRET_KEY
10. Parse claims (userId, username, roles)
11. Set authentication in SecurityContext
12. Controller method executes with authenticated user
```

### Why JWT vs Sessions:

**Sessions (Traditional):**
```
Client → Login → Server creates session → Store in Redis/Database
Client → Request → Server looks up session in Redis/Database
```
- ❌ Requires shared session store (Redis)
- ❌ Microservices all need to query same store
- ❌ State on server side

**JWT (Stateless):**
```
Client → Login → Server generates token (no storage)
Client → Request → Server validates signature (no database call)
```
- ✅ No shared storage needed
- ✅ Each service validates independently
- ✅ Stateless (scales better)

### Test It Right Now:

```bash
# 1. Login and get token
TOKEN=$(curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}' \
  | jq -r '.token')

echo $TOKEN  # Should see the JWT token

# 2. Use token to access protected endpoint
curl http://localhost:8082/api/payments \
  -H "Authorization: Bearer $TOKEN"

# 3. Try without token (should fail)
curl http://localhost:8082/api/payments
# Expected: 401 Unauthorized

# 4. Try with invalid token (should fail)
curl http://localhost:8082/api/payments \
  -H "Authorization: Bearer FAKE_TOKEN"
# Expected: 403 Forbidden
```

### Common Questions:

**Q: What if someone modifies the token payload?**
- Signature verification fails because signature is based on payload
- Example: Change `"userId":1` to `"userId":2` → Signature no longer matches

**Q: What if token is stolen?**
- Token is valid until expiration (24 hours in your config)
- Should use HTTPS to prevent interception
- Can implement token blacklist for logout

**Q: Where is SECRET_KEY stored?**
- In `application.yml` or environment variable
- **Must be same across all services** (or they can't validate each other's tokens)

---

## 4. Kafka Communication Implementation

### Kafka Architecture in Your Project:

```
payment-service (Producer)
    ↓ publishes event
Kafka Broker (Topic: payment.succeeded)
    ↓ delivers event
order-service (Consumer)
    ↓ processes event
Update order status to PAID
```

### Producer Side (`PaymentProcessor.java`):

```java
@Autowired
private KafkaTemplate<String, Object> kafkaTemplate;

// Publish event
kafkaTemplate.send(
    "payment.succeeded",        // Topic name
    payment.getOrderId(),       // Key (for partitioning)
    event                       // Value (the event object)
);
```

**What happens:**
1. `kafkaTemplate.send()` serializes event object to JSON
2. Sends to Kafka broker on topic `payment.succeeded`
3. Uses `orderId` as partition key (ensures all events for same order go to same partition)
4. Returns immediately (async, doesn't wait for consumer)

### Consumer Side (`PaymentEventListener.java`):

```java
@Service
public class PaymentEventListener {
    
    @KafkaListener(
        topics = "payment.succeeded",      // Listen to this topic
        groupId = "order-service"          // Consumer group
    )
    public void handlePaymentSucceeded(PaymentEvent event) {
        // 1. Kafka calls this method when event arrives
        logger.info("Received payment succeeded event: {}", event.getOrderId());
        
        // 2. Process the event
        Order order = orderRepository.findByOrderId(event.getOrderId());
        
        // 3. Update order status
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        
        logger.info("Order {} marked as PAID", event.getOrderId());
    }
}
```

**What happens:**
1. Kafka delivers message to consumer group `order-service`
2. Spring deserializes JSON to `PaymentEvent` object
3. Calls `handlePaymentSucceeded()` method
4. Method processes event (updates order)
5. Kafka commits offset (marks message as processed)

### Kafka Configuration (`order-service/application.yml`):

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: kafka:9092         # Kafka broker address
      group-id: order-service               # Consumer group ID
      auto-offset-reset: earliest           # Start from beginning if no offset
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.use.type.headers: false           # ← CRITICAL FIX
        spring.json.value.default.type: com.icc.orderservice.event.PaymentEvent
```

**Why these configs matter:**

**Problem without `use.type.headers: false`:**
```
payment-service sends: PaymentEvent class = com.icc.payment.dto.PaymentEvent
order-service expects: PaymentEvent class = com.icc.orderservice.event.PaymentEvent
Result: ClassNotFoundException (classes don't match)
```

**Solution:**
```yaml
spring.json.use.type.headers: false  # Ignore producer's class name
spring.json.value.default.type: com.icc.orderservice.event.PaymentEvent  # Use local class
```

### Partition & Consumer Groups:

**Topics and Partitions:**
```
Topic: payment.succeeded
├─ Partition 0: [event1, event4, event7]
├─ Partition 1: [event2, event5, event8]
└─ Partition 2: [event3, event6, event9]
```

**Consumer Group:**
```
Consumer Group: order-service
├─ Consumer Instance 1 → reads Partition 0, 1
└─ Consumer Instance 2 → reads Partition 2
```

**Key Guarantees:**
- Events with same key (orderId) go to same partition
- Events in same partition are processed in order
- Only one consumer in group processes each message
- If consumer dies, another picks up its partitions

### Test It Right Now:

**Watch Kafka Events Live:**
```bash
# Terminal 1: Start console consumer to see all events
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment.succeeded \
  --from-beginning

# Terminal 2: Create and process payment
curl -X POST http://localhost:8082/api/payments ...

# Terminal 3: Watch order-service logs
docker compose logs -f order-service

# You should see:
# Terminal 1: Raw JSON event
# Terminal 3: "Received payment succeeded event: ORD-123"
#            "Order ORD-123 marked as PAID"
```

**List All Topics:**
```bash
docker compose exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Should see:
# payment.succeeded
# payment.failed
# payment.refunded
# order.created
# order.cancelled
```

### Common Questions:

**Q: What if order-service is down when event is sent?**
- Event is stored in Kafka (persisted to disk)
- When order-service starts, it reads from last committed offset
- Processes all missed events

**Q: What if same event is delivered twice?**
- Kafka guarantees "at-least-once" delivery
- Consumer must be idempotent (safe to process twice)
- Your code: Updating order from PAID to PAID is safe (idempotent)

**Q: Why not just call order-service API directly?**
- Loose coupling: payment-service doesn't need to know about order-service
- Resilience: If order-service is down, event is queued
- Async: payment-service doesn't wait for response
- Multiple consumers: Can add notification-service later without changing payment-service

---

## Quick Practice Tasks (Do These NOW)

### Task 1: Verify Async (15 mins)
```bash
# Add logging to see thread names
# In PaymentService.createPayment():
logger.info("Creating payment in thread: {}", Thread.currentThread().getName());

# In PaymentProcessor.processPayment():
logger.info("Processing payment in thread: {}", Thread.currentThread().getName());

# Restart payment-service
docker compose restart payment-service

# Create payment, watch logs
curl -X POST http://localhost:8082/api/payments ...

# Should see TWO different thread names (http-nio vs task)
```

### Task 2: Test Race Condition (10 mins)
```bash
# Try duplicate payment
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId":"RACE-TEST","amount":100,"customerId":"C1"}'

# Run same command again immediately
# Should fail with "Payment already exists"

# Check database - should only have ONE payment
docker compose exec mysql mysql -uroot -proot -e \
  "SELECT * FROM payment.payments WHERE order_id='RACE-TEST';"
```

### Task 3: JWT Flow (15 mins)
```bash
# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}'

# Copy token, use it
curl http://localhost:8082/api/payments \
  -H "Authorization: Bearer <TOKEN>"

# Try without token - should fail
curl http://localhost:8082/api/payments
```

### Task 4: Kafka Events (15 mins)
```bash
# Terminal 1: Watch events
docker compose exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic payment.succeeded \
  --from-beginning

# Terminal 2: Trigger event
# Create payment, process it, watch event appear in Terminal 1
```

---

## Your 3-Day Plan

### Day 1: Async & Transactions (4 hours)
- ✅ Morning: Add thread logging, verify async execution
- ✅ Afternoon: Test @Transactional with concurrent requests
- ✅ Practice: Explain async flow out loud 5 times

### Day 2: JWT & Security (4 hours)
- ✅ Morning: Trace JWT generation and validation
- ✅ Afternoon: Test with valid/invalid/expired tokens
- ✅ Practice: Explain JWT flow out loud 5 times

### Day 3: Kafka & Events (4 hours)
- ✅ Morning: Watch live Kafka events
- ✅ Afternoon: Trace event from producer to consumer
- ✅ Practice: Explain Kafka flow out loud 5 times

### Day 4: Mock Demo (2 hours)
- ✅ Do full walkthrough with someone quizzing you
- ✅ Record yourself, watch for weak spots

---

## Interview Prep (Memorize These)

### Async:
**Q: How does @Async work?**
> "@EnableAsync creates a thread pool. When I call a method marked with @Async, Spring intercepts it and submits it to the thread pool. The caller gets a CompletableFuture immediately and continues. The method executes in a background thread. In my PaymentProcessor, the 2-second payment processing happens in the background while the API returns immediately."

### Race Conditions:
**Q: How do you prevent duplicate payments?**
> "Defense in depth. First, application-level check using existsByOrderId(). Second, database unique constraint on order_id column. Even if two concurrent requests pass the app check, the database guarantees only one INSERT succeeds. For inventory, I use @Transactional to make read-check-update atomic with row-level locking."

### JWT:
**Q: How does JWT authentication work?**
> "User logs in, auth-service validates credentials and generates a JWT token signed with a secret key. Token contains user info like userId and roles. Client sends this token in Authorization header with each request. Other services validate the signature using the same secret key. No need to query database or session store - it's stateless. Token expires after 24 hours."

### Kafka:
**Q: Why use Kafka instead of REST calls?**
> "Loose coupling and resilience. When payment succeeds, payment-service publishes an event and continues. It doesn't need to know about order-service or wait for response. If order-service is down, events are queued in Kafka. When it comes back up, it processes all missed events. Also enables multiple consumers - could add notification-service without touching payment-service."

---

## You're Ready When You Can:

✅ Add logging and see different thread names for async
✅ Test duplicate payments and explain both prevention layers
✅ Login, get token, use token, explain signature validation
✅ Watch Kafka console consumer and see your events live
✅ Explain each flow in 30-60 seconds without notes
✅ Answer "what if" questions (what if service is down, what if token is stolen, etc.)

**Start with Task 1 RIGHT NOW. Spend 15 minutes, see async working with your own eyes.**

加油！You know Java and Spring. You just need to see these features in ACTION. 💪
