# Chuwa Shopping Platform - API Gateway

## Overview

The API Gateway service is the entry point for the Chuwa Shopping Platform, providing centralized routing, authentication, and cross-cutting concerns for all microservices. Built with Spring Cloud Gateway, it handles request routing, JWT token validation, CORS configuration, and request logging.

## Features

- **Request Routing**: Routes requests to appropriate microservices based on URL patterns
- **JWT Authentication**: Validates JWT tokens and forwards user information to downstream services
- **CORS Support**: Configures Cross-Origin Resource Sharing for web applications
- **Request Logging**: Logs all incoming requests with timestamps and client information
- **Health Checks**: Provides health endpoints for monitoring
- **Security**: Implements authentication filters for protected endpoints

## Architecture

The gateway serves as a reverse proxy and routes traffic to these services:

- **Auth Service** (port 9000): `/api/auth/**`
- **Account Service** (port 9001): `/api/accounts/**`
- **Item Service** (port 9002): `/api/items/**`
- **Inventory Service** (port 9003): `/api/inventory/**`
- **Order Service** (port 9004): `/api/orders/**`
- **Payment Service** (port 9005): `/api/payments/**`

## Security Model

### Public Endpoints
- `POST /api/auth/**` - Authentication endpoints
- `POST /api/accounts` - User registration
- `GET /api/items/**` - Public item browsing
- `/actuator/**` - Health check endpoints

### Protected Endpoints
- All other endpoints require a valid JWT token in the `Authorization` header with the format: `Bearer <token>`

### JWT Token Handling
- Validates JWT tokens using the shared secret
- Extracts user information from tokens
- Forwards user ID and username to downstream services via headers:
  - `X-User-Id`: User's unique identifier
  - `X-Username`: User's username

## Configuration

### Application Properties (`application.yml`)

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
      routes:
        # Service routes configuration
        
jwt:
  secret: your-jwt-secret-key-here
  expiration: 86400000  # 24 hours in milliseconds
```

### Environment Variables

- `JWT_SECRET`: JWT signing secret (override via environment)
- `JWT_EXPIRATION`: JWT token expiration time in milliseconds

## Key Components

### 1. GatewayApplication
Main Spring Boot application class that starts the gateway service.

### 2. AuthenticationFilter
Custom gateway filter that:
- Validates JWT tokens
- Extracts user information
- Forwards user context to downstream services
- Handles authentication errors

### 3. SecurityConfig
Spring Security configuration for:
- CORS setup
- Public endpoint permissions
- WebFlux security chain

### 4. LoggingFilter
Global filter for request/response logging with:
- Request timestamps
- HTTP methods and paths
- Client IP addresses
- Response status codes

### 5. JwtUtil
Utility class for JWT operations:
- Token generation
- Token validation
- Claims extraction
- Signature verification

### 6. HealthController
REST controller providing:
- Service health status
- Version information
- Runtime details

## API Endpoints

### Health Check
```
GET /health
GET /

Response:
{
  "status": "UP",
  "service": "API Gateway",
  "timestamp": "2025-11-13T18:30:00",
  "version": "1.0.0"
}
```

### Management Endpoints
- `/actuator/health` - Health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics
- `/actuator/gateway` - Gateway route information

## Request Flow

1. **Request Reception**: Gateway receives HTTP request
2. **Logging**: Request is logged with timestamp and client info
3. **CORS Processing**: CORS headers are applied if needed
4. **Route Matching**: Request path is matched against configured routes
5. **Authentication**: JWT token is validated for protected endpoints
6. **Header Enrichment**: User information is added to request headers
7. **Forwarding**: Request is forwarded to appropriate microservice
8. **Response Processing**: Response is returned to client

## Error Handling

### Authentication Errors
- **401 Unauthorized**: Missing, invalid, or expired JWT token
- Returns JSON error response with details

### Routing Errors
- **404 Not Found**: No matching route found
- **502 Bad Gateway**: Downstream service unavailable

## Testing

The service includes comprehensive unit tests covering:

- JWT token validation
- Authentication filter behavior
- Health endpoint functionality
- Security configuration

Run tests with:
```bash
mvn test
```

## Building and Running

### Local Development
```bash
# Build the application
mvn clean package

# Run the application
java -jar target/gateway-1.0.0.jar

# Or run with Maven
mvn spring-boot:run
```

### Docker
```bash
# Build Docker image
docker build -t icc-gateway .

# Run container
docker run -p 8080:8080 icc-gateway
```

### Environment Configuration
```bash
# Set JWT secret
export JWT_SECRET="your-secure-secret-key-here"

# Set JWT expiration (optional)
export JWT_EXPIRATION=86400000
```

## Monitoring

The gateway exposes several monitoring endpoints:

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Gateway Routes**: `/actuator/gateway/routes`

## Dependencies

Key dependencies include:

- **Spring Cloud Gateway**: Core routing and filtering
- **Spring Security**: Authentication and authorization
- **JJWT**: JWT token processing
- **Spring Boot Actuator**: Monitoring and management
- **Reactor Test**: Reactive testing support

## Development Notes

- Uses WebFlux reactive stack for non-blocking I/O
- Implements custom gateway filters for cross-cutting concerns
- Supports dynamic route configuration
- Provides comprehensive logging for debugging
- Includes extensive unit test coverage

## Performance Considerations

- Non-blocking reactive architecture
- Connection pooling for downstream services
- Efficient JWT token caching
- Optimized CORS preflight handling
- Minimal memory footprint for high throughput