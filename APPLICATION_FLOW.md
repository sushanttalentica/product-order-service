# Product Order Service - Complete Application Flow

## Overview

This document explains the complete flow of the Product Order Service application from start to end, covering each step with detailed examples and real-world scenarios.

## 1. Application Startup Flow

### 1.1 System Initialization
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              APPLICATION STARTUP FLOW                          │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Spring    │───▶│   Database  │───▶│    Redis    │───▶│    Kafka    │
│   Boot      │    │ Connection │    │ Connection  │    │ Connection  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Security  │    │   JPA       │    │   Cache     │    │   Events    │
│   Config    │    │   Setup     │    │   Manager   │    │   Setup     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

**Example Startup Logs:**
```
2024-01-01 10:00:00 - Starting ProductOrderServiceApplication
2024-01-01 10:00:01 - Connecting to database: jdbc:mysql://localhost:3306/ecommerce_db
2024-01-01 10:00:02 - Redis connection established: localhost:6379
2024-01-01 10:00:03 - Kafka producer initialized: localhost:9092
2024-01-01 10:00:04 - JWT security configuration loaded
2024-01-01 10:00:05 - Application started successfully on port 8080
```

### 1.2 Component Initialization
```java
// Example: Application startup sequence
@SpringBootApplication
@EnableCaching
@EnableKafka
public class ProductOrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductOrderServiceApplication.class, args);
    }
}
```

## 2. User Authentication Flow

### 2.1 Login Process
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              USER AUTHENTICATION FLOW                          │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Security   │───▶│   JWT       │
│  Request    │    │   (Nginx)   │    │   Filter    │    │  Generator  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   User      │            │
        │                   │            │  Database   │            │
        │                   │            └─────────────┘            │
        │                   │                                       │
        ▼                   ▼                                       ▼
┌─────────────┐    ┌─────────────┐                        ┌─────────────┐
│  Response   │    │   JWT       │                        │   Token     │
│   (JSON)    │    │   Token     │                        │  Storage    │
└─────────────┘    └─────────────┘                        └─────────────┘
```

**Example Authentication Request:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "customer123",
  "password": "securePassword123"
}
```

**Example Authentication Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "customer123",
    "roles": ["CUSTOMER"],
    "permissions": ["READ_PRODUCTS", "CREATE_ORDERS"]
  }
}
```

## 3. Product Management Flow

### 3.1 Product Search Flow
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCT SEARCH FLOW                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service    │───▶│   Cache      │
│  Request    │    │   (Nginx)   │    │   Layer     │    │  (Redis)     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Cache     │
        │                   │                   │            │   Hit       │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │  Database   │            │
        │                   │            │  (MySQL)    │            │
        │                   │            └─────────────┘            │
        │                   │                                       │
        ▼                   ▼                                       ▼
┌─────────────┐    ┌─────────────┐                        ┌─────────────┐
│  Response   │    │   Cache    │                        │   Cache     │
│   (JSON)    │    │   Update   │                        │   Store     │
└─────────────┘    └─────────────┘                        └─────────────┘
```

**Example Product Search Request:**
```http
GET /api/v1/products/search?name=iPhone&category=Electronics&minPrice=500&maxPrice=1500
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example Product Search Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "iPhone 15",
      "description": "Latest iPhone model",
      "price": 999.99,
      "sku": "IPHONE15-001",
      "stockQuantity": 50,
      "isActive": true,
      "category": {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 25,
  "totalPages": 2
}
```

### 3.2 Product Creation Flow (Admin)
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCT CREATION FLOW                            │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Admin     │───▶│   Gateway   │───▶│   Security  │───▶│   Service   │
│  Request    │    │   (Nginx)   │    │   Filter    │    │   Layer     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Product   │
        │                   │                   │            │  Creation   │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │  Database   │
        │                   │                   │            │  (MySQL)    │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Cache     │
        │                   │                   │            │  Invalidation│
        │                   │                   │            └─────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Response   │    │   Product   │    │   Cache     │
