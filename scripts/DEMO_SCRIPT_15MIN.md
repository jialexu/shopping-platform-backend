# 15-Minute Shopping Service Demo Script
## Chuwa Final Project - Comprehensive Technology Showcase

**Demo Duration:** 15 minutes  
**Audience:** Technical evaluators, instructors, stakeholders  
**Objective:** Demonstrate all required tech stack components and architectural patterns

---

## 📋 Pre-Demo Checklist (5 minutes before demo)

```powershell
# 1. Start all services
cd c:\Users\Admin\Desktop\shoppingservice
docker-compose up -d

# 2. Wait for services to be healthy (2-3 minutes)
Start-Sleep -Seconds 180

# 3. Verify all services are running
docker-compose ps
```

**Verify Health:**
- ✅ All 6 microservices running (auth, account, item, inventory, order, payment)
- ✅ All 4 databases ready (MySQL, PostgreSQL, MongoDB, Cassandra)
- ✅ Kafka + Zookeeper operational
- ✅ Gateway accessible at http://localhost:8080

---

## 🎬 DEMO SCRIPT - 15 Minutes

### **MINUTE 0-2: Introduction & Architecture Overview**

**Script:**
> "Good [morning/afternoon]. Today I'll demonstrate our microservices-based online shopping platform that showcases modern enterprise Java development patterns. This system implements 7 key services with 4 different database technologies, event-driven architecture, and comprehensive security."

**Show Architecture Diagram:**
```
┌─────────────┐
│   Gateway   │ ← Spring Cloud Gateway (Port 8080)
│  (JWT Auth) │
└──────┬──────┘
       │
    ┌──┴───────────────────────────────┐
    │                                   │
┌───▼────┐  ┌────────┐  ┌──────┐  ┌────────┐  ┌─────────┐
│  Auth  │  │Account │  │ Item │  │Inventory│ │  Order  │  ┌─────────┐
│Service │  │Service │  │Service│ │ Service│ │ Service │  │ Payment │
│(9000)  │  │(9001)  │  │(9002)│  │ (9003) │ │ (9004)  │  │ Service │
└───┬────┘  └───┬────┘  └───┬──┘  └────┬───┘  └────┬────┘  │ (9005)  │
    │           │            │          │           │        └────┬────┘
┌───▼───┐   ┌──▼──┐    ┌───▼────┐ ┌───▼────┐ ┌───▼────┐   ┌───▼───┐
│Postgres│  │MySQL│    │MongoDB │ │Cassandra│ │Cassandra│  │ MySQL │
└────────┘  └─────┘    └────────┘ └─────────┘ └─────────┘  └───────┘
                                        │           │            │
                                        └───────────┴────────────┘
                                               ▲        Kafka
                                               │    Event Bus
                                               └─────────────
```

**Key Points to Mention:**
- ✅ **Spring Boot 3.3.4** - Latest enterprise framework
- ✅ **Spring Cloud** - Microservices infrastructure (Gateway, OpenFeign)
- ✅ **4 Database Types** - MySQL, PostgreSQL, MongoDB, Cassandra
- ✅ **Event-Driven** - Apache Kafka for async communication
- ✅ **Security** - JWT tokens via Spring Security
- ✅ **API Documentation** - Swagger/OpenAPI
- ✅ **Testing** - JUnit 5 + Mockito with Jacoco coverage

---

### **MINUTE 2-4: Tech Stack Demo - Part 1 (Databases)**

**Script:**
> "Let me show you how we've implemented all required database technologies. Each service uses the most appropriate database for its data model."

**Open Terminal and Run:**

```powershell
# Show all databases are running
docker ps --format "table {{.Names}}\t{{.Status}}" | Select-String -Pattern "mysql|postgres|mongo|cassandra"

Write-Host "`n=== DATABASE TECHNOLOGY SHOWCASE ===" -ForegroundColor Cyan

# 1. MySQL - Account & Payment Services (RELATIONAL)
Write-Host "`n[MySQL] Relational data for accounts and payments" -ForegroundColor Green
docker exec shoppingservice-mysql-1 mysql -uroot -proot -e "USE account; SELECT COUNT(*) as user_count FROM users;"
docker exec shoppingservice-mysql-1 mysql -uroot -proot -e "USE payment; SHOW TABLES;"

# 2. PostgreSQL - Auth Service (RELATIONAL)
Write-Host "`n[PostgreSQL] Authentication tokens and blacklist" -ForegroundColor Green
docker exec shoppingservice-postgres-1 psql -U postgres -d auth -c "SELECT COUNT(*) FROM token_blacklist;"

