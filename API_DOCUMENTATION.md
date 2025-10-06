# API Documentation

## Product Order Service API

This document provides comprehensive API documentation for the Product Order Service.

### Base URL
- Development: `http://localhost:8080/product-order-service`
- Production: `https://api.yourdomain.com/product-order-service`

### Authentication
All API endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### API Endpoints

#### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration

#### Products
- `GET /api/v1/products` - Get all products
- `GET /api/v1/products/{id}` - Get product by ID
- `POST /api/v1/products` - Create product (Admin only)
- `PUT /api/v1/products/{id}` - Update product (Admin only)
- `DELETE /api/v1/products/{id}` - Delete product (Admin only)
- `GET /api/v1/products/search?query={name}` - Search products by name

#### Categories
- `GET /api/v1/categories` - Get all categories
- `GET /api/v1/categories/{id}` - Get category by ID

#### Customers
- `GET /api/v1/customers` - Get all customers (Admin only)
- `GET /api/v1/customers/{id}` - Get customer by ID
- `POST /api/v1/customers` - Create customer
- `PUT /api/v1/customers/{id}` - Update customer
- `DELETE /api/v1/customers/{id}` - Delete customer
- `GET /api/v1/customers/search?query={name}` - Search customers
- `GET /api/v1/customers/statistics` - Get customer statistics (Admin only)

#### Orders
- `GET /api/v1/orders` - Get all orders
- `GET /api/v1/orders/{id}` - Get order by ID
- `POST /api/v1/orders` - Create order
- `PATCH /api/v1/orders/{id}/status?status={status}` - Update order status (Admin only)
- `DELETE /api/v1/orders/{id}` - Cancel order

#### Payments
- `GET /api/v1/payments` - Get all payments (Admin only)
- `GET /api/v1/payments/{id}` - Get payment by ID
- `POST /api/v1/payments/process` - Process payment
- `POST /api/v1/payments/{id}/refund` - Refund payment
- `DELETE /api/v1/payments/{id}/cancel` - Cancel payment
- `GET /api/v1/payments/statistics` - Get payment statistics (Admin only)

#### Invoices
- `POST /api/v1/invoices/order/{orderId}` - Generate invoice
- `GET /api/v1/invoices/order/{orderId}/url` - Get invoice URL
- `GET /api/v1/invoices/order/{orderId}/exists` - Check if invoice exists

### Response Format
All API responses follow a consistent format:

#### Success Response
```json
{
  "data": { ... },
  "message": "Success message"
}
```

#### Error Response
```json
{
  "timestamp": "2025-01-01T00:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message",
  "path": "/api/v1/endpoint"
}
```

### Status Codes
- `200` - OK
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

### Rate Limiting
API requests are rate limited to 1000 requests per hour per user.

### Swagger UI
Interactive API documentation is available at:
- Development: `http://localhost:8080/product-order-service/swagger-ui.html`
- Production: `https://api.yourdomain.com/product-order-service/swagger-ui.html`