│   (JSON)    │    │   Created   │    │   Updated   │
└─────────────┘    └─────────────┘    └─────────────┘
```

**Example Product Creation Request:**
```http
POST /api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone Pro model with advanced features",
  "price": 1099.99,
  "sku": "IPHONE15PRO-001",
  "stockQuantity": 100,
  "categoryId": 1
}
```

**Example Product Creation Response:**
```json
{
  "id": 2,
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone Pro model with advanced features",
  "price": 1099.99,
  "sku": "IPHONE15PRO-001",
  "stockQuantity": 100,
  "isActive": true,
  "category": {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices"
  },
  "createdAt": "2024-01-01T10:30:00Z",
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

## 4. Order Processing Flow

### 4.1 Order Creation Flow
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ORDER CREATION FLOW                              │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Customer  │───▶│   Gateway   │───▶│   Service   │───▶│   Product   │
│  Request    │    │   (Nginx)   │    │   Layer     │    │ Validation  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Stock     │
        │                   │                   │            │  Check      │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Order     │            │
        │                   │            │  Creation   │            │
        │                   │            └─────────────┘            │
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │  Database   │            │
        │                   │            │  (MySQL)    │            │
        │                   │            └─────────────┘            │
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Kafka     │            │
        │                   │            │   Events    │            │
        │                   │            └─────────────┘            │
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Cache     │            │
        │                   │            │  Invalidation│          │
        │                   │            └─────────────┘            │
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐
│  Response   │    │   Order     │
│   (JSON)    │    │   Created   │
└─────────────┘    └─────────────┘
```

**Example Order Creation Request:**
```http
POST /api/v1/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "customerId": "customer123",
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99
    },
    {
      "productId": 2,
      "quantity": 1,
      "unitPrice": 1099.99
    }
  ]
}
```

**Example Order Creation Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 3099.97,
  "status": "PENDING",
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99,
      "totalPrice": 1999.98
    },
    {
      "id": 2,
      "productId": 2,
      "quantity": 1,
      "unitPrice": 1099.99,
      "totalPrice": 1099.99
    }
  ],
  "createdAt": "2024-01-01T10:30:00Z",
  "updatedAt": "2024-01-01T10:30:00Z"
}
```

### 4.2 Payment Processing Flow
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            PAYMENT PROCESSING FLOW                             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Order     │───▶│   Payment   │───▶│   gRPC      │───▶│  Payment    │
│   Service   │    │   Service   │    │   Client    │    │  Service    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Payment   │
        │                   │                   │            │ Authorization│
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Payment   │
        │                   │                   │            │  Processing │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Payment   │
        │                   │                   │            │   Result    │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Kafka     │            │
        │                   │            │   Events    │            │
        │                   │            └─────────────┘            │
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Order     │            │
        │                   │            │  Status    │            │
        │                   │            │  Update    │            │
        │                   │            └─────────────┘            │
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐
│   Order     │    │   Payment   │
│  Updated    │    │   Result    │
└─────────────┘    └─────────────┘
```

**Example Payment Processing Request:**
```http
POST /api/v1/orders/1/payment
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryDate": "12/25",
  "cvv": "123",
  "amount": 3099.97
}
```

**Example Payment Processing Response:**
```json
{
  "paymentId": "PAY-20240101-000001",
  "orderId": 1,
  "status": "AUTHORIZED",
  "amount": 3099.97,
  "transactionId": "TXN-123456789",
  "processedAt": "2024-01-01T10:35:00Z"
}
```

## 5. Event-Driven Architecture Flow

### 5.1 Order Event Processing
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EVENT-DRIVEN ARCHITECTURE                          │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Order     │───▶│   Kafka     │───▶│  Consumer   │───▶│ Notification│
│   Service   │    │  Producer   │    │  Services   │    │   Service   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Email     │
        │                   │                   │            │ Notification│
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │  Inventory  │            │
        │                   │            │   Service   │            │
        │                   │            └─────────────┘            │
        │                   │                   │                   │
        │                   │                   ▼                   │
        │                   │            ┌─────────────┐            │
        │                   │            │   Stock     │            │
        │                   │            │  Update    │            │
        │                   │            └─────────────┘            │
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐
│   Order    │    │   Events    │
│  Events    │    │  Published  │
└─────────────┘    └─────────────┘
```

**Example Order Event:**
```json
{
  "eventType": "ORDER_CREATED",
  "orderId": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 3099.97,
  "status": "PENDING",
  "timestamp": "2024-01-01T10:30:00Z",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99
    }
  ]
}
```

**Example Payment Event:**
```json
{
  "eventType": "PAYMENT_AUTHORIZED",
  "paymentId": "PAY-20240101-000001",
  "orderId": 1,
  "amount": 3099.97,
  "status": "AUTHORIZED",
  "timestamp": "2024-01-01T10:35:00Z"
}
```

## 6. Complete End-to-End Flow Example

### 6.1 Customer Journey: iPhone Purchase
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              COMPLETE CUSTOMER JOURNEY                         │
└─────────────────────────────────────────────────────────────────────────────────┘