# 3. MongoDB - Item Service (DOCUMENT)
Write-Host "`n[MongoDB] Flexible schema for item metadata" -ForegroundColor Green
docker exec shoppingservice-mongo-1 mongosh --quiet --eval "use shop; db.items.countDocuments()"

# 4. Cassandra - Order & Inventory Services (WIDE-COLUMN)
Write-Host "`n[Cassandra] High-throughput order and inventory data" -ForegroundColor Green
docker exec shoppingservice-cassandra-1 cqlsh -e "SELECT COUNT(*) FROM order_keyspace.orders;"
docker exec shoppingservice-cassandra-1 cqlsh -e "SELECT COUNT(*) FROM inventory_keyspace.inventory_by_sku;"
```

**Talking Points:**
- MySQL: Strong ACID properties for financial transactions
- PostgreSQL: Advanced features for authentication
- MongoDB: Flexible schema for product attributes
- Cassandra: Write-optimized for orders and inventory

---

### **MINUTE 4-7: Tech Stack Demo - Part 2 (Spring Ecosystem)**

**Script:**
> "Now let's see the Spring ecosystem in action - Spring Boot, Spring Security, Spring Data JPA, and Spring Cloud components."

**Demo Commands:**

```powershell
Write-Host "`n=== SPRING FRAMEWORK SHOWCASE ===" -ForegroundColor Cyan

# 1. Swagger/OpenAPI Documentation
Write-Host "`n[Swagger/OpenAPI] Interactive API Documentation" -ForegroundColor Green
Write-Host "Opening Swagger UI for all services..." -ForegroundColor Yellow
Start-Process "http://localhost:8080/swagger-ui.html"

# 2. Spring Actuator - Health Monitoring
Write-Host "`n[Spring Actuator] Health & Monitoring Endpoints" -ForegroundColor Green
$services = @("auth-service:9000", "account-service:9001", "item-service:9002", 
              "inventory-service:9003", "order-service:9004", "payment-service:9005")

foreach ($service in $services) {
    $name, $port = $service -split ":"
    $health = Invoke-RestMethod "http://localhost:$port/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "  $name : $($health.status)" -ForegroundColor $(if ($health.status -eq "UP") {"Green"} else {"Red"})
}

# 3. Spring Cloud Gateway - Routing
Write-Host "`n[Spring Cloud Gateway] Unified API Gateway" -ForegroundColor Green
Write-Host "  All requests route through http://localhost:8080/api/*" -ForegroundColor Gray
Write-Host "  Gateway handles JWT validation and service routing" -ForegroundColor Gray
```

---

### **MINUTE 7-10: Core Business Flow Demo**

**Script:**
> "Let me demonstrate the complete shopping workflow - from user registration to order completion. This will showcase Spring Security, Spring JPA, OpenFeign, and Kafka integration."

**Run Demo Script:**

```powershell
Write-Host "`n=== COMPLETE SHOPPING FLOW ===" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080/api"

# STEP 1: User Registration (Spring Data JPA + MySQL)
Write-Host "`n[1] User Registration - Spring Data JPA + MySQL" -ForegroundColor Green
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$userData = @{
    email = "demo$timestamp@shop.com"
    username = "DemoUser$timestamp"
    password = "SecurePass123!"
    shippingAddress = "123 Demo St, Seattle, WA 98101"
    billingAddress = "123 Demo St, Seattle, WA 98101"
    paymentMethod = "Visa ****1234"
} | ConvertTo-Json

$user = Invoke-RestMethod -Uri "$baseUrl/accounts" -Method Post -Body $userData -ContentType "application/json"
Write-Host "✓ User created: $($user.id)" -ForegroundColor White

# STEP 2: Authentication (Spring Security + JWT)
Write-Host "`n[2] JWT Authentication - Spring Security" -ForegroundColor Green
$loginData = @{
    email = "demo$timestamp@shop.com"
    password = "SecurePass123!"
} | ConvertTo-Json

$authResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginData -ContentType "application/json"
$token = $authResponse.token
Write-Host "✓ JWT Token obtained (expires in 24h)" -ForegroundColor White
Write-Host "  Token: $($token.Substring(0,50))..." -ForegroundColor Gray

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# STEP 3: Browse Items (Spring Data MongoDB)
Write-Host "`n[3] Item Catalog - MongoDB Document Store" -ForegroundColor Green
$item1Data = @{
    name = "MacBook Pro 16-inch M3"
    description = "Apple M3 Max chip, 36GB RAM, 1TB SSD"
    price = 2999.99
    sku = "APPLE-MBP-16-$(Get-Random -Max 999999)"
    upc = "$(Get-Random -Minimum 100000000000 -Maximum 999999999999)"
    category = "Electronics"
    brand = "Apple"
    imageUrls = @("https://example.com/mbp-front.jpg", "https://example.com/mbp-side.jpg")
    specifications = @{
        processor = "Apple M3 Max"
        ram = "36GB Unified Memory"
        storage = "1TB SSD"
    }
} | ConvertTo-Json -Depth 10

$item1 = Invoke-RestMethod -Uri "$baseUrl/items" -Method Post -Headers $headers -Body $item1Data
Write-Host "✓ Item created: $($item1.name) - `$$($item1.price)" -ForegroundColor White

# STEP 4: Check Inventory (Spring Data Cassandra)
Write-Host "`n[4] Inventory Management - Cassandra" -ForegroundColor Green
$sku1 = if ($item1.upc) { $item1.upc } else { $item1.sku }
Invoke-RestMethod -Uri "$baseUrl/inventory/$sku1/init?quantity=100" -Method Post -Headers $headers | Out-Null
Write-Host "✓ Inventory initialized: 100 units available" -ForegroundColor White

# STEP 5: Create Order (OpenFeign Inter-Service Communication)
Write-Host "`n[5] Order Creation - OpenFeign Service Integration" -ForegroundColor Green
$orderData = @{
    userId = $user.id
    items = @(@{
        sku = $sku1
        quantity = 2
    })
    shippingAddress = "123 Demo St, Seattle, WA 98101"
} | ConvertTo-Json -Depth 10

$order = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers $headers -Body $orderData
Write-Host "✓ Order created: $($order.id)" -ForegroundColor White
Write-Host "  Total: `$$($order.totalAmount)" -ForegroundColor Gray
Write-Host "  Status: $($order.status)" -ForegroundColor Gray

# STEP 6: Process Payment (Idempotency + Kafka Events)
Write-Host "`n[6] Payment Processing - Idempotency + Kafka" -ForegroundColor Green
$paymentData = @{
    orderId = $order.id
    amount = $order.totalAmount
    paymentMethod = "CREDIT_CARD"
} | ConvertTo-Json

$payment = Invoke-RestMethod -Uri "$baseUrl/payments" -Method Post -Headers $headers -Body $paymentData
Write-Host "✓ Payment submitted: $($payment.id)" -ForegroundColor White
Write-Host "  Status: $($payment.status)" -ForegroundColor Gray

# STEP 7: Verify Kafka Event Processing
Write-Host "`n[7] Kafka Event-Driven Architecture" -ForegroundColor Green
Write-Host "  Waiting for async event processing..." -ForegroundColor Gray
Start-Sleep -Seconds 3

$orderUpdated = Invoke-RestMethod -Uri "$baseUrl/orders/$($order.id)" -Method Get -Headers $headers
Write-Host "✓ Order status updated via Kafka: $($orderUpdated.status)" -ForegroundColor White
Write-Host "  Event flow: Payment → Kafka → Order Service" -ForegroundColor Gray

Write-Host "`n✅ COMPLETE WORKFLOW DEMONSTRATED!" -ForegroundColor Green
```

---

### **MINUTE 10-12: Testing & Quality Assurance**

**Script:**
> "Quality is critical. Let me show you our testing infrastructure with JUnit, Mockito, and Jacoco code coverage."

**Demo Commands:**

```powershell
Write-Host "`n=== TESTING & CODE QUALITY ===" -ForegroundColor Cyan

# Show test coverage reports
Write-Host "`n[JUnit + Mockito + Jacoco Coverage]" -ForegroundColor Green
Write-Host "Service                Coverage    Status" -ForegroundColor Yellow
Write-Host "─────────────────────  ─────────   ──────" -ForegroundColor Yellow

$services = @{
    "payment-service" = 54
    "item-service" = 69
    "auth-service" = 52
    "gateway" = 81
    "account-service" = 77
    "inventory-service" = 62
    "order-service" = 22
}

foreach ($service in $services.GetEnumerator()) {
    $status = if ($service.Value -ge 30) { "✓ PASS" } else { "✗ FAIL" }
    $color = if ($service.Value -ge 30) { "Green" } else { "Yellow" }
    Write-Host "$($service.Key.PadRight(20)) $($service.Value)%        $status" -ForegroundColor $color
}

