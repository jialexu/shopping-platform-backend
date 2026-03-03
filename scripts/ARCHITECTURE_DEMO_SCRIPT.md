# Architecture Diagram Demo Script
**ICC Final Project - Microservices Shopping Platform**

## 📋 Pre-Demo Checklist
- ✅ Open `architecture-demo.html` in browser
- ✅ Verify services are running: `docker compose ps`
- ✅ Have requirements.md reference ready

---

## 🎯 TAB 1: SYSTEM OVERVIEW (2-3 minutes)

### Opening Statement
*"Good [morning/afternoon]. Today I'll present our ICC Final Project - a complete microservices-based online shopping platform that demonstrates all the technologies and patterns we've learned in class."*

### System Architecture Overview
*"Let me start with the complete system architecture."* **[Show System Overview Tab]**

**Point to Gateway:**
*"At the top, we have our API Gateway running on port 8080, which is built with Spring Cloud Gateway, which is part of the Spring Cloud ecosystem. The Gateway handles all incoming requests and performs JWT token validation before routing to backend services."*

**Point to Services Layer:**
*"Below the gateway, we have 7 microservices. We have:"*
- **Auth Service (port 9000)** - *"Handles JWT token generation and authentication."*
- **Account Service (port 9001)** - *"Manages user accounts with email, username, password, shipping/billing addresses, and payment methods."*
- **Item Service (port 9002)** - *"Handles the product catalog and item metadata."*
- **Inventory Service (port 9003)** - *"Separated from Item Service to handle high-volume stock management operations."*
- **Order Service (port 9004)** - *"Manages the complete order lifecycle from Created → Paid → Completed or Cancelled."*
- **Payment Service (port 9005)** - *"Processes payments with idempotency guarantees to prevent double-charging."*

## Point to Database Layer

### **MySQL**  
*Used by Account Service and Payment Service for transactional data requiring ACID compliance.*

**Why MySQL?**  
- **ACID Guaranteistency (InnoDB)**  
  Ensures every financial update is atomic, consistent, isolated, and durable.  
- **Row-Level Locking**  
  Essential for high-concurrency operations on balances, ledgers, and payment state.  
- **Stable & Predictable Transaction Semantics**  
  Ideal for strict financial correctness.  
- **Mature ecosystem & tooling**  
  Proven reliability for money-related systems.

**Why it's perfect for these services:**  
- Account and payment operations require **absolute correctness**, rollback protection, safe concurrency, and strong referential integrity.  
- MySQL's transactional guarantees map directly to **bank-style operations**.

---

### **PostgreSQL**  
*Used by Auth Service for token-blacklist, session lifecycle, and audit trails.*

**Why PostgreSQL?**  
- **Advanced Indexing (GIN/GiST)**  
  Perfect for fast token lookups and checking if a JWT is revoked.  
- **JSONB + powerful query operators**  
  Flexible for storing variable token metadata, device fingerprints, IP history.  
- **Robust ACID & transactional DDL**  
  Safe for updating auth states (blacklisted tokens, password resets).  
- **Extensions & scheduling (pg_cron)**  
  Useful for routine cleanup of expired sessions.

**Why it's perfect for these services:**  
- Auth workloads require **complex queries, metadata flexibility, and fast lookups** against blacklists—Postgres excels at these patterns.  
- The combination of JSONB + strong SQL = **ideal for authentication and security logic**.

---

### **MongoDB**  
*Used by Item Service because product attributes have flexible and evolving schemas.*

**Why MongoDB?**  
- **Document Model** fits product catalogs naturally  
  Each item stores variants, attributes, descriptions, images, all in one document.  
- **Schema Flexibility**  
  Categories differ; attributes change frequently (electronics vs clothing).  
- **Fast Reads + Rich Secondary Indexes**  
  Supports search, filtering, and dynamic attribute queries efficiently.  
- **Horizontal sharding for massive catalogs**  
  Handles millions of items with minimal operational friction.

**Why it's perfect for these services:**  
- Product catalogs are **heterogeneous and unstructured**, frequently updated, and read-heavy.  
- MongoDB’s schema-less model eliminates migration overhead and allows **rapid iteration** on item data.

---

### **Cassandra**  
*Used by Order Service and Inventory Service for high write throughput and low-latency reads.*

**Why Cassandra?**  
- **Write-optimized storage engine (LSM Trees)**  
  Perfect for heavy order-creation bursts and continuous inventory updates.  
- **Masterless & fault-tolerant**  
  No single point of failure; ideal for global e-commerce.  
