# Axon Microservices Project

This project is a microservices-based system built with Spring Boot, Axon Framework, and Netflix Eureka for service discovery. It demonstrates a modular architecture for order management, API gateway routing, and service registration/discovery.

## Services Overview

### 1. order-service
- **Purpose:** Handles order creation, cancellation, and querying. Implements CQRS and Event Sourcing using Axon Framework.
- **Tech Stack:** Spring Boot, Axon Framework, Spring Data JPA (H2 DB), Eureka Client
- **Key Endpoints:**
  - `POST /api/orders` — Create a new order
  - `POST /api/orders/{orderId}/cancel` — Cancel an order
  - `GET /api/orders` — List all orders
- **Features:**
  - Command and Query separation (CQRS)
  - Event sourcing for order lifecycle
  - Validation and error handling
  - In-memory H2 database for persistence

### 2. api-gateway
- **Purpose:** Serves as the single entry point for all client requests, routing them to the appropriate backend services using Spring Cloud Gateway.
- **Tech Stack:** Spring Boot, Spring Cloud Gateway, Eureka Client
- **Key Features:**
  - Dynamic routing to microservices (order-service, shipment-service, payment-service, blockchain-integration)
  - Service discovery via Eureka
  - Centralized API endpoint: all requests go through the gateway
- **Example Routes:**
  - `/api/orders/**` → order-service
  - `/api/shipments/**` → shipment-service
  - `/api/payments/**` → payment-service
  - `/api/blockchain/**` → blockchain-integration

### 3. eureka-server
- **Purpose:** Provides service discovery for all microservices in the system.
- **Tech Stack:** Spring Boot, Netflix Eureka Server, Spring Security
- **Key Features:**
  - Central registry for all services
  - Secured with HTTP Basic Auth (default: admin/admin)
  - Web dashboard at `http://localhost:8761`

## Running the Project

1. **Start Eureka Server**
   - Go to `eureka-server` directory
   - Run: `mvn spring-boot:run`
   - Access dashboard: [http://localhost:8761](http://localhost:8761)

2. **Start API Gateway**
   - Go to `api-gateway` directory
   - Run: `mvn spring-boot:run`
   - Gateway runs on [http://localhost:8080](http://localhost:8080)

3. **Start Order Service**
   - Go to `order-service` directory
   - Run: `mvn spring-boot:run`
   - Service runs on [http://localhost:8081](http://localhost:8081)

> **Note:** All services must be registered with Eureka to be discoverable by the gateway.

## Example API Usage

- **Create Order:**
  ```http
  POST /api/orders
  Content-Type: application/json
  {
    "customerId": "string",
    "items": [
      { "productId": "string", "quantity": 1, "unitPrice": 10.0 }
    ],
    "totalAmount": 10.0,
    "shippingAddress": "string"
  }
  ```

- **Cancel Order:**
  ```http
  POST /api/orders/{orderId}/cancel?reason=CustomerRequest
  ```

- **List Orders:**
  ```http
  GET /api/orders
  ```

## Service Ports
- **Eureka Server:** 8761
- **API Gateway:** 8080
- **Order Service:** 8081

## Security
- Eureka dashboard is protected with HTTP Basic Auth (admin/admin).

## Requirements
- Java 17+
- Maven 3.6+

---

*This README covers only the order-service, api-gateway, and eureka-server. Other services are not described here.* 