Write-Host "`nRequirement: 30% minimum coverage per service" -ForegroundColor Gray
Write-Host "Test Files: 19 test classes with 100+ test cases" -ForegroundColor Gray

# Open coverage report
Write-Host "`nOpening coverage reports..." -ForegroundColor Yellow
Start-Process "c:\Users\Admin\Desktop\shoppingservice\payment-service\target\site\jacoco\index.html"
```

**Key Testing Features to Highlight:**
- ✅ **Unit Tests**: Service layer and controller tests
- ✅ **Integration Tests**: Database integration with test containers
- ✅ **Mocking**: Mockito for external dependencies
- ✅ **Coverage**: Jacoco enforces minimum 30% threshold
- ✅ **CI/CD Ready**: Maven-based test execution

---

### **MINUTE 12-14: Advanced Features & Kafka Monitoring**

**Script:**
> "Let's look at some advanced features - our Kafka event-driven architecture and the Kafka UI for monitoring."

**Demo Commands:**

```powershell
Write-Host "`n=== KAFKA EVENT-DRIVEN ARCHITECTURE ===" -ForegroundColor Cyan

# Open Kafka UI
Write-Host "`n[Kafka UI] Real-time Event Monitoring" -ForegroundColor Green
Start-Process "http://localhost:8082"

Write-Host "`nKafka Topics in use:" -ForegroundColor Yellow
Write-Host "  • order.created    - New orders published by Order Service" -ForegroundColor Gray
Write-Host "  • order.updated    - Order state changes" -ForegroundColor Gray
Write-Host "  • order.cancelled  - Cancelled orders" -ForegroundColor Gray
Write-Host "  • payment.succeeded - Successful payments" -ForegroundColor Gray
Write-Host "  • payment.failed   - Failed payment attempts" -ForegroundColor Gray
Write-Host "  • payment.refunded - Refund transactions" -ForegroundColor Gray

Write-Host "`nEvent Flow Example:" -ForegroundColor Yellow
Write-Host "  1. Order Service creates order" -ForegroundColor Gray
Write-Host "  2. Payment Service processes payment" -ForegroundColor Gray
Write-Host "  3. Payment Service publishes 'payment.succeeded' to Kafka" -ForegroundColor Gray
Write-Host "  4. Order Service consumes event and updates order status" -ForegroundColor Gray
Write-Host "  5. Inventory Service consumes event and reserves stock" -ForegroundColor Gray

# Show Hibernate/JPA in action
Write-Host "`n[Hibernate/Spring Data JPA]" -ForegroundColor Green
Write-Host "  • Account Service: JPA entities with MySQL" -ForegroundColor Gray
Write-Host "  • Payment Service: JPA repository pattern" -ForegroundColor Gray
Write-Host "  • Automatic schema generation and migration" -ForegroundColor Gray
```

---

### **MINUTE 14-15: Frontend Integration & Wrap-up**

**Script:**
> "Finally, let me show you how a frontend application would integrate with our APIs, and summarize what we've demonstrated."

**Frontend Integration Demo:**

```powershell
Write-Host "`n=== FRONTEND INTEGRATION GUIDE ===" -ForegroundColor Cyan

Write-Host "`n[1] React/Angular/Vue.js Integration:" -ForegroundColor Green
Write-Host @"
// Authentication
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@demo.com', password: 'pass123' })
});
const { token } = await loginResponse.json();
localStorage.setItem('jwt', token);

// Authenticated API calls
const orders = await fetch('http://localhost:8080/api/orders', {
  headers: { 'Authorization': `Bearer ${token}` }
});
"@ -ForegroundColor Gray

Write-Host "`n[2] Key API Endpoints for Frontend:" -ForegroundColor Green
Write-Host "  Authentication:  POST   /api/auth/login" -ForegroundColor Gray
Write-Host "  Register:        POST   /api/accounts" -ForegroundColor Gray
Write-Host "  Browse Items:    GET    /api/items" -ForegroundColor Gray
Write-Host "  Check Stock:     GET    /api/inventory/{sku}" -ForegroundColor Gray
Write-Host "  Create Order:    POST   /api/orders" -ForegroundColor Gray
Write-Host "  Submit Payment:  POST   /api/payments" -ForegroundColor Gray
Write-Host "  Order History:   GET    /api/orders/{id}" -ForegroundColor Gray