- **Linear scalability**  
  Adding nodes directly increases write and read capacity.  
- **Tunable consistency**  
  Choose strong consistency for orders, fast/loose consistency for inventory reads.

**Why it's perfect for these services:**  
- Orders and inventory generate **time-series style, append-heavy workloads**.  
- Cassandra excels when the pattern is:  
  - Many writes  
  - Sequential inserts  
  - High availability  
  - Partition-based queries  
- The masterless architecture ensures the system remains **always available**, even during massive traffic spikes.





**Point to Kafka:**
*"Finally, Apache Kafka serves as our event bus for asynchronous communication. Payment Service publishes events that Order Service consumes to update order status."*

### Technology Stack Callout
*"This architecture demonstrates the persistence pattern - choosing the right database for each specific use case, which is a key principle in microservices design."*

---

## 💾 TAB 2: DATABASE ARCHITECTURE (3-4 minutes)

**[Click Database Architecture Tab]**

### MySQL Card
**[Point to MySQL card]**
- *"Account Service stores users, addresses, and credentials"*
- *"Payment Service stores payment transactions with unique constraints for idempotency"*
- *"We chose MySQL for its proven reliability in financial transactions and strong consistency"*

### PostgreSQL Card
**[Point to PostgreSQL card]**
*"PostgreSQL is used by Auth Service for:"*
- *"Token blacklist management - when users log out, tokens are blacklisted"*
- *"We leverage PostgreSQL's advanced indexing and JSONB support for efficient token lookups"*

### MongoDB Card
**[Point to MongoDB card]**
*"MongoDB is perfect for Item Service because:"*
- *"Product specifications vary dramatically - electronics have different attributes than clothing"*
- *"The flexible document model stores item metadata including name, price, UPC, images, and specifications"*
- *"'difficult to finalize a schema' - MongoDB handles this perfectly"*

### Cassandra Card
**[Point to Cassandra card]**
*"Cassandra handles high-throughput operations:"*
- *"Order Service: Optimized for write-heavy order creation workloads"*
- *"Inventory Service: Handles frequent stock updates with eventual consistency"*

### Database Selection Strategy
**[Point to the highlight box]**
*"Each database choice was intentional:"*
- *"Relational databases (MySQL/PostgreSQL) where data integrity is critical"*
- *"Document store (MongoDB) for flexible schemas"*
- *"Wide-column store (Cassandra) for massive write loads"*

### Data Flow Examples
**[Point to code block]**
*"Here you can see the complete data flow:"*
- *"User registration flows from Gateway → Account Service → MySQL"*
- *"Item lookups query MongoDB for product details"*
- *"Order creation writes to Cassandra and triggers inventory updates"*
- *"Payment processing writes to MySQL and publishes Kafka events"*

---

## 🍃 TAB 3: SPRING ECOSYSTEM (4-5 minutes)

**[Click Spring Ecosystem Tab]**

### Spring Boot Card
**[Point to Spring Boot card]**
*"Spring Boot 3.3.4 is our foundation:"*
- *"All 7 services are Spring Boot applications built with Maven"*
- *"Auto-configuration reduces boilerplate"*
- *"Embedded Tomcat servers - no external application servers needed"*
- *"Production-ready"*

### Spring Cloud Card
**[Point to Spring Cloud card]**
- *"Spring Cloud Gateway for intelligent routing and load balancing"*
- *"Spring Cloud OpenFeign for declarative inter-service communication"*
- *"We use OpenFeign in multiple services, for example, Order Service calls Inventory Service to check stock"*

### Spring Security Card
**[Point to Spring Security card]**
- *"OAuth2 Resource Server validates JWT tokens at the Gateway"*
- *"Method-level security with @PreAuthorize annotations"*
- *"BCrypt password hashing with strength 10"*

### Spring Data Card
**[Point to Spring Data card]**
*"Spring Data provides our persistence layer:"*
- *"Spring Data JPA with Hibernate ORM for MySQL/PostgreSQL - satisfies the Hibernate and Spring JPA requirements"*
- *"Spring Data MongoDB for document operations"*
- *"Spring Data Cassandra for wide-column store"*
- *"Repository pattern abstracts database complexity"*

### Spring Kafka Card
**[Point to Spring Kafka card]**
*"Spring Kafka integration for requirement #8:"*
- *"KafkaTemplate for publishing events"*
- *"@KafkaListener for consuming events"*
- *"Payment Service publishes payment.succeeded/failed events"*
- *"Order Service consumes these to update order status"*

