# Axon Microservices Project

This project is a comprehensive microservices-based system built with Spring Boot, Axon Framework, and Netflix Eureka
for service discovery. It demonstrates a modular architecture for e-commerce operations including order management,
product catalog, payment processing, user management using Event Sourcing and CQRS patterns.

## Architecture Overview

The system follows a **Domain-Driven Design (DDD)** approach with **Event Sourcing** and **CQRS** patterns implemented
through Axon Framework. It uses **Saga patterns** for distributed transaction management and **Event-Driven Architecture
** for service communication.

## Services Overview

### 1. core (Shared Module)

- **Purpose:** Centralized shared module containing common commands, events, models, and configurations used across all
  services.

### 2. order-service

- **Purpose:** Handles order creation, cancellation, and querying. Implements CQRS and Event Sourcing using Axon
  Framework with comprehensive saga orchestration.
- **Key Endpoints:**
  - `POST /api/orders` — Create a new order
  - `POST /api/orders/{orderId}/cancel` — Cancel an order
  - `GET /api/orders` — List all orders
  - `GET /api/orders/{orderId}` — Get specific order details
- **Features:**
  - Command and Query separation (CQRS)
  - Event sourcing for order lifecycle
  - **Saga orchestration** for distributed transaction management
  - **2-phase product reservation** with confirmation and release flows
  - **Timeout management** for reservation deadlines (20-second window)
  - **Compensation logic** for failed transactions
  - Validation and error handling
  - In-memory H2 database for persistence

### 3. product-service

- **Purpose:** Manages product catalog, inventory, and product reservation system with TTL-based management.
- **Key Features:**
    - Product catalog management
    - **Inventory reservation system** with time-based expiration
    - **Product reservation confirmation** workflow
    - **Automatic release** of expired reservations
    - Product mapping and transformation
    - Aggregate-based product management

### 4. payment-service

- **Purpose:** Handles payment processing, transaction management, and payment status tracking.
- **Key Features:**
    - **Credit card payment processing**
    - Payment transaction management
    - Payment status tracking (Pending, Completed, Failed, Refunded)
    - **Refund processing** capabilities
    - Payment projection and querying
    - Integration with OrderSaga for payment orchestration

### 5. user-service

- **Purpose:** Manages user accounts, authentication, and user profile information.
- **Key Features:**
    - User registration and management
    - User profile information
    - Payment details storage
    - Integration with order and payment services

### 6. api-gateway
- **Purpose:** Serves as the single entry point for all client requests, routing them to the appropriate backend services using Spring Cloud Gateway.
- **Key Features:**
    - Dynamic routing to all microservices
  - Service discovery via Eureka
  - Centralized API endpoint: all requests go through the gateway
- **Routes:**
  - `/api/orders/**` → order-service
  - `/api/products/**` → product-service
  - `/api/payments/**` → payment-service
  - `/api/users/**` → user-service

### 7. eureka-server
- **Purpose:** Provides service discovery for all microservices in the system.
- **Tech Stack:** Spring Boot, Netflix Eureka Server, Spring Security
- **Key Features:**
  - Central registry for all services
  - Secured with HTTP Basic Auth (default: admin/admin)
  - Web dashboard at `http://localhost:8761`

## Saga Pattern Implementation

The system implements **Saga patterns** for managing distributed transactions:

- **OrderSaga**: Orchestrates the complete order lifecycle
    - Product reservation with timeout management
    - Payment processing after successful reservations
    - 2-phase commit for product reservations
    - Comprehensive compensation for failed transactions
    - Deadline management for reservation timeouts

## Running the Project

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker (for Axon Server)

### 1. Start Axon Server (Event Store)

```bash
# Using Docker with custom ports to avoid conflicts
docker run -d --name axonserver -p 8025:8024 -p 8125:8124 \
  -v "~\docker-data\data":/axonserver/data \
  -v "~\docker-data\events":/axonserver/events \
  -v "~\docker-data\config":/axonserver/config \
  axoniq/axonserver
```

### 2. Start Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
# Access dashboard: http://localhost:8761 (admin/admin)
```

### 3. Start Core Services

```bash
# Start user-service
cd user-service && mvn spring-boot:run

# Start product-service  
cd product-service && mvn spring-boot:run

# Start payment-service
cd payment-service && mvn spring-boot:run
```

### 4. Start Order

```bash
# Start order-service
cd order-service && mvn spring-boot:run
```

### 5. Start API Gateway

```bash
cd api-gateway
mvn spring-boot:run
# Gateway runs on http://localhost:8080
```

## Service Ports

- **Eureka Server:** 8761
- **API Gateway:** 8080
- **Order Service:** 8081
- **Product Service:** 8082
- **Payment Service:** 8083
- **User Service:** 8084
- **Axon Server:** 8025 (HTTP), 8125 (gRPC)

## Example API Usage

### Create Order

```http
POST /api/orders
Content-Type: application/json
{
  "customerId": "user-123",
  "items": [
    { "productId": "prod-456", "quantity": 2, "unitPrice": 25.0 }
  ],
  "totalAmount": 50.0,
  "shippingAddress": "123 Main St, City, Country"
}
```

### Cancel Order

```http
POST /api/orders/{orderId}/cancel?reason=CustomerRequest
```

### List Orders

```http
GET /api/orders
```

### Get Specific Order

```http
GET /api/orders/{orderId}
```

### Product Operations

```http
GET /api/products
GET /api/products/{productId}
POST /api/products
```

### Payment Operations

```http
GET /api/payments
GET /api/payments/{paymentId}
POST /api/payments/refund/{paymentId}
```

## Key Features

- **Event Sourcing**: Complete audit trail of all system changes
- **CQRS**: Separate read and write models for optimal performance
- **Saga Orchestration**: Distributed transaction management
- **Timeout Management**: Automatic cleanup of expired reservations
- **Compensation Logic**: Rollback mechanisms for failed transactions
- **Service Discovery**: Dynamic service registration and discovery
- **API Gateway**: Centralized routing and load balancing

## Security

- Eureka dashboard is protected with HTTP Basic Auth (admin/admin)
- Service-to-service communication can be secured as needed
- Blockchain integration provides additional security through smart contracts

## Development

### Building the Project

```bash
# Build all modules
mvn clean install

# Build specific module
cd order-service && mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific module tests
cd order-service && mvn test
```

---

*This project demonstrates advanced microservices patterns including Event Sourcing, CQRS, Saga orchestration, and
blockchain integration for a comprehensive e-commerce solution.* 