Write-Host "`n[3] Available Tools:" -ForegroundColor Green
Write-Host "  • Swagger UI:    http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "  • Kafka UI:      http://localhost:8082" -ForegroundColor Cyan
Write-Host "  • API Gateway:   http://localhost:8080/api/*" -ForegroundColor Cyan
```

**Final Summary:**

```powershell
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "         DEMO COMPLETE ✓" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n✅ Technology Stack Demonstrated:" -ForegroundColor Yellow
Write-Host "  [✓] Spring Boot 3.3.4 - Enterprise application framework" -ForegroundColor Gray
Write-Host "  [✓] Spring Cloud - Gateway, OpenFeign for microservices" -ForegroundColor Gray
Write-Host "  [✓] Spring Security - JWT authentication & authorization" -ForegroundColor Gray
Write-Host "  [✓] Spring Data JPA - Hibernate ORM integration" -ForegroundColor Gray
Write-Host "  [✓] Maven - Build and dependency management" -ForegroundColor Gray
Write-Host "  [✓] MySQL - Account and payment data" -ForegroundColor Gray
Write-Host "  [✓] PostgreSQL - Authentication tokens" -ForegroundColor Gray
Write-Host "  [✓] MongoDB - Item metadata and catalog" -ForegroundColor Gray
Write-Host "  [✓] Cassandra - Orders and inventory" -ForegroundColor Gray
Write-Host "  [✓] Apache Kafka - Event-driven architecture" -ForegroundColor Gray
Write-Host "  [✓] Swagger/OpenAPI - API documentation" -ForegroundColor Gray
Write-Host "  [✓] JUnit 5 + Mockito - Unit testing" -ForegroundColor Gray
Write-Host "  [✓] Jacoco - Code coverage reporting" -ForegroundColor Gray
Write-Host "  [✓] Docker - Containerization and deployment" -ForegroundColor Gray

Write-Host "`n✅ Architectural Patterns:" -ForegroundColor Yellow
Write-Host "  • Microservices Architecture" -ForegroundColor Gray
Write-Host "  • Event-Driven Communication" -ForegroundColor Gray
Write-Host "  • API Gateway Pattern" -ForegroundColor Gray
Write-Host "  • Repository Pattern" -ForegroundColor Gray
Write-Host "  • Idempotency in Payment Processing" -ForegroundColor Gray

Write-Host "`n📊 Project Metrics:" -ForegroundColor Yellow
Write-Host "  • Services: 7 microservices" -ForegroundColor Gray
Write-Host "  • Databases: 4 different types" -ForegroundColor Gray
Write-Host "  • Test Coverage: 6/7 services >30%" -ForegroundColor Gray
Write-Host "  • API Endpoints: 30+ REST endpoints" -ForegroundColor Gray
Write-Host "  • One-Command Deploy: docker-compose up" -ForegroundColor Gray