### Spring Actuator Card
**[Point to Spring Actuator card]**
*"Spring Actuator provides production monitoring:"*
- *"Health check endpoints - /actuator/health"*
- *"Metrics and application info"*
- *"Used in Docker health checks for container orchestration"*

### Spring Test Card
**[Point to Spring Test card]**
*"Testing infrastructure for requirement #9:"*
- *"JUnit 5 integration as specified"*
- *"Mockito for mocking - part of the JUnit/Mockito/PowerMock requirement"*
- *"@SpringBootTest for integration tests"*
- *"TestContainers for database integration tests"*

### Bean Validation Card
**[Point to Bean Validation card]**
*"JSR-303 validation ensures data quality:"*
- *"@NotNull, @NotBlank, @Email annotations"*
- *"Automatic validation in controllers with @Valid"*
- *"Custom validators for business rules"*

### Spring Architecture Layers
**[Point to architecture layers diagram]**
*"Here's our standard layered architecture:"*
- *"@RestController layer for REST APIs - all services expose RESTful endpoints"*
- *"@Service layer for business logic with @Transactional support"*
- *"@Repository layer with JpaRepository/MongoRepository interfaces"*
- *"Database layer with our four database types"*

### Key Spring Features Used
**[Point to highlight box]**
*"We leverage several advanced Spring features:"*
- *"Dependency Injection with constructor injection for testability"*
- *"AOP with @Transactional for declarative transaction management"*
- *"@RestControllerAdvice for centralized exception handling"*
- *"Spring Profiles for dev, docker, and test environments"*

### Swagger/OpenAPI
*"All our services include Swagger UI:"*
- *"Access at http://localhost:8080/swagger-ui.html through the gateway"*
- *"Interactive API documentation"*
- *"Try-it-out functionality for testing endpoints"*

---

## 📨 TAB 4: EVENT-DRIVEN FLOW (3-4 minutes)

**[Click Event-Driven Flow Tab]**

*"Using Kafka for event-driven asynchronous communication. Let me walk you through our implementation."*

### Kafka Topics & Event Flow
**[Point to code block with topics]**
*"We've implemented 5 Kafka topics:"*

1. **order.created**
   - *"Published by Order Service when customer submits order"*
   - *"Consumed by Inventory Service to reserve stock"*

2. **order.cancelled**
   - *"Published when order is cancelled"*
   - *"Inventory Service restores stock, Payment Service processes refunds"*

3. **payment.succeeded**
   - *"Published by Payment Service after successful payment"*
   - *"Order Service updates status from CREATED to PAID"*
   - *"This is the key integration point mentioned in requirements"*

4. **payment.failed**
   - *"Published when payment fails"*
   - *"Order Service marks order as failed"*

5. **payment.refunded**
   - *"Published when refund is processed"*
   - *"Demonstrates idempotency requirement"*

### Complete Order Flow
**[Point to the flow diagram]**
*"This diagram shows the complete asynchronous workflow:"*

**Step-by-step:**
1. *"Client creates order via POST /orders → Order Service saves to Cassandra"*
2. *"Order Service publishes 'order.created' event to Kafka"*
3. *"Inventory Service consumes event and reserves stock"*
4. *"Client submits payment via POST /payments → Payment Service processes"*
5. *"Payment Service publishes 'payment.succeeded' to Kafka"*
6. *"Order Service consumes event and updates order status to PAID"*

*"This demonstrates both synchronous communication (REST APIs) and asynchronous communication (Kafka events)."*

### Benefits of Event-Driven Architecture
**[Point to benefits list]**
*"This architecture provides:"*
- **Loose Coupling** - *"Services don't directly depend on each other"*
- **Scalability** - *"We can scale Order Service and Payment Service independently"*
- **Resilience** - *"If Inventory Service is down, events are queued in Kafka"*
- **Audit Trail** - *"Every business event is permanently logged"*
- **Real-time Updates** - *"Status changes propagate immediately"*

### Kafka UI
*"We also deployed Kafka UI on port 8082 for monitoring:"*
- *"View all topics and messages"*
- *"Monitor consumer lag"*
- *"Debug event flows during development"*

---

## 🔐 TAB 5: SECURITY ARCHITECTURE (2-3 minutes)

**[Click Security Architecture Tab]**

### Authentication Flow
**[Point to authentication flow diagram]**
*"Let me walk through the login process:"*

**Step 1-2:** *"Client sends credentials to Gateway which routes to Auth Service"*

**Step 3:** *"Auth Service validates credentials"*

**Step 4:** *"Auth Service calls Account Service via OpenFeign to fetch user from MySQL"*

