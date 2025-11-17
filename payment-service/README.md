# Payment Service

Payment Service is a payment management microservice for the online shopping platform, responsible for handling all payment-related business logic.

## Features

### Core Functions
- **Payment Creation**: Create new payment records
- **Payment Status Update**: Update payment status (success, failure, etc.)
- **Payment Query**: Query payment information by payment ID or order ID
- **Payment Refund**: Handle payment refund requests
- **Idempotency**: Support idempotent handling of duplicate requests

### Technical Features
- **Database**: MySQL (payment record persistence)
- **Message Queue**: Kafka (async event notification)
- **Security**: JWT authentication + Spring Security
- **API Documentation**: Swagger/OpenAPI 3.0
- **Monitoring**: Spring Boot Actuator
- **Containerization**: Docker support

## API Endpoints

### Payment Management
- `POST /api/payments` - Create payment
- `PUT /api/payments/{id}` - Update payment status
- `GET /api/payments/{id}` - Get payment information by ID
- `GET /api/payments/order/{orderId}` - Get payment by order ID
- `POST /api/payments/{id}/refund` - Request refund

### Health Check
- `GET /actuator/health` - Service health status

### API Documentation
- `GET /swagger-ui.html` - Swagger UI interface
- `GET /v3/api-docs` - OpenAPI specification

## Data Model

### Payment Entity
```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'REFUNDED') NOT NULL,
    provider_ref VARCHAR(255),
    payment_method VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Payment Status
- `PENDING` - Pending processing
- `PROCESSING` - Processing
- `SUCCEEDED` - Payment successful
- `FAILED` - Payment failed
- `CANCELLED` - Cancelled
- `REFUNDED` - Refunded

## Kafka Events

### Produced Events
- `payment.succeeded` - Payment success event
- `payment.failed` - Payment failure event
- `payment.refunded` - Refund completed event

### Event Format
```json
{
    "paymentId": 123,
    "orderId": "ORDER-001",
    "amount": 100.00,
    "status": "SUCCEEDED",
    "eventType": "PAYMENT_SUCCEEDED",
    "timestamp": "2025-01-01T12:00:00"
}
```

## Configuration

### Environment Variables
- `MYSQL_URL` - MySQL database connection URL
- `MYSQL_USERNAME` - Database username
- `MYSQL_PASSWORD` - Database password
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka server address
- `JWT_SECRET` - JWT signing key

### Port Configuration
- Service port: `9005`
- Database port: `3306` (MySQL)
- Kafka port: `9092`

## Security Configuration

### Authentication Method
- JWT Token authentication
- Role-based access control (RBAC)

### Role Permissions
- `ROLE_USER` - Regular user permissions
  - Create payments
  - Query own payment information
- `ROLE_ADMIN` - Administrator permissions
  - Update payment status
  - Process refunds
  - Query all payment information

## Build and Run

### Local Development
```bash
# Compile project
mvn clean compile

# Run tests
mvn test

# Start service
mvn spring-boot:run
```

### Docker Deployment
```bash
# Build image
docker build -t payment-service:1.0.0 .

# Run container
docker run -p 9005:9005 \
  -e MYSQL_URL=jdbc:mysql://mysql:3306/payment_db \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  payment-service:1.0.0
```

### Docker Compose
```yaml
version: '3.8'
services:
  payment-service:
    build: .
    ports:
      - "9005:9005"
    environment:
      - MYSQL_URL=jdbc:mysql://mysql:3306/payment_db
      - MYSQL_USERNAME=payment_user
      - MYSQL_PASSWORD=payment_password
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    depends_on:
      - mysql
      - kafka
```

## Test Coverage

### Unit Tests
- Service layer tests - 100% method coverage
- Repository layer tests - Main query method coverage
- Controller layer tests - Main endpoint coverage

### Coverage Requirements
- Overall code coverage ≥ 30% (Jacoco)
- Test framework: JUnit 5 + Mockito

## Monitoring and Logging

### Health Checks
- Spring Boot Actuator provides health check endpoints
- Database connection status check support
- Kafka connection status check support

### Logging Configuration
- Console log output
- Support for different log level configuration
- Includes request tracing information

## Error Handling

### Global Exception Handling
- `PaymentNotFoundException` - Payment record does not exist
- `DuplicatePaymentException` - Duplicate payment
- `InvalidPaymentStateException` - Invalid payment state change
- `ValidationException` - Request parameter validation failure

### Error Response Format
```json
{
    "code": "PAYMENT_NOT_FOUND",
    "message": "Payment not found with id: 123",
    "timestamp": "2025-01-01T12:00:00",
    "validationErrors": null
}
```

## Performance Considerations

### Database Optimization
- Indexes based on order_id and status
- Connection pool configuration optimization
- Transaction management

### Asynchronous Processing
- Payment processing executes asynchronously
- Kafka events sent asynchronously
- Support for high concurrent payment requests

## Deployment Instructions

### Prerequisites
1. MySQL database service
2. Kafka message queue service
3. Auth Service (JWT authentication)

### Service Discovery
- Support access through API Gateway routing
- Path prefix: `/api/payments/**`

### Scalability
- Support horizontal scaling
- Stateless design
- Support load balancing

## Development Team

- **Service**: Payment service core business logic
- **Repository**: Data access layer
- **Controller**: REST API controller
- **Config**: Configuration management
- **Exception**: Exception handling
- **DTO**: Data transfer objects

## Version Information

- **Version**: 1.0.0
- **Java**: 17+
- **Spring Boot**: 3.3.4
- **Maven**: 3.6+