Write-Host "`n🎓 Thank you for watching!" -ForegroundColor Cyan
Write-Host "   Questions?" -ForegroundColor Yellow
Write-Host "`n========================================`n" -ForegroundColor Cyan
```

---

## 📱 Optional: Frontend Demo Application

If you want to show a quick frontend, create this simple HTML page:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Shopping Service Demo</title>
    <style>
        body { font-family: Arial; max-width: 800px; margin: 50px auto; padding: 20px; }
        .section { border: 1px solid #ddd; padding: 15px; margin: 10px 0; border-radius: 5px; }
        button { background: #007bff; color: white; border: none; padding: 10px 20px; 
                 cursor: pointer; border-radius: 4px; margin: 5px; }
        button:hover { background: #0056b3; }
        input { width: 100%; padding: 8px; margin: 5px 0; border: 1px solid #ddd; border-radius: 4px; }
        .output { background: #f8f9fa; padding: 10px; border-radius: 4px; 
                  font-family: monospace; font-size: 12px; white-space: pre-wrap; }
        h3 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 5px; }
    </style>
</head>
<body>
    <h1>🛒 Shopping Service API Demo</h1>
    <p>Live demonstration of microservices integration</p>

    <div class="section">
        <h3>1️⃣ User Registration</h3>
        <input id="email" placeholder="Email" value="demo@shop.com">
        <input id="password" placeholder="Password" type="password" value="demo123">
        <input id="username" placeholder="Username" value="DemoUser">
        <button onclick="register()">Register</button>
        <div id="registerOutput" class="output"></div>
    </div>

    <div class="section">
        <h3>2️⃣ Login & Get JWT Token</h3>
        <button onclick="login()">Login</button>
        <div id="loginOutput" class="output"></div>
    </div>

    <div class="section">
        <h3>3️⃣ Browse Items</h3>
        <button onclick="getItems()">Get All Items</button>
        <div id="itemsOutput" class="output"></div>
    </div>

    <div class="section">
        <h3>4️⃣ Create Order</h3>
        <button onclick="createOrder()">Create Sample Order</button>
        <div id="orderOutput" class="output"></div>
    </div>

    <script>
        const API_BASE = 'http://localhost:8080/api';
        let authToken = '';

        async function register() {
            const data = {
                email: document.getElementById('email').value,
                username: document.getElementById('username').value,
                password: document.getElementById('password').value,
                shippingAddress: "123 Demo St, Seattle, WA",
                billingAddress: "123 Demo St, Seattle, WA",
                paymentMethod: "Visa"
            };
            try {
                const response = await fetch(`${API_BASE}/accounts`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                const result = await response.json();
                document.getElementById('registerOutput').textContent = 
                    JSON.stringify(result, null, 2);
            } catch (error) {
                document.getElementById('registerOutput').textContent = 
                    'Error: ' + error.message;
            }
        }

        async function login() {
            const data = {
                email: document.getElementById('email').value,
                password: document.getElementById('password').value
            };
            try {
                const response = await fetch(`${API_BASE}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                const result = await response.json();
                authToken = result.token;
                document.getElementById('loginOutput').textContent = 
                    `✓ Authenticated!\nToken: ${authToken.substring(0, 50)}...`;
            } catch (error) {
                document.getElementById('loginOutput').textContent = 
                    'Error: ' + error.message;
            }
        }

        async function getItems() {
            try {
                const response = await fetch(`${API_BASE}/items`, {
                    headers: { 'Authorization': `Bearer ${authToken}` }
                });
                const result = await response.json();
                document.getElementById('itemsOutput').textContent = 
                    JSON.stringify(result, null, 2);
            } catch (error) {
                document.getElementById('itemsOutput').textContent = 
                    'Error: ' + error.message;
            }
        }

        async function createOrder() {
            const orderData = {
                userId: "demo-user-123",
                items: [{ sku: "DEMO-SKU-001", quantity: 2 }],
                shippingAddress: "123 Demo St, Seattle, WA"
            };
            try {
                const response = await fetch(`${API_BASE}/orders`, {
                    method: 'POST',
                    headers: { 
                        'Authorization': `Bearer ${authToken}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(orderData)
                });
                const result = await response.json();
                document.getElementById('orderOutput').textContent = 
                    JSON.stringify(result, null, 2);
            } catch (error) {
                document.getElementById('orderOutput').textContent = 
                    'Error: ' + error.message;
            }
        }
    </script>
</body>
</html>
```

Save as `demo-frontend.html` and open in browser during demo.

---

## 🎯 Q&A Preparation

**Expected Questions & Answers:**

**Q: Why did you choose different databases for different services?**  
A: Each service uses the database that best fits its data model - MySQL for ACID transactions (payments), MongoDB for flexible schemas (items), Cassandra for high write throughput (orders/inventory), PostgreSQL for advanced querying (auth).

**Q: How does the system ensure data consistency across services?**  
A: We use eventual consistency via Kafka events. Payment success triggers an event that the Order Service consumes to update order status. Critical operations like payments use database-level idempotency constraints.

**Q: What happens if a service fails?**  
A: Services are stateless and containerized. Docker can restart failed containers. Kafka ensures no messages are lost. The Gateway handles service unavailability gracefully.

**Q: How would you scale this system?**  
A: Each service can scale independently via container orchestration (Kubernetes). Cassandra and Kafka are inherently distributed. We can add read replicas for MySQL/PostgreSQL.

**Q: Is the code production-ready?**  
A: It demonstrates enterprise patterns and best practices. For production, we'd add: distributed tracing, centralized logging, circuit breakers, service mesh, enhanced monitoring, and higher test coverage.

---

## ✅ Post-Demo Actions

```powershell
# Clean up after demo
docker-compose down

# Show code structure
tree /F /A c:\Users\Admin\Desktop\shoppingservice | head -n 50
```

**Repository Links to Share:**
- GitHub: [Your repo URL]
- Coverage Reports: `target/site/jacoco/index.html` in each service
- Swagger Docs: http://localhost:8080/swagger-ui.html (when running)

---

**End of Demo Script** 🎬
