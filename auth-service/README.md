# Auth Service

JWT Authentication and Authorization Service for Chuwa Shopping Platform.

## Overview

The Auth Service is responsible for:
- User authentication (login with email/password)
- JWT token generation
- Token validation
- Integration with Account Service for user verification

## Technical Stack

- **Spring Boot** - Main framework
- **Spring Security** - Security and authentication
- **Spring Cloud OpenFeign** - Service-to-service communication
- **JWT (jjwt)** - Token generation and validation
- **BCrypt** - Password encryption
- **Swagger/OpenAPI** - API documentation
- **JUnit 5 & Mockito** - Unit testing
- **Maven** - Build tool
- **Docker** - Containerization

## Architecture

### Components

1. **AuthController** - REST endpoints for authentication
2. **AuthService** - Business logic for authentication
3. **JwtService** - JWT token generation and parsing
4. **AccountClient** - Feign client to communicate with Account Service
5. **GlobalExceptionHandler** - Centralized error handling
6. **SecurityConfig** - Spring Security configuration
7. **PasswordConfig** - BCrypt password encoder configuration

### Security

- **JWT Secret**: Configured in `application.yml`
- **Token Expiration**: 3600 seconds (1 hour)
- **Password Encoding**: BCrypt
- **Public Endpoints**: `/api/auth/**`, `/swagger-ui/**`, `/actuator/health`

## API Endpoints

### POST /api/auth/login
Authenticate user and generate JWT token.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### GET /api/auth/validate
Validate a JWT token.

**Parameters:**
- `token` (query parameter): JWT token to validate

**Response:**
```json
true
```

## Configuration

### application.yml

```yaml
server:
  port: 9000

jwt:
  secret: "your-secret-key-here"
  expiration: 3600

spring:
  application:
    name: auth-service
```

### Environment Variables (Docker)

- `JWT_SECRET` - JWT signing secret (override application.yml)
- `ACCOUNT_SERVICE_URL` - Account service URL (default: http://account-service:9001)

## Running the Service

### Local Development

```bash
cd auth-service
mvn clean install
mvn spring-boot:run
```

### Docker

```bash
# Build
docker build -t auth-service:latest .

# Run
docker run -p 9000:9000 \
  -e JWT_SECRET=your-secret-key \
  auth-service:latest
```

### Docker Compose

The service is part of the main `docker-compose.yml`:

```bash
docker compose up -d auth-service
```

## Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

Jacoco coverage report:
```bash
mvn clean test jacoco:report
```

View report at: `target/site/jacoco/index.html`

**Target Coverage**: ≥ 30%

### Test Classes

- `AuthServiceTest` - Service layer tests
- `JwtServiceTest` - JWT token tests
- `AuthControllerTest` - Controller tests
- `PasswordConfigTest` - Password encoder tests

## Swagger Documentation

Access Swagger UI at:
- Local: http://localhost:9000/swagger-ui.html
- Docker: http://auth-service:9000/swagger-ui.html

OpenAPI JSON:
- http://localhost:9000/v3/api-docs

## Integration with Other Services

### Account Service

Auth Service calls Account Service to:
- Verify user credentials
- Retrieve password hash
- Get user information

**Endpoint Used**: `POST /api/accounts/_internal/auth`

## Health Check

Health check endpoint: `/actuator/health`

```bash
curl http://localhost:9000/actuator/health
```

## Error Handling

The service returns standardized error responses:

```json
{
  "message": "Invalid email or password",
  "status": 401,
  "timestamp": 1699900000000
}
```

**Error Codes:**
- `401 Unauthorized` - Invalid credentials
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Account service unavailable

## JWT Token Structure

```json
{
  "sub": "1",
  "email": "user@example.com",
  "iat": 1699900000,
  "exp": 1699903600
}
```

## Security Considerations

1. **JWT Secret**: Must be at least 256 bits for HS256
2. **Token Expiration**: Tokens expire after 1 hour
3. **Password Hashing**: BCrypt with default strength (10 rounds)
4. **HTTPS**: Should be used in production
5. **CORS**: Configure in API Gateway

## Logging

Logging levels configured in `application.yml`:
- `com.icc.auth`: DEBUG
- `org.springframework.security`: DEBUG
- `feign`: DEBUG

## Dependencies

Key dependencies in `pom.xml`:
- spring-boot-starter-web
- spring-boot-starter-security
- spring-cloud-starter-openfeign
- jjwt (0.11.5)
- springdoc-openapi (2.6.0)
- spring-boot-starter-test
- mockito

## Future Enhancements

- [ ] Refresh token support
- [ ] Token revocation/blacklist
- [ ] OAuth2 integration
- [ ] Multi-factor authentication
- [ ] Rate limiting
- [ ] Redis caching for tokens
