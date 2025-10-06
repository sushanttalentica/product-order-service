# Product Order Service - API Documentation

## Overview

The Product Order Service provides RESTful APIs for product management, order processing, and system administration. The APIs follow REST principles and implement proper HTTP status codes, authentication, and error handling.

## API Base Information

- **Base URL**: `https://api.product-order-service.com/api/v1`
- **Authentication**: JWT Bearer Token
- **Content-Type**: `application/json`
- **API Version**: v1

## Authentication

### JWT Token Structure
```json
{
  "sub": "user123",
  "iat": 1640995200,
  "exp": 1641081600,
  "roles": ["CUSTOMER", "ADMIN"],
  "permissions": ["READ_PRODUCTS", "CREATE_ORDERS"]
}
```

### Authentication Headers
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

## API Endpoints

### 1. Product Management APIs

#### 1.1 Get All Products
```http
GET /api/v1/products
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: id)
- `direction` (optional): Sort direction (ASC/DESC, default: ASC)

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
        "name": "Electronics",
        "description": "Electronic devices"
      },
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

#### 1.2 Get Product by ID
```http
GET /api/v1/products/{productId}
```

**Path Parameters:**
- `productId`: Product ID (required)

**Response:**
```json
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
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### 1.3 Get Product by SKU
```http
GET /api/v1/products/sku/{sku}
```

**Path Parameters:**
- `sku`: Product SKU (required)

**Response:**
```json
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
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### 1.4 Search Products
```http
GET /api/v1/products/search
```

**Query Parameters:**
- `name` (optional): Product name to search
- `category` (optional): Category name to filter
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter
- `inStock` (optional): Filter by stock availability (true/false)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

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

#### 1.5 Create Product (Admin Only)
```http
POST /api/v1/products
```

**Request Body:**
```json
{
  "name": "iPhone 15",
  "description": "Latest iPhone model",
  "price": 999.99,
  "sku": "IPHONE15-001",
  "stockQuantity": 50,
  "categoryId": 1
}
```

**Response:**
```json
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
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### 1.6 Update Product (Admin Only)
```http
PUT /api/v1/products/{productId}
```

**Path Parameters:**
- `productId`: Product ID (required)

**Request Body:**
```json
{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone Pro model",
  "price": 1099.99,
  "stockQuantity": 75
}
```

**Response:**
```json
{
  "id": 1,
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone Pro model",
  "price": 1099.99,
  "sku": "IPHONE15-001",
  "stockQuantity": 75,
  "isActive": true,
  "category": {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices"
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z"
}
```

#### 1.7 Delete Product (Admin Only)
```http
DELETE /api/v1/products/{productId}
```

**Path Parameters:**
- `productId`: Product ID (required)

**Response:**
```http
HTTP/1.1 204 No Content
```

### 2. Order Management APIs

#### 2.1 Create Order
```http
POST /api/v1/orders
```

**Request Body:**
```json
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
      "unitPrice": 199.99
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
  "totalAmount": 2199.97,
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
      "unitPrice": 199.99,
      "totalPrice": 199.99
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### 2.2 Get Order by ID
```http
GET /api/v1/orders/{orderId}
```

**Path Parameters:**
- `orderId`: Order ID (required)

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 2199.97,
  "status": "CONFIRMED",
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
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T01:00:00Z"
}
```

#### 2.3 Get Orders by Customer
```http
GET /api/v1/orders/customer/{customerId}
```

**Path Parameters:**
- `customerId`: Customer ID (required)

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `status` (optional): Order status filter

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-20240101-000001",
      "customerId": "customer123",
      "totalAmount": 2199.97,
      "status": "CONFIRMED",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 5,
  "totalPages": 1
}
```

#### 2.4 Update Order Status (Admin Only)
```http
PUT /api/v1/orders/{orderId}/status
```

**Path Parameters:**
- `orderId`: Order ID (required)

**Request Body:**
```json
{
  "status": "PROCESSING"
}
```

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 2199.97,
  "status": "PROCESSING",
  "updatedAt": "2024-01-01T02:00:00Z"
}
```

#### 2.5 Cancel Order
```http
PUT /api/v1/orders/{orderId}/cancel
```

**Path Parameters:**
- `orderId`: Order ID (required)

**Response:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20240101-000001",
  "customerId": "customer123",
  "totalAmount": 2199.97,
  "status": "CANCELLED",
  "updatedAt": "2024-01-01T03:00:00Z"
}
```

### 3. Category Management APIs

#### 3.1 Get All Categories
```http
GET /api/v1/categories
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and gadgets",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  {
    "id": 2,
    "name": "Clothing",
    "description": "Fashion and apparel",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
]
```

#### 3.2 Get Category by ID
```http
GET /api/v1/categories/{categoryId}
```

**Path Parameters:**
- `categoryId`: Category ID (required)

**Response:**
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices and gadgets",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### 3.3 Create Category (Admin Only)
```http
POST /api/v1/categories
```

**Request Body:**
```json
{
  "name": "Books",
  "description": "Books and literature"
}
```

**Response:**
```json
{
  "id": 3,
  "name": "Books",
  "description": "Books and literature",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### 4. Health and Monitoring APIs

#### 4.1 Health Check
```http
GET /actuator/health
```

**Response:**
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
    "kafka": {
      "status": "UP",
      "details": {
        "bootstrapServers": "localhost:9092"
      }
    },
    "cache": {
      "status": "UP",
      "details": {
        "type": "Caffeine"
      }
    }
  }
}
```

#### 4.2 Application Info
```http
GET /actuator/info
```

**Response:**
```json
{
  "app": {
    "name": "Product Order Service",
    "version": "1.0.0",
    "description": "E-commerce microservice for product and order management"
  },
  "build": {
    "version": "1.0.0",
    "time": "2024-01-01T00:00:00Z"
  }
}
```

#### 4.3 Metrics
```http
GET /actuator/metrics
```

**Response:**
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "jvm.gc.pause",
    "process.cpu.usage"
  ]
}
```