**Step 5:** *"Auth Service generates JWT token signed with HS256 algorithm"*

**Step 6:** *"Token returned to client with 24-hour expiration"*

*"the token becomes part of request headers for subsequent calls."*

### Authorization Flow
**[Point to authorization flow diagram]**
*"For protected endpoints:"*

**Step 1:** *"Client includes token in Authorization: Bearer header"*

**Step 2:** *"Gateway extracts and validates JWT signature, expiration, and blacklist status"*

**Decision Point:** *"Valid token → Request forwarded to business service with user context"*
*"Invalid token → 401 Unauthorized returned immediately"*

**Step 3:** *"Business service processes request with authenticated user"*

### JWT Token Structure
**[Point to JWT code block]**
*"Our JWT contains:"*
- **Header:** *"Algorithm HS256 and token type"*
- **Payload:** *"User ID, email, issued-at, and expiration timestamps"*
- **Signature:** *"HMAC-SHA256 ensures token hasn't been tampered with"*

### Security Features Implemented
**[Point to features list]**
*"Beyond basic authentication, we implement:"*
- **JWT Authentication** - *"Stateless tokens, no server-side session storage"*
- **Password Hashing** - *"BCrypt with strength 10, never store plaintext passwords"*
- **Token Blacklist** - *"Revoked tokens stored in PostgreSQL for immediate invalidation"*
- **CORS Configuration** - *"Properly configured for cross-origin requests"*
- **Method Security** - *"@PreAuthorize annotations for fine-grained access control"*
- **HTTPS Ready** - *"SSL/TLS support for production deployment"*

*"This security architecture protects all four business services as required."*

---

## 🐳 TAB 6: DEPLOYMENT VIEW (2-3 minutes)

**[Click Deployment View Tab]**

### Docker Compose Structure
**[Point to docker-compose.yml structure]**
*"Our docker-compose.yml orchestrates 15 containers:"*

**Services Section:**
- *"7 Spring Boot microservices (Gateway + Auth + 4 business services + Inventory)"*
- *"Each built from source using multi-stage Docker builds"*

**Databases Section:**
- *"MySQL, PostgreSQL, MongoDB, Cassandra"*
- *"All four required databases with initialization scripts"*
- *"Persistent volumes for data retention"*

**Messaging Section:**
- *"Zookeeper for Kafka coordination"*
- *"Kafka broker for event streaming"*
- *"Kafka UI for event monitoring"*

### One-Click Deployment
**[Point to Quick Start card]**
*"The requirement asks for one-click deployment. Here it is:"*

```bash
docker-compose up -d
```

*"This single command:"*
- *"Builds all 7 services with Maven"*
- *"Creates Docker images"*
- *"Starts all 15 containers in dependency order"*
- *"Initializes all databases with schemas"*
- *"Configures networking between containers"*
- *"Runs health checks until all services are ready"*

*"Complete startup takes about 2 minutes. That's it."*

### Health Check
**[Point to Health Check card]**
```bash
docker-compose ps
```
*"This shows the status of all containers - all should show 'Up' and 'healthy'."*

### Container Configuration
**[Point to configuration list]**
*"Our Docker setup includes:"*
- **Base Image** - *"Eclipse Temurin JDK 21 for optimal Java performance"*
- **Build Tool** - *"Maven with Spring Boot plugin creates executable JARs"*
- **Networking** - *"Bridge network allows services to communicate by name"*
- **Volumes** - *"Database data persists across container restarts"*
- **Health Checks** - *"Spring Actuator endpoints ensure containers are truly ready"*
- **Environment** - *"SPRING_PROFILES_ACTIVE=docker for container-specific configuration"*

### Network Ports Mapping
**[Point to ports diagram]**
*"All services are accessible from localhost:"*
- **Gateway (8080)** - *"Single entry point for all API requests"*
- **Kafka UI (8082)** - *"Monitor events in real-time"*
- **Microservices (9000-9005)** - *"Direct access for debugging"*
- **Databases (3306, 5432, 27017, 9042)** - *"Connect via database clients"*

---

## 🎓 CLOSING STATEMENT (1 minute)

*"To summarize, our project demonstrates:"*

### ✅ All Required Technologies
- ✅ **Spring Boot, Maven, Spring Security** - Foundation of all services
- ✅ **JUnit, Mockito, Jacoco** - 6 out of 7 services exceed 30% coverage requirement
- ✅ **Swagger** - Interactive API documentation on all services
- ✅ **Hibernate, Spring JPA** - Data access layer
- ✅ **Spring Cloud** - Gateway and OpenFeign for inter-service communication
- ✅ **MySQL, PostgreSQL, MongoDB, Cassandra** - All four databases utilized
- ✅ **Kafka** - Event-driven architecture for asynchronous communication

