# Inventory Service

Inventory Service is a microservice for inventory management in the online shopping platform backend system, responsible for managing product inventory, reserving inventory, and releasing inventory.

## Features

- **Inventory Query**: Query available inventory, reserved inventory, and total inventory by SKU
- **Inventory Reservation**: Reserve specified quantities of inventory for orders
- **Inventory Release**: Release reserved inventory (when orders are canceled or payment fails)
- **Kafka Event Processing**: Automatically process order cancellation events and release corresponding inventory
- **Inventory Event Recording**: Record all inventory change history

## Technology Stack

- **Spring Boot**: Microservice framework
- **Spring Data Cassandra**: Cassandra database access
- **Spring Kafka**: Message queue integration
- **Spring Security**: Security control
- **Swagger/OpenAPI**: API documentation
- **Cassandra**: Distributed database storage
- **Docker**: Containerized deployment

## Database Design

### Table Structure

#### inventory_by_sku
- `sku` (text): Product SKU, primary key
- `available` (int): Available inventory quantity
- `reserved` (int): Reserved inventory quantity
- `updated_at` (timestamp): Last update time

#### inventory_events
- `sku` (text): Product SKU
- `timeuuid` (timeuuid): Time UUID for ordering
- `delta` (int): Inventory change amount (positive for increase, negative for decrease)
- `order_id` (text): Associated order ID
- `type` (text): Event type (RESERVE/RELEASE/INITIAL)
- `created_at` (timestamp): Event creation time

## API Endpoints

### 1. Query Inventory
```http
GET /api/inventory/{sku}
```

**Response Example:**
```json
{
  "sku": "SAMPLE-001",
  "available": 100,
  "reserved": 20,
  "total": 120
}
```

### 2. Reserve Inventory
```http
POST /api/inventory/reserve
Content-Type: application/json

{
  "sku": "SAMPLE-001",
  "quantity": 5,
  "orderId": "ORDER-123"
}
```

### 3. Release Inventory
```http
POST /api/inventory/release
Content-Type: application/json

{
  "sku": "SAMPLE-001",
  "quantity": 5,
  "orderId": "ORDER-123"
}
```

### 4. Initialize Inventory
```http
POST /api/inventory/{sku}/init?quantity=100
```

## Kafka Events

### Consumed Events
- **Topic**: `order.cancelled`
- **Processing**: Automatically release reserved inventory related to orders

**Event Format:**
```json
{
  "orderId": "ORDER-123",
  "sku": "SAMPLE-001",
  "quantity": 5
}
```

## Configuration

### Main application.yml Configuration

```yaml
server:
  port: 9003

spring:
  data:
    cassandra:
      keyspace-name: inventory_keyspace
      contact-points: localhost
      port: 9042
      local-datacenter: datacenter1
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: inventory-service-group
```

## Build and Run

### Local Development

1. Ensure Cassandra and Kafka services are running
2. Create database table structure (using `src/main/resources/db/schema.cql`)
3. Run the application:
```bash
mvn spring-boot:run
```

### Docker Deployment

```bash
# Build image
mvn clean package
docker build -t inventory-service .

# Run container
docker run -p 9003:9003 inventory-service
```

### Run the entire system via Docker Compose

```bash
# Run in parent directory
docker-compose up -d
```

## Testing

Run unit tests:
```bash
mvn test
```

Coverage report:
```bash
mvn test jacoco:report
```

## API Documentation

After starting the service, access Swagger UI:
```
http://localhost:9003/swagger-ui.html
```

## Monitoring and Health Check

- **Health Check**: `http://localhost:9003/actuator/health`
- **Application Info**: `http://localhost:9003/actuator/info`

## Troubleshooting

### Common Issues

1. **Cassandra Connection Failure**
   - Check if Cassandra service is started
   - Confirm if keyspace has been created

2. **Kafka Connection Failure**
   - Check if Kafka service is started
   - Confirm if topic has been created

3. **Inventory Reservation Failure**
   - Check if inventory is sufficient
   - Verify if SKU exists

### Log Level

Recommended to set log level to DEBUG in development environment:
```yaml
logging:
  level:
    com.icc.inventoryservice: DEBUG
```

## Business Flows

### Order Creation Flow
1. Order Service calls Inventory Service to reserve inventory
2. If inventory is sufficient, reservation succeeds, inventory moves from available to reserved
3. If inventory is insufficient, reservation fails, Order Service handles insufficient inventory situation

### Order Cancellation Flow
1. Order Service sends order cancellation event to Kafka
2. Inventory Service consumes the event and automatically releases reserved inventory
3. Inventory returns from reserved to available

### Payment Completion Flow
1. After Payment Service completes payment, Order Service updates order status
2. Reserved inventory remains in reserved status, indicating it has been sold
3. Alternatively, can choose to clear reserved inventory to zero, indicating it has been shipped