#### 4.4 Prometheus Metrics
```http
GET /actuator/prometheus
```

**Response:**
```
# HELP jvm_memory_used_bytes Used memory in bytes
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 1.23456789E8

# HELP http_server_requests_seconds HTTP server request duration
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{method="GET",status="200",uri="/api/v1/products",le="0.1"} 100
```

## Data Flow Diagrams

### 1. Product Search Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCT SEARCH FLOW                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service   │───▶│   Cache     │
│  Request    │    │   (Nginx)   │    │   Layer    │    │ (Caffeine)  │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                           │                   │
                           │                   ▼
                           │            ┌─────────────┐
                           │            │  Database   │
                           │            │  (MySQL)    │
                           │            └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │  Response   │
                   │   (JSON)    │
                   └─────────────┘
```

### 2. Order Creation Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ORDER CREATION FLOW                              │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service   │───▶│  Database   │
│  Request    │    │   (Nginx)   │    │   Layer    │    │  (MySQL)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                           │                   │
                           │                   ▼
                           │            ┌─────────────┐
                           │            │   Kafka     │
                           │            │   Events    │
                           │            └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │  Response   │
                   │   (JSON)    │
                   └─────────────┘
```

### 3. Payment Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            PAYMENT PROCESSING FLOW                             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Service   │───▶│   gRPC      │───▶│  Payment   │───▶│  Database   │
│   Layer    │    │   Client    │    │  Service    │    │  (MySQL)    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │
        │                   │                   ▼
        │                   │            ┌─────────────┐
        │                   │            │   Kafka     │
        │                   │            │   Events    │
        │                   │            └─────────────┘
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐
│  Response   │    │  Response   │
│   (JSON)    │    │   (gRPC)    │
└─────────────┘    └─────────────┘
```

### 4. Event-Driven Architecture Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EVENT-DRIVEN ARCHITECTURE                           │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Service   │───▶│   Kafka     │───▶│  Consumer   │───▶│  External   │
│   Layer     │    │  Producer   │    │  Services   │    │  Services   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │
        │                   │                   ▼
        │                   │            ┌─────────────┐
        │                   │            │Notification│
        │                   │            │  Service   │
        │                   │            └─────────────┘
        │                   │
        ▼                   ▼
┌─────────────┐    ┌─────────────┐
│  Order      │    │  Payment    │
│  Events     │    │  Events     │
└─────────────┘    └─────────────┘
```

## Error Handling

### HTTP Status Codes

| Code | Description | Usage |
|------|-------------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 422 | Unprocessable Entity | Validation errors |
| 500 | Internal Server Error | Server error |

### Error Response Format

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "details": [
    {
      "field": "name",
      "message": "Product name is required"
    },
    {
      "field": "price",
      "message": "Price must be greater than 0"
    }
  ]
}
```

## Rate Limiting

### Rate Limit Headers
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

### Rate Limit Configuration
- **Default**: 1000 requests per minute
- **Burst**: 2000 requests per minute
- **Per IP**: 100 requests per minute
- **Per User**: 500 requests per minute

## API Versioning

### Version Strategy
- **URL Versioning**: `/api/v1/products`
- **Header Versioning**: `Accept: application/vnd.product-order.v1+json`
- **Backward Compatibility**: Maintained for 2 major versions

### Version Lifecycle
- **v1**: Current stable version
- **v2**: Next major version (planned)
- **Deprecation**: 6 months notice
- **Sunset**: 12 months after deprecation

## API Testing

### Test Environment
- **Base URL**: `https://test-api.product-order-service.com/api/v1`
- **Authentication**: Test JWT tokens
- **Data**: Test database with sample data

### Test Scenarios
1. **Happy Path**: Successful API calls
2. **Error Handling**: Invalid requests, authentication failures
3. **Performance**: Load testing, stress testing
4. **Security**: Authorization, input validation

## SDK and Client Libraries

### Java Client
```java
// Maven dependency
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>product-order-client</artifactId>
    <version>1.0.0</version>
</dependency>

// Usage
ProductOrderClient client = new ProductOrderClient("https://api.product-order-service.com");
List<Product> products = client.getProducts();
```

### JavaScript Client
```javascript
// NPM package
npm install @ecommerce/product-order-client

// Usage
import { ProductOrderClient } from '@ecommerce/product-order-client';
const client = new ProductOrderClient('https://api.product-order-service.com');
const products = await client.getProducts();
```

This comprehensive API documentation provides all the necessary information for developers to integrate with the Product Order Service APIs effectively.
