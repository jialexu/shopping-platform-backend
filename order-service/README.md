# Order Service

This is the order service for the online shopping platform, responsible for handling order creation, updates, cancellation, and other operations.

## Features

- Order creation (with inventory validation and reservation)
- Order querying
- Order updates
- Order cancellation
- Kafka event publishing and consumption
- JWT security authentication
- Swagger API documentation

## Technology Stack

- Spring Boot 3.x
- Spring Security (JWT Resource Server)
- Spring Data Cassandra
- Spring Kafka
- OpenFeign (Inter-service communication)
- Docker

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{id}` | Get order details |
| PUT | `/api/orders/{id}` | Update order |
| DELETE | `/api/orders/{id}` | Cancel order |

## Order Status Flow

```
CREATED -> PENDING_PAYMENT -> PAID -> COMPLETED
    |              |           |
    v              v           v
CANCELLED     CANCELLED   CANCELLED
```

## Dependent Services

- **Item Service**: Get product information and price validation
- **Inventory Service**: Inventory queries and reservation/release
- **Cassandra**: Data storage
- **Kafka**: Event messaging

## Kafka Topics

### Producer Topics
- `order.created`: Order creation event
- `order.inventory_reserved`: Inventory reservation success event
- `order.cancelled`: Order cancellation event

### Consumer Topics
- `payment.succeeded`: Payment successful (update order status to PAID)
- `payment.failed`: Payment failed (cancel order)

## Configuration

Key configuration items:

```yaml
server:
  port: 9004

cassandra:
  contact-points: localhost
  port: 9042
  keyspace: orderservice
  datacenter: datacenter1

app:
  services:
    item-service:
      url: http://item-service:9002
    inventory-service:
      url: http://inventory-service:9003
```

## Local Development

### Prerequisites

1. Ensure Cassandra is running on localhost:9042
2. Ensure Kafka is running on localhost:9092
3. Item Service and Inventory Service are running

### Commands

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn package

# Run
java -jar target/order-service-1.0.0.jar
```

### Docker Deployment

```bash
# Build image
docker build -t order-service:latest .

# Run container
docker run -p 9004:9004 \
  -e CASSANDRA_CONTACT_POINTS=cassandra \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e ITEM_SERVICE_URL=http://item-service:9002 \
  -e INVENTORY_SERVICE_URL=http://inventory-service:9003 \
  order-service:latest
```

## Swagger API Documentation

After service startup, visit: http://localhost:9004/swagger-ui.html

## Database Initialization

Create the following tables in Cassandra:

```sql
CREATE KEYSPACE IF NOT EXISTS orderservice 
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE orderservice;

CREATE TABLE IF NOT EXISTS orders (
    id text PRIMARY KEY,
    user_id text,
    total_amount decimal,
    status text,
    created_at timestamp,
    updated_at timestamp,
    shipping_address text
);

CREATE TABLE IF NOT EXISTS order_items (
    order_id text,
    sku text,
    qty int,
    unit_price decimal,
    PRIMARY KEY (order_id, sku)
);
```

## Health Checks

- Health Check: `GET /actuator/health`
- Metrics: `GET /actuator/metrics`