### ✅ All Required Services
- ✅ **Item Service** - Product catalog with inventory (MongoDB + Cassandra)
- ✅ **Order Service** - Complete lifecycle: Created → Paid → Completed/Cancelled
- ✅ **Payment Service** - Idempotent payment processing with Kafka events
- ✅ **Account Service** - User management with addresses and payment methods
- ✅ **Authentication Server** - JWT-based security for all services

### ✅ All Required Features
- ✅ **Synchronous Communication** - REST APIs with OpenFeign
- ✅ **Asynchronous Communication** - Kafka event streaming
- ✅ **One-Click Deployment** - `docker-compose up -d`
- ✅ **Token-Based Auth** - JWT tokens validated at Gateway
- ✅ **Test Coverage** - Jacoco reports show 30%+ coverage
- ✅ **Polyglot Persistence** - Right database for each use case

*"We've built a production-ready microservices architecture that demonstrates every technology and pattern covered in the ICC course. The system is fully functional, well-tested, and ready for demonstration."*

*"I'm happy to answer any questions or demonstrate specific features."*

---

## 📚 APPENDIX: Q&A PREPARATION

### Likely Questions & Answers

**Q: Why did you separate Inventory Service from Item Service?**
*"Great question. The requirements mention Item Service includes inventory, but we separated them for scalability. Item lookups are read-heavy while inventory updates are write-heavy. This allows us to scale them independently and use Cassandra for high-throughput inventory operations."*

**Q: How do you handle idempotency in Payment Service?**
*"We use unique constraint on orderId in the payments table. If a payment request is submitted twice with the same orderId, the database constraint prevents duplicate entries. We also generate unique payment IDs that can be checked before processing."*

**Q: What if Kafka goes down?**
*"Services remain operational for synchronous requests. Kafka buffers messages when consumers are unavailable. When services reconnect, they consume queued events and catch up. This is the resilience benefit of event-driven architecture."*

**Q: How do you ensure 30% test coverage?**
*"We use Jacoco Maven plugin with a check phase that fails builds below 30%. We focus on testing service layer business logic, repository layer data access, and controller layer request handling. Six services exceed the requirement."*

**Q: Why use four different databases?**
*"It's polyglot persistence. MySQL gives ACID for transactions, PostgreSQL for advanced SQL, MongoDB for flexible schemas, and Cassandra for high write throughput. Each database excels at different workloads."*

**Q: How does the Gateway validate tokens?**
*"Gateway uses Spring Security OAuth2 Resource Server. It validates JWT signature, checks expiration, and queries Auth Service to verify the token isn't blacklisted. Invalid tokens get 401 responses immediately."*

**Q: Can you show me the code?**
*"Absolutely. [Be ready to open specific files like OrderService.java, PaymentEventListener.java, JwtTokenProvider.java, or docker-compose.yml]"*

---

## 🎯 DEMO TIPS

### Navigation Tips
- Keep requirements.md open in another tab to reference line numbers
- Use Ctrl+F to quickly find terms in requirements
- Click between tabs smoothly - practice transitions

### Presentation Tips
- Speak clearly and at moderate pace
- Point to specific elements on screen as you mention them
- Use the diagrams as visual aids - don't just read the script
- Pause after complex points to allow questions
- Make eye contact if presenting in person / look at camera if remote

### Technical Confidence
- Know the port numbers (Gateway 8080, services 9000-9005, Kafka UI 8082)
- Remember the database mappings (MySQL: Account/Payment, Postgres: Auth, MongoDB: Item, Cassandra: Order/Inventory)
- Be able to quickly show `docker compose ps` to prove everything is running
- Have Swagger UI ready: http://localhost:8080/swagger-ui.html

### Time Management
- Tab 1 (Overview): 2-3 min
- Tab 2 (Databases): 3-4 min  
- Tab 3 (Spring): 4-5 min (most technical depth)
- Tab 4 (Kafka): 3-4 min
- Tab 5 (Security): 2-3 min
- Tab 6 (Deployment): 2-3 min
- **Total: 16-22 minutes with buffer for questions**

### Emergency Backup
If something breaks during demo:
1. Show the architecture diagrams anyway (HTML works offline)
2. Reference code in the repository
3. Show test coverage reports in target/site/jacoco/
4. Walk through docker-compose.yml to prove everything exists
5. Show logs with `docker compose logs`

Good luck with your presentation! 🚀
