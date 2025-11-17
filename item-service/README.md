# Item Service

Item Service is a microservice responsible for managing item metadata in the Chuwa shopping platform.

## Features

- Create, read, update, delete items
- MongoDB storage for flexible item attributes
- Unique UPC code validation
- RESTful API with comprehensive documentation
- JWT-based authentication
- Comprehensive error handling
- Unit tests with Mockito

## API Endpoints

### Public Endpoints
- `GET /swagger-ui.html` - API Documentation
- `GET /actuator/health` - Health check

### Protected Endpoints (Requires JWT)
- `POST /api/items` - Create new item
- `PUT /api/items/{id}` - Update item by ID
- `GET /api/items/{id}` - Get item by ID
- `GET /api/items/by-upc/{upc}` - Get item by UPC code
- `GET /api/items` - Get paginated list of items
- `DELETE /api/items/{id}` - Delete item by ID

## Data Model

### Item Entity
```json
{
  "id": "string",
  "name": "string",
  "price": "number",
  "upc": "string",
  "pictures": ["string"],
  "attributes": {
    "key": "value"
  },
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Configuration

### Application Properties
- `server.port`: Service port (default: 9002)
- `spring.data.mongodb.uri`: MongoDB connection string
- `spring.security.oauth2.resourceserver.jwt.jwk-set-uri`: JWT validation endpoint

### Environment Variables
- `MONGO_URI`: MongoDB connection string
- `JWT_JWK_SET_URI`: JWT keys endpoint
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka connection

## Dependencies

- Spring Boot 3.x
- Spring Data MongoDB
- Spring Security OAuth2 Resource Server
- Spring Kafka
- SpringDoc OpenAPI (Swagger)
- JUnit 5 & Mockito for testing

## Running the Service

### Local Development
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t item-service .
docker run -p 9002:9002 item-service
```

### With Docker Compose
```bash
docker-compose up item-service
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Test Coverage
The service maintains >30% test coverage as measured by JaCoCo.

## Health Checks

- Health endpoint: `GET /actuator/health`
- Metrics endpoint: `GET /actuator/metrics`

## Integration

### Dependencies
- **Auth Service**: For JWT validation
- **MongoDB**: For data persistence
- **Kafka**: For event messaging

### Used By
- **Order Service**: For item price validation
- **API Gateway**: For routing requests

## Error Handling

The service provides comprehensive error handling with appropriate HTTP status codes:

- `400 Bad Request`: Validation errors
- `404 Not Found`: Item not found
- `409 Conflict`: Duplicate UPC code
- `500 Internal Server Error`: Unexpected errors

## Security

- JWT-based authentication for all business endpoints
- Public access for documentation and health checks
- OAuth2 Resource Server configuration