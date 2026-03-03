# Online Shopping System - Backend Implementation Documentation

## System Overview
A microservices-based online shopping platform backend consisting of four core business services and an authentication service. The system supports account management, item browsing, order processing, and payment handling with comprehensive security and event-driven architecture.

## Architecture

### Services
1. **Item Service** - Manages product catalog and inventory
2. **Order Service** - Handles order lifecycle and state management
3. **Payment Service** - Processes payment transactions with idempotency
4. **Account Service** - Manages user accounts and authentication
5. **Authentication Server** - Provides JWT-based security and authorization

### Technology Stack
- **Framework**: Spring Boot with Maven
- **Security**: Spring Security with JWT tokens
- **API Documentation**: Swagger/OpenAPI
- **Testing**: JUnit, Mockito, PowerMock with Jacoco (30% coverage minimum)
- **Data Access**: Hibernate, Spring JPA
- **Microservices**: Spring Cloud (OpenFeign for synchronous communication)
- **Message Broker**: Apache Kafka for asynchronous event-driven communication
- **Databases**:
  - MongoDB - Item metadata storage
  - Cassandra - Order data storage
  - MySQL/PostgreSQL - Account data storage

## Service Specifications

### Item Service
**Responsibilities**:
- Store and retrieve item metadata (name, price, UPC, images)
- Manage inventory levels
- Provide real-time stock availability

**Database**: MongoDB (flexible schema for product attributes)

**APIs**:
- GET /items/{id} - Retrieve item details
- GET /items/search - Search items by criteria
- GET /items/{id}/inventory - Check stock availability
- PUT /items/{id}/inventory - Update inventory levels

### Order Service
**Responsibilities**:
- Manage order lifecycle with state transitions
- Support synchronous REST APIs
- Publish/consume Kafka events for asynchronous processing

**Database**: Cassandra (optimized for write-heavy order operations)

**Order States**: Created → Paid → Completed / Cancelled

**APIs**:
- POST /orders - Create new order
- PUT /orders/{id} - Update order
- DELETE /orders/{id} - Cancel order
- GET /orders/{id} - Lookup order details

**Kafka Topics**:
- order-created
- order-updated
- order-cancelled
- order-completed

### Payment Service
**Responsibilities**:
- Process payment transactions
- Ensure idempotency to prevent double-charging/refunding
- Publish payment status events

**APIs**:
- POST /payments - Submit payment
- PUT /payments/{id} - Update payment
- POST /payments/{id}/refund - Reverse payment
- GET /payments/{id} - Payment lookup

**Idempotency**: Implemented using unique transaction IDs and database constraints

### Account Service
**Responsibilities**:
- User registration and profile management
- Store user credentials and addresses
- Manage payment methods

**Database**: MySQL/PostgreSQL (structured relational data)

**APIs**:
- POST /accounts - Create account
- PUT /accounts/{id} - Update account
- GET /accounts/{id} - Account lookup
- GET /accounts/{id}/addresses - Retrieve shipping/billing addresses

**Data Model**:
- User credentials (email, username, hashed password)
- Shipping addresses
- Billing addresses
- Payment methods

### Authentication Server
**Responsibilities**:
- Validate user credentials
- Generate JWT tokens
- Authorize requests via token validation

**Flow**:
1. User logs in with credentials
2. Server validates and generates JWT token
3. Client includes token in Authorization header
4. Services validate token for protected endpoints

## Communication Patterns

### Synchronous Communication
- **Technology**: Spring Cloud OpenFeign / RestTemplate
- **Use Cases**:
  - Item Service → Inventory check during order creation
  - Order Service → Payment Service for payment processing
  - All Services → Authentication Server for token validation

### Asynchronous Communication
- **Technology**: Apache Kafka
- **Use Cases**:
  - Order Service publishes order events
  - Payment Service publishes payment confirmations
  - Inventory Service consumes order events to update stock

## Deployment

### Containerization
- All services dockerized with Docker Compose orchestration
- One-command deployment: `docker-compose up`
- Includes all service dependencies (databases, Kafka, Zookeeper)

### Repository Structure
 Final Project
Goal:
Design an online shopping website that which at least contains following services
● Item Service (including inventory service)
● Order Service
● Payment Service
● Account Service
On your website, customers should be able to create an account, select an item, add the item to
cart, check out (create an order), update order, and cancel order.
General requirements:
1. You are responsible for only the backend part of this project, you are recommended
to implement some sample web pages for demo purposes if you are able to do so.
2. Create one or multiple repositories for these services, dockerize services and their
dependencies, make sure your applications are “one-click” runnable.
3. You’re encouraged to team up, up to 3 members per team, and you’re supposed to
deliver a more deliberated, comprehensive solution than solo players.
4. Please share your git repo link and grant access to following Github userIds once
you started the project:
a. CTYue
b. JoshTWang
5. Tech Stack:
Spring boot, Maven, Spring Security, Junit/Mockito/PowerMock, Jacoco, Swagger,
Hibernate, Spring JPA, Spring Cloud and everything else we covered in class.
6. Use Spring Cloud OpenFeign and/or RestTemplate to implement inter-service
synchronous communications.
7. You are required to use all following databases: MySQL/PostgreSQL, MongoDB,
Cassandra.
8. You are required to use Kafka for event-driven asynchronous communication.
9. Unit testing coverage: at least 30% coverage for each service layer. Covering all
possible cases.
11. An authentication server is required, apart from the 4 business services we
mentioned (you can also implement the authentication server as part of Account
service), this server helps to secure services by providing authentication and
authorization facility, e.g. once a user logs into its account successfully, the server
generates a token, with this token as part of the request header, requests sent to
order service can be accepted.
Item Service:
The item service provides item related information including unit price, item name, item picture
urls, UPC (universal product code), item id.
Item Service stores everything about an item, we usually call such data as metadata, you may
use MongoDB to realize it as it’s difficult to finalize a schema for such data.
Item service is also responsible for inventory lookup and update, by returning remaining
available units of a product.
Order Service:
The order service supports both synchronous and asynchronous communications, it produces
Kafka messages and also consumes kafka messages.
Order Flow:
● Create Order (right after user clicks the submit button, an order will be created)
● Cancel Order
● Update Order
Order Service APIs:
Create Order
Cancel Order
Update Order
Order Lookup
While RESTful APIs remain stateless, each order itself has a state, for example.
Order Create, Order Paid, Order Completed, Order Cancel
Consider using Cassandra database for order service, and design a reasonable schema for
order information.
Please note that above APIs provide a synchronous approach, order service should publish
order information to some other services…
Payment Service:
integrates with order service, provides REST APIs, and publish payment transaction
results to consumers.
● Submit Payment
● Update Payment
● Reverse Payment: Refund
● Payment Lookup: lookup a payment, return its status
Idempotency should be guaranteed throughout the payment flow, as we don’t want to
double-charge customers or double-refund them.
Account Service:
● Create Account
● Update Account
● Account Lookup
● …
Users should be able to create/update their account with following necessary information:
● User Email
● User name
● Password
● Shipping Address
● Billing Address
● Payment Method
You may use MySQL or PostgreSQL for data persistence.