Step 1: Customer Login
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Customer  │───▶│   Login     │───▶│   JWT       │───▶│   Access    │
│  Browser    │    │   API       │    │   Token     │    │  Granted    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Step 2: Product Search
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Customer  │───▶│   Search    │───▶│   Cache     │───▶│   Product   │
│  Browser    │    │   API       │    │  (Redis)    │    │   Results   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Step 3: Add to Cart
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Customer  │───▶│   Cart      │───▶│   Session   │───▶│   Cart      │
│  Browser    │    │   API       │    │  Storage    │    │  Updated    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Step 4: Checkout
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Customer  │───▶│   Order     │───▶│   Payment   │───▶│   Order     │
│  Browser    │    │   API       │    │  Service    │    │  Created    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Step 5: Payment Processing
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Payment   │───▶│   gRPC      │───▶│   Payment   │───▶│   Payment   │
│  Service    │    │   Client    │    │  Gateway    │    │  Authorized │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Step 6: Order Confirmation
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Order     │───▶│   Kafka     │───▶│ Notification│───▶│   Email     │
│  Service    │    │   Events    │    │   Service   │    │  Sent       │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

### 6.2 Detailed Step-by-Step Example

#### Step 1: Customer Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "customer123",
    "username": "john.doe@example.com",
    "roles": ["CUSTOMER"],
    "permissions": ["READ_PRODUCTS", "CREATE_ORDERS"]
  }
}
```

#### Step 2: Search for iPhone
```http
GET /api/v1/products/search?name=iPhone&category=Electronics
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "iPhone 15",
      "description": "Latest iPhone model",
      "price": 999.99,
      "sku": "IPHONE15-001",
      "stockQuantity": 50,
      "isActive": true,
      "category": {
        "id": 1,
        "name": "Electronics"
      }
    }
  ],
  "totalElements": 1
}
```

#### Step 3: Create Order
```http
POST /api/v1/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "customerId": "customer123",
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "orderItems": [
    {
      "productId": 1,
      "quantity": 1,
      "unitPrice": 999.99
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 999.99,
  "status": "PENDING",
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 1,
      "unitPrice": 999.99,
      "totalPrice": 999.99
    }
  ],
  "createdAt": "2024-01-01T10:30:00Z"
}
```

#### Step 4: Process Payment
```http
POST /api/v1/orders/1/payment
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryDate": "12/25",
  "cvv": "123",
  "amount": 999.99
}
```

**Response:**
```json
{
  "paymentId": "PAY-20240101-000001",
  "orderId": 1,
  "status": "AUTHORIZED",
  "amount": 999.99,
  "transactionId": "TXN-123456789",
  "processedAt": "2024-01-01T10:35:00Z"
}
```

#### Step 5: Order Confirmation
```http
GET /api/v1/orders/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 999.99,
  "status": "CONFIRMED",
  "orderItems": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 1,
      "unitPrice": 999.99,
      "totalPrice": 999.99
    }
  ],
  "createdAt": "2024-01-01T10:30:00Z",
  "updatedAt": "2024-01-01T10:35:00Z"
}
```

## 7. Error Handling Flow

### 7.1 Error Scenarios
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ERROR HANDLING FLOW                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service   │───▶│   Error     │
│  Request    │    │   (Nginx)   │    │   Layer     │    │  Handler    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Error     │
        │                   │                   │            │  Response   │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Logging   │
        │                   │                   │            │   System    │
        │                   │                   │            └─────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Error      │    │   Error     │    │   Error     │
│ Response    │    │  Logging    │    │ Monitoring  │
└─────────────┘    └─────────────┘    └─────────────┘
```

**Example Error Response:**
```json
{
  "timestamp": "2024-01-01T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "details": [
    {
      "field": "customerId",
      "message": "Customer ID is required"
    },
    {
      "field": "orderItems",
      "message": "At least one order item is required"
    }
  ]
}
```

## 8. Monitoring and Health Checks

### 8.1 Health Check Flow
```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              HEALTH CHECK FLOW                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Load      │───▶│   Gateway   │───▶│   Service   │───▶│   Health    │
│  Balancer   │    │   (Nginx)   │    │   Layer     │    │  Checker    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Database │
        │                   │                   │            │   Check    │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Redis     │
        │                   │                   │            │   Check    │
        │                   │                   │            └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   ▼
        │                   │                   │            ┌─────────────┐
        │                   │                   │            │   Kafka     │
        │                   │                   │            │   Check    │
        │                   │                   │            └─────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Health    │    │   Metrics   │    │   Alerts    │
│  Status     │    │  Collection │    │  Generated  │
└─────────────┘    └─────────────┘    └─────────────┘
```

**Example Health Check Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "host": "localhost:6379",
        "connection": "active"
      }
    },
    "kafka": {
      "status": "UP",
      "details": {
        "bootstrapServers": "localhost:9092"
      }
    }
  }
}
```

This comprehensive flow documentation covers the complete application lifecycle from startup to end-user interactions, including error handling and monitoring. Each step includes real-world examples and detailed explanations of the data flow through the system.
