# ICC Project Survival Guide - Minimum Viable Understanding

## Reality Check

**You have an AI-generated project you don't understand.**
**You need to defend it in interviews in 1-2 weeks.**
**You can't learn everything, but you can learn enough to survive.**

This guide focuses on the **20% of knowledge that answers 80% of interview questions.**

---

## Week 1: Core Concepts Only

### Day 1-2: Spring Boot Basics (8-10 hours)

**Goal:** Understand what Spring Boot is and how your project uses it

#### Learn These Concepts:
1. **What is Spring Boot?**
   - Framework for building Java applications
   - "Convention over configuration" - sensible defaults
   - Your project: 7 Spring Boot microservices

2. **Key Annotations You MUST Know:**
   ```java
   @RestController    // This class handles HTTP requests
   @Service          // This class contains business logic
   @Repository       // This class talks to database
   @Autowired        // Spring automatically provides this dependency
   @GetMapping       // Handle GET requests
   @PostMapping      // Handle POST requests
   ```

3. **How a Request Works:**
   ```
   HTTP Request → @RestController → @Service → @Repository → Database
                  ↓
   HTTP Response ← Return JSON    ← Business Logic ← Query Data
   ```

#### Practice Task:
1. Open `payment-service/src/main/java/com/icc/payment/controller/PaymentController.java`
2. Find one endpoint (e.g., `createPayment`)
3. Trace the flow:
   - Controller receives request
   - Calls PaymentService
   - Service calls PaymentRepository
   - Repository saves to database
4. **Explain this flow out loud 10 times**

#### Interview Question Practice:
- Q: "What is Spring Boot?"
  - A: "It's a Java framework that simplifies building web applications. It provides auto-configuration and embedded servers. My project uses it for all 7 microservices."

- Q: "What's the difference between @Service and @Controller?"
  - A: "Controllers handle HTTP requests and responses. Services contain business logic. In my payment service, PaymentController receives the request, PaymentService processes the payment logic."

#### Resources (Pick ONE):
- YouTube: "Spring Boot Tutorial for Beginners" (any popular video)
- Don't spend more than 4 hours watching - spend time DOING

---

### Day 3-4: Database & JPA (8-10 hours)

**Goal:** Understand how your code talks to databases

#### Learn These Concepts:

1. **What is JPA?**
   - Java Persistence API - translates Java objects to database tables
   - You write Java, JPA generates SQL

2. **Key Annotations:**
   ```java
   @Entity           // This class maps to a database table
   @Id              // This field is the primary key
   @Column          // This field is a database column
   @OneToMany       // One record can have many related records
   @ManyToOne       // Many records can relate to one record
   ```

3. **Repository Pattern:**
   ```java
   public interface PaymentRepository extends JpaRepository<Payment, Long> {
       boolean existsByOrderId(String orderId);  // JPA generates SQL automatically
   }
   ```

4. **Your Databases:**
   - MySQL: Payments, Accounts
   - PostgreSQL: Authentication
   - MongoDB: Items (flexible schema)
   - Cassandra: Orders, Inventory (high-throughput)

#### Practice Task:
1. Open `payment-service/src/main/java/com/icc/payment/entity/Payment.java`
2. Understand each annotation:
   - `@Entity` → This becomes a `payments` table
   - `@Id` → Primary key
   - `@Column(nullable = false, unique = true)` → Must have value, no duplicates
3. Open `init/mysql/001_init.sql`
4. Match Java fields to SQL columns
5. **Explain this mapping out loud**

#### Interview Question Practice:
- Q: "How do you interact with the database?"
  - A: "I use Spring Data JPA. My entity classes map to database tables, and repository interfaces handle queries. For example, Payment entity maps to payments table, and PaymentRepository provides methods like existsByOrderId() which JPA automatically implements."

- Q: "Why use different databases?"
  - A: "Different data patterns require different databases. MySQL for structured financial data (ACID transactions), MongoDB for flexible item catalog, Cassandra for high-throughput order processing with eventual consistency."

#### Resources:
- YouTube: "JPA Tutorial" or "Hibernate Tutorial"
- Focus on understanding @Entity, @Id, and basic queries

---

### Day 5-6: REST APIs & HTTP (6-8 hours)

**Goal:** Understand how HTTP requests work

#### Learn These Concepts:

1. **HTTP Methods:**
   - GET: Retrieve data (read-only)
   - POST: Create new resource
   - PUT: Update existing resource
   - DELETE: Remove resource

2. **HTTP Status Codes:**
   - 200: Success
   - 201: Created
   - 400: Bad request (client error)
   - 404: Not found
   - 500: Server error

3. **Request/Response Flow:**
   ```java
   @PostMapping("/api/payments")
   public ResponseEntity<PaymentDTO> createPayment(@RequestBody CreatePaymentRequest request) {
       // @RequestBody: Convert JSON to Java object
       // ResponseEntity: HTTP response with status code
       PaymentDTO payment = paymentService.createPayment(request);
       return ResponseEntity.status(HttpStatus.CREATED).body(payment);
       // Returns 201 Created with payment JSON
   }
   ```

#### Practice Task:
1. Use Postman or curl to test your APIs:
   ```bash
   # Create payment
   curl -X POST http://localhost:8082/api/payments \
     -H "Content-Type: application/json" \
     -d '{"orderId": "TEST-001", "amount": 100, "customerId": "CUST-001"}'

   # Get payment
   curl http://localhost:8082/api/payments/1
   ```

2. Watch the logs to see the request flow
3. Change the request body, see what happens
4. Try invalid data, see the error response

#### Interview Question Practice:
- Q: "Explain the difference between POST and PUT"
  - A: "POST creates a new resource, PUT updates an existing one. In my payment service, POST /api/payments creates a new payment. PUT /api/payments/{id} would update a payment status."

- Q: "What happens when you call your create payment API?"
  - A: "The client sends a POST request with JSON body. PaymentController receives it, converts JSON to CreatePaymentRequest object using @RequestBody, calls PaymentService to process it, saves to database, and returns 201 Created with the payment DTO."

---

### Day 7: Security & JWT (6-8 hours)

**Goal:** Understand how authentication works

#### Learn These Concepts:

1. **What is JWT?**
   - JSON Web Token - like a concert wristband
   - Proves you're authenticated without server storing session
   - Contains user info, signed so it can't be forged

2. **JWT Flow:**
   ```
   1. User logs in with username/password
   2. Server validates credentials
   3. Server generates JWT token (signed)
   4. Client stores token
   5. Client sends token with each request (Authorization header)
   6. Server validates token signature
   7. Server extracts user info from token
   ```

3. **Your Implementation:**
   - Auth-service generates JWT tokens
   - Other services validate JWT tokens
   - Spring Security handles authentication

#### Practice Task:
1. Test login flow:
   ```bash
   # Login
   curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "testuser", "password": "password"}'

   # Copy the token from response
   # Use token in subsequent requests
   curl http://localhost:8082/api/payments \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

2. Try without token → Should get 401 Unauthorized
3. Try with invalid token → Should get 403 Forbidden

#### Interview Question Practice:
- Q: "How does authentication work in your project?"
  - A: "I use JWT tokens. User logs in through auth-service with username/password. If valid, server returns a JWT token. Client includes this token in Authorization header for subsequent requests. Other services validate the token using the shared secret key."

- Q: "Why JWT instead of sessions?"
  - A: "JWT is stateless - the server doesn't need to store session data. This scales better in microservices because any service can validate the token without calling auth service. The token contains all necessary user information."

---

## Week 2: Your Project Specifics

### Day 8-9: Microservices Architecture (8-10 hours)

**Goal:** Explain why you split into multiple services

#### Your 7 Services:
1. **Gateway**: Entry point, routes requests
2. **Auth-Service**: Login, token generation
3. **Account-Service**: User account management
4. **Item-Service**: Product catalog
5. **Inventory-Service**: Stock management
6. **Order-Service**: Order processing
7. **Payment-Service**: Payment handling

#### Why Microservices?
- **Separation of concerns**: Each service does one thing
- **Independent scaling**: Scale payment-service without scaling item-service
- **Technology flexibility**: Different databases for different needs
- **Team autonomy**: Different teams can work on different services

#### Practice Task:
1. Draw the architecture on paper:
   ```
   Client → Gateway → [Auth, Account, Item, Inventory, Order, Payment]
   ```

2. Trace one complete flow (order creation):
   ```
   1. Client → Gateway → Order-Service (create order)
   2. Order-Service → Item-Service (validate item exists)
   3. Order-Service → Inventory-Service (check stock)
   4. Order-Service → Inventory-Service (reserve stock)
   5. Order-Service → Database (save order)
   6. Return order to client
   ```

#### Interview Question Practice:
- Q: "Why did you use microservices?"
  - A: "To separate concerns and enable independent scaling. Payment processing has different scaling needs than product catalog. Also, it allows using different databases - Cassandra for high-throughput orders, MongoDB for flexible product schemas."

- Q: "What are the downsides of microservices?"
  - A: "Increased complexity - more services to deploy and monitor. Distributed transactions are harder. Network calls between services add latency. Need proper logging and tracing to debug issues."

---

### Day 10-11: Your Key Features (8-10 hours)

**Goal:** Explain the 3 features your instructor asked about

#### Feature 1: Async Processing

**Memorize This:**
```
Q: "How is async processing implemented?"

A: "I use Spring's @Async annotation on the PaymentProcessor.processPayment() method. 
When a payment is created, the API returns immediately with PENDING status. 
The actual payment processing runs in a background thread pool for 2 seconds. 
Once complete, it publishes a Kafka event to notify the order service. 
This prevents the HTTP thread from blocking, improving throughput."

SHOW CODE:
1. AppConfig.java line 9: @EnableAsync
2. PaymentProcessor.java line 32: @Async method
3. Demo: Create payment, see immediate response
```

#### Feature 2: Race Condition Prevention

**Memorize This:**
```
Q: "How do you prevent race conditions?"

A: "I use defense-in-depth with multiple layers:

Layer 1 - Application check: Before creating a payment, I check if one already exists 
for that order ID using existsByOrderId(). This rejects duplicates quickly.

Layer 2 - Database constraint: The payments table has a unique index on order_id. 
Even if two concurrent requests pass the application check, the database guarantees 
only one INSERT succeeds.

Layer 3 - Transactions: Inventory operations use @Transactional to make 
check-and-reserve atomic. This prevents overselling.

Layer 4 - State machine: Orders have explicit state transitions. A CANCELLED order 
can't become PAID even if a late payment event arrives.

SHOW CODE:
1. PaymentService.java line 41: existsByOrderId check
2. init/mysql/001_init.sql: UNIQUE INDEX on order_id
3. InventoryService.java line 49: @Transactional
4. OrderService.java line 145: isValidStatusTransition
```

#### Feature 3: Service Communication

**Memorize This:**
```
Q: "How do services communicate?"

A: "I use a hybrid approach:

Synchronous (OpenFeign): When I need immediate validation. For example, when creating 
an order, I call item-service to verify the product exists and inventory-service to 
check stock. The order creation depends on these responses.

Asynchronous (Kafka): For event notifications where eventual consistency is acceptable. 
When payment succeeds, payment-service publishes a Kafka event. Order-service consumes 
this event and updates order status. This decouples the services.

