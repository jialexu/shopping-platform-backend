# Shopping Service Microservices Demo

## Architecture Overview
**7 Microservices + 4 Databases + Message Queue**
- Gateway (8080) - API Gateway with routing
- Auth Service (9000) - JWT authentication
- Account Service (9001) - User management (MySQL)
- Item Service (9002) - Product catalog (MongoDB)
- Inventory Service (9003) - Stock management (Cassandra)
- Order Service (9004) - Order processing (Cassandra + Kafka)
- Payment Service (9005) - Payment handling (PostgreSQL + Kafka)

---

## Demo Flow

### 1. Verify All Services Are Running
```powershell
# Check all health endpoints
@(9000, 9001, 9002, 9003, 9004, 9005, 8080) | ForEach-Object { 
    Invoke-WebRequest -Uri "http://localhost:$_/actuator/health" -UseBasicParsing | 
    Select-Object StatusCode, @{Name="Port";Expression={$_}} 
}
```

### 2. User Registration & Authentication

#### Register a new user (via Gateway)
```powershell
$registerBody = @{
    username = "demo_user"
    email = "demo@example.com"
    password = "SecurePass123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/accounts/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $registerBody
```

#### Login to get JWT token
```powershell
$loginBody = @{
    email = "demo@example.com"
    password = "SecurePass123"
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$token = ($loginResponse.Content | ConvertFrom-Json).token
Write-Host "JWT Token: $token"
```

### 3. Item Management (Product Catalog)

#### Create an item
```powershell
$itemBody = @{
    name = "Laptop Pro 15"
    description = "High-performance laptop"
    price = 1299.99
    category = "Electronics"
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Invoke-WebRequest -Uri "http://localhost:8080/api/items" `
    -Method POST `
    -Headers $headers `
    -Body $itemBody
```

#### List all items
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/items" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"} | 
    Select-Object -ExpandProperty Content | ConvertFrom-Json
```

### 4. Inventory Management

#### Add inventory for an item
```powershell
$inventoryBody = @{
    itemId = "YOUR_ITEM_ID"  # Replace with actual item ID
    quantity = 100
    location = "Warehouse-A"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/inventory" `
    -Method POST `
    -Headers $headers `
    -Body $inventoryBody
```

#### Check inventory
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/inventory/YOUR_ITEM_ID" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"} |
    Select-Object -ExpandProperty Content
```

### 5. Order & Payment Flow (Event-Driven with Kafka)

#### Create an order
```powershell
$orderBody = @{
    items = @(
        @{
            itemId = "YOUR_ITEM_ID"
            quantity = 2
            price = 1299.99
        }
    )
    totalAmount = 2599.98
} | ConvertTo-Json

$orderResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/orders" `
    -Method POST `
    -Headers $headers `
    -Body $orderBody

$orderId = ($orderResponse.Content | ConvertFrom-Json).id
Write-Host "Order Created: $orderId"
```

#### Process payment (triggers Kafka events)
```powershell
$paymentBody = @{
    orderId = $orderId
    amount = 2599.98
    paymentMethod = "CREDIT_CARD"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/payments" `
    -Method POST `
    -Headers $headers `
    -Body $paymentBody
```

**What happens behind the scenes:**
1. Payment service publishes `payment.succeeded` event to Kafka
2. Order service consumes the event and updates order status
3. Inventory service reduces stock automatically

#### Check order status
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/orders/$orderId" `
    -Method GET `
    -Headers @{"Authorization"="Bearer $token"} |
    Select-Object -ExpandProperty Content
```

### 6. Monitor Kafka Messages

**Open Kafka UI:** http://localhost:8090

Topics to check:
- `payment.succeeded` - Successful payment events
- `payment.failed` - Failed payment events
- `order.created` - New order events

---

## API Documentation (Swagger UI)

Access interactive API docs:
- **Gateway**: http://localhost:8080/swagger-ui.html
- **Auth Service**: http://localhost:9000/swagger-ui.html
- **Account Service**: http://localhost:9001/swagger-ui.html
- **Item Service**: http://localhost:9002/swagger-ui.html
- **Order Service**: http://localhost:9004/swagger-ui.html
- **Payment Service**: http://localhost:9005/swagger-ui.html

---

## Database Verification

### Check MySQL (Account data)
```powershell
docker exec -it shoppingservice-mysql-1 mysql -uuser -ppassword -e "SELECT * FROM account_db.users LIMIT 5;"
```

### Check MongoDB (Items data)
```powershell
docker exec -it shoppingservice-mongo-1 mongosh items_db --eval "db.items.find().pretty()"
```

### Check PostgreSQL (Payments data)
```powershell
docker exec -it shoppingservice-postgres-1 psql -U user -d payment_db -c "SELECT * FROM payments LIMIT 5;"
```

### Check Cassandra (Orders & Inventory)
```powershell
docker exec -it shoppingservice-cassandra-1 cqlsh -e "SELECT * FROM order_keyspace.orders LIMIT 5;"
docker exec -it shoppingservice-cassandra-1 cqlsh -e "SELECT * FROM inventory_keyspace.inventory LIMIT 5;"
```

---

## Key Features to Highlight

### ✅ Microservices Architecture
- Independent deployment and scaling
- Each service has its own database
- Clear separation of concerns

### ✅ API Gateway Pattern
- Single entry point for all clients
- Request routing and load balancing
- Centralized authentication

### ✅ Security
- JWT-based authentication
- OAuth2 Resource Server pattern
- Secure inter-service communication

### ✅ Polyglot Persistence
- MySQL for relational user data
- MongoDB for flexible document storage
- Cassandra for high-volume distributed data
- PostgreSQL for transactional payment data

### ✅ Event-Driven Architecture
- Apache Kafka for async messaging
- Decoupled services via events
- Real-time order/payment processing

### ✅ Observability
- Spring Boot Actuator health checks
- Centralized logging capability
- Monitoring-ready endpoints

### ✅ Production-Ready
- Docker containerization
- Docker Compose orchestration
- Proper error handling
- Data validation

---

## Troubleshooting Commands

### Restart all services
```powershell
docker-compose restart
```

### View logs for a specific service
```powershell
docker logs shoppingservice-order-service-1 --tail 50 -f
```

### Rebuild and restart
```powershell
docker-compose down
mvn clean package -DskipTests
docker-compose up -d
```

---

## Demo Talking Points

1. **Scalability**: Each service can be scaled independently
2. **Resilience**: Failure in one service doesn't bring down the system
3. **Technology Flexibility**: Different databases for different needs
4. **Async Processing**: Kafka enables non-blocking operations
5. **Developer Experience**: Swagger UI for easy API testing
6. **Cloud-Ready**: Containerized for easy cloud deployment

---

*Demo Duration: ~15-20 minutes*
*Questions? Check the README.md in each service directory*
