# API Testing Plan

## Test Data Setup
- ✅ Created `data.sql` with test categories and products
- ✅ Updated `application.yml` to load test data on startup
- ⏳ Application needs to be restarted in IntelliJ

## API Testing Checklist

### 1. Health & Info Endpoints
- [ ] GET `/actuator/health` - Health check
- [ ] GET `/actuator/info` - Application info

### 2. Product APIs (Public - No Auth Required)
- [ ] GET `/api/v1/products` - Get all products (paginated)
- [ ] GET `/api/v1/products/{id}` - Get product by ID
- [ ] GET `/api/v1/products/sku/{sku}` - Get product by SKU
- [ ] GET `/api/v1/products/search?name={name}` - Search products by name
- [ ] GET `/api/v1/products/category/{categoryId}` - Get products by category
- [ ] GET `/api/v1/products/price-range?minPrice={min}&maxPrice={max}` - Get products by price range
- [ ] GET `/api/v1/products/search/advanced` - Advanced search

### 3. Product APIs (Admin - Auth Required)
- [ ] POST `/api/v1/products` - Create product (requires ADMIN role)
- [ ] PUT `/api/v1/products/{id}` - Update product (requires ADMIN role)
- [ ] DELETE `/api/v1/products/{id}` - Delete product (requires ADMIN role)
- [ ] GET `/api/v1/products/low-stock?threshold={threshold}` - Get low stock products
- [ ] PATCH `/api/v1/products/{id}/stock?stock={quantity}` - Update product stock

### 4. Order APIs (Customer - Auth Required)
- [ ] POST `/api/v1/orders` - Create order (requires CUSTOMER role)
- [ ] GET `/api/v1/orders/{id}` - Get order by ID
- [ ] GET `/api/v1/orders/customer/{customerId}` - Get orders by customer
- [ ] DELETE `/api/v1/orders/{id}` - Cancel order

### 5. Order APIs (Admin - Auth Required)
- [ ] GET `/api/v1/orders` - Get all orders
- [ ] GET `/api/v1/orders/status/{status}` - Get orders by status
- [ ] PATCH `/api/v1/orders/{id}/status?status={status}` - Update order status
- [ ] GET `/api/v1/orders/date-range?startDate={start}&endDate={end}` - Get orders by date range
- [ ] GET `/api/v1/orders/amount-range?minAmount={min}&maxAmount={max}` - Get orders by amount range
- [ ] GET `/api/v1/orders/statistics` - Get order statistics
- [ ] GET `/api/v1/orders/needing-attention` - Get orders needing attention

### 6. Authentication & Security
- [ ] Test JWT authentication (if auth endpoints exist)
- [ ] Test role-based access control
- [ ] Test unauthorized access scenarios

### 7. gRPC APIs
- [ ] Test gRPC payment service on port 9090
- [ ] Test payment processing via gRPC

### 8. Kafka Integration
- [ ] Test order event publishing
- [ ] Test payment event publishing
- [ ] Test notification events

### 9. Cache Testing
- [ ] Test Redis cache functionality
- [ ] Test cache eviction
- [ ] Test cache performance

## Test Data Available
- **Categories**: Electronics, Clothing, Books
- **Products**: iPhone 15, MacBook Pro, T-Shirt, Programming Book
- **Users**: admin/adminpass, user/password (if auth is implemented)

## Expected Results
- All public endpoints should work without authentication
- Admin endpoints should require proper authentication
- Customer endpoints should require proper authentication
- gRPC service should be accessible on port 9090
- Kafka events should be published successfully
- Redis cache should be working