Why hybrid? Balance between consistency and performance. Item validation needs strong 
consistency (can't create order for non-existent item). Payment status updates can be 
eventual (order shows as PENDING for a few milliseconds before updating to PAID).

SHOW CODE:
1. OrderService.java: itemServiceClient.getItemBySku() - OpenFeign
2. PaymentProcessor.java: kafkaTemplate.send() - Kafka producer
3. PaymentEventListener.java: @KafkaListener - Kafka consumer
```

#### Practice Task:
1. Write these answers on paper WITHOUT looking
2. Practice saying them out loud
3. Have someone quiz you
4. Time yourself - each answer should be 30-60 seconds

---

### Day 12-13: Testing & Demo Prep (8-10 hours)

**Goal:** Be able to demonstrate working features

#### Prepare These Demos:

**Demo 1: Show Async Works**
```bash
# Terminal 1: Watch logs
docker compose logs -f payment-service

# Terminal 2: Create payment
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId": "DEMO-001", "amount": 100, "customerId": "CUST-001"}'

# Point out:
# - API responds immediately (< 100ms)
# - Log shows "Processing in thread: task-1" (background thread)
# - After 2 seconds, status updates to SUCCEEDED
# - Kafka event is published
```

**Demo 2: Show Race Condition Prevention**
```bash
# Create first payment
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId": "DEMO-002", "amount": 100, "customerId": "CUST-001"}'

# Try duplicate (should fail)
curl -X POST http://localhost:8082/api/payments \
  -H "Content-Type: application/json" \
  -d '{"orderId": "DEMO-002", "amount": 100, "customerId": "CUST-001"}'

# Response: 400 Bad Request - "Payment already exists for order: DEMO-002"
```

**Demo 3: Show Kafka Event Flow**
```bash
# Terminal 1: Watch order-service logs
docker compose logs -f order-service

# Terminal 2: Create order, then payment
# Watch Terminal 1 for "Received payment.succeeded event" message
# Watch order status change from PENDING_PAYMENT to PAID
```

#### Practice:
1. Run each demo 5 times until smooth
2. Narrate what's happening as you demo
3. Prepare for "what if" questions:
   - "What if Kafka is down?" → Events are persisted, will retry
   - "What if database is down?" → Service returns 500 error, no partial data
   - "What if two orders reserve same inventory?" → @Transactional prevents it

---

### Day 14: Mock Interviews (6-8 hours)

**Goal:** Practice under pressure

#### Find a Study Partner
- ICC classmate
- Friend with tech background
- Even non-tech friend reading questions

#### Mock Interview Script:

**Round 1: Technical Questions (30 mins)**
- Explain your architecture
- Walk through order creation flow
- Explain async processing
- Explain race condition prevention
- Explain service communication

**Round 2: Code Review (30 mins)**
- Open PaymentService.java
- Explain each method
- Answer "what if" questions
- Make a small modification (e.g., change timeout)

**Round 3: Demo (15 mins)**
- Show the application running
- Demonstrate async processing
- Demonstrate race condition prevention
- Show Kafka events

**Round 4: Behavioral (15 mins)**
- "Why microservices instead of monolith?"
- "What was the hardest part of this project?"
- "What would you improve?"
- "How did you handle challenges?"

#### Record Yourself:
- Watch for filler words ("um", "like", "I think")
- Check for confidence (don't sound unsure)
- Time your answers (not too short, not too long)
- Note questions you struggled with

---

## Minimum Viable Answers (Memorize These)

### "Walk me through your project architecture"
> "I built a microservices-based shopping platform with 7 services: gateway for routing, auth for JWT tokens, account for users, item for products, inventory for stock, order for order processing, and payment for transactions. Services communicate via OpenFeign for synchronous calls and Kafka for async events. I use 4 different databases - MySQL for transactions, PostgreSQL for auth, MongoDB for flexible schemas, and Cassandra for high-throughput operations. Everything runs in Docker containers."

### "What's the tech stack?"
> "Spring Boot 3.3.4 for all services, Spring Cloud for microservices features, Spring Security with JWT for authentication, JPA for database access, Apache Kafka for event streaming, and Docker Compose for orchestration. Java 21, Maven for builds."

### "How do you ensure data consistency?"
> "Multiple layers: application-level idempotency checks, database unique constraints, transactional boundaries with @Transactional, state machine validation for order status, and event sourcing for audit trails. For example, payments have both an application check for duplicates and a database unique constraint on order_id."

### "How do you handle failures?"
> "Validation at API level returns 400 errors immediately. Database errors return 500. Kafka events are persisted and retried if consumers are down. OpenFeign calls have timeouts. Inventory uses compensating transactions - if order is cancelled, we publish an event to release reserved stock."

### "What would you improve?"
> "Add circuit breakers for resilience when calling other services. Implement distributed tracing with Sleuth and Zipkin for debugging. Add API rate limiting. Improve test coverage - currently at 30% for most services, could aim for 70%+. Add health checks for better monitoring."

---

## Red Flags to Avoid

### Don't Say:
❌ "I don't know why that's there"
❌ "AI generated that part"
❌ "I didn't implement that feature"
❌ "I'm not sure how it works"
❌ "I followed a tutorial"
❌ "Someone helped me with that"

### Do Say:
✅ "Let me show you the exact code"
✅ "I implemented this because..."
✅ "I tested this by..."
✅ "The trade-off here is..."
✅ "I chose this approach because..."
✅ "If we changed X, then Y would happen"

---

## Emergency Cheat Sheet (Print This)

**Async Processing:**
- Config: @EnableAsync in AppConfig
- Method: @Async on processPayment()
- Returns: CompletableFuture
- Benefit: Non-blocking, better throughput

**Race Conditions:**
- Layer 1: existsByOrderId() check
- Layer 2: Database UNIQUE constraint
- Layer 3: @Transactional for atomicity
- Layer 4: State machine validation

**Communication:**
- Sync: OpenFeign for validation
- Async: Kafka for events
- Why: Balance consistency vs performance

**Databases:**
- MySQL: Payments, Accounts (ACID)
- PostgreSQL: Auth (relational)
- MongoDB: Items (flexible)
- Cassandra: Orders, Inventory (high-throughput)

**Security:**
- JWT tokens for authentication
- Auth-service generates tokens
- Other services validate tokens
- Token in Authorization header

---

## The Honest Truth

### What This Plan WON'T Do:
❌ Make you an expert
❌ Let you answer every possible question
❌ Teach you everything about Spring/Java/Microservices

### What This Plan WILL Do:
✅ Give you enough knowledge to survive most interviews
✅ Let you explain the core features confidently
✅ Provide memorized answers for common questions
✅ Give you working demos to fall back on

### The Risk:
If an interviewer goes deep on something outside these topics, you're toast. But at least you'll have a fighting chance.

---

## Your Schedule (Starting Today)

```
Day 1 (Today):   Spring Boot basics (8 hours)
Day 2:           Spring Boot practice (6 hours)
Day 3:           Database & JPA (8 hours)
Day 4:           Database practice (6 hours)
Day 5:           REST APIs & HTTP (6 hours)
Day 6:           Security & JWT (6 hours)
Day 7:           Rest day (review notes)
Day 8:           Microservices concepts (8 hours)
Day 9:           Architecture practice (6 hours)
Day 10:          Your 3 key features (8 hours)
Day 11:          Memorize answers (6 hours)
Day 12:          Demo prep (8 hours)
Day 13:          Demo practice (6 hours)
Day 14:          Mock interviews (8 hours)

Total: ~90 hours over 2 weeks
```

---

## Start NOW

**Close this document. Do this:**

1. Open YouTube
2. Search "Spring Boot Tutorial for Beginners"
3. Watch ONE video (pick most popular, under 2 hours)
4. Take notes on key concepts
5. Open your PaymentController.java
6. Identify: @RestController, @Autowired, @PostMapping
7. Trace ONE request from controller → service → repository

**Don't overthink. Just start learning.**

You have 2 weeks. It's doable if you commit.

加油！💪
