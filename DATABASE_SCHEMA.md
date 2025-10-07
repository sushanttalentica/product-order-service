# Database Schema Documentation

## Product Order Service Database Schema

This document describes the database schema for the Product Order Service.

### Database Overview
- **Database Type**: H2 (In-Memory)
- **ORM Framework**: JPA/Hibernate
- **Connection Pool**: HikariCP

### Entity Relationships

```
Customer (1) ──── (N) Order (1) ──── (N) OrderItem (N) ──── (1) Product
    │                                    │
    │                                    │
    └─── Address (Embedded)              └─── Category (N) ──── (1) Product
                                                                    │
                                                                    │
Payment (1) ──── (1) Order              Invoice (1) ──── (1) Order
```

### Entity Definitions

#### 1. Customer Entity
```sql
CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Address (Embedded in Customer)
-- street VARCHAR(255)
-- city VARCHAR(100)
-- state VARCHAR(100)
-- postal_code VARCHAR(20)
-- country VARCHAR(100)
```

#### 2. Category Entity
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 3. Product Entity
```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) UNIQUE NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

#### 4. Order Entity
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(100) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
```

#### 5. OrderItem Entity
```sql
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

#### 6. Payment Entity
```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(100) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    gateway_response TEXT,
    failure_reason TEXT,
    refunded_amount DECIMAL(10,2) DEFAULT 0.00,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

#### 7. Invoice Entity
```sql
CREATE TABLE invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_url VARCHAR(1000) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100) DEFAULT 'application/pdf',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

### Indexes

#### Performance Indexes
```sql
-- Customer indexes
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_created_at ON customers(created_at);

-- Product indexes
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_product_name ON products(name);

-- Order indexes
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at);
CREATE INDEX idx_order_number ON orders(order_number);

-- OrderItem indexes
CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_product ON order_items(product_id);

-- Payment indexes
CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created_at ON payments(created_at);

-- Invoice indexes
CREATE INDEX idx_invoice_order ON invoices(order_id);
CREATE INDEX idx_invoice_number ON invoices(invoice_number);
```

### Data Types

#### Decimal Precision
- **Price Fields**: DECIMAL(10,2) - Up to 99,999,999.99
- **Amount Fields**: DECIMAL(10,2) - Up to 99,999,999.99
- **Quantity Fields**: INTEGER - Up to 2,147,483,647

#### String Lengths
- **Names**: VARCHAR(50-255)
- **Emails**: VARCHAR(100)
- **SKUs**: VARCHAR(100)
- **Status**: VARCHAR(20)
- **Phone**: VARCHAR(20)

#### Timestamps
- **Created At**: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- **Updated At**: TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

### Constraints

#### Primary Keys
- All entities have auto-incrementing BIGINT primary keys
- Composite keys avoided for simplicity

#### Foreign Keys
- Order → Customer (customer_id)
- OrderItem → Order (order_id)
- OrderItem → Product (product_id)
- Product → Category (category_id)
- Payment → Order (order_id)
- Invoice → Order (order_id)

#### Unique Constraints
- Customer email
- Product SKU
- Order number
- Payment ID
- Invoice number

#### Check Constraints
- Price > 0
- Quantity > 0
- Amount > 0
- Valid status values

### Sample Data

#### Categories
```sql
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Apparel and fashion items'),
('Books', 'Books and educational materials');
```

#### Products
```sql
INSERT INTO products (name, description, sku, price, stock_quantity, category_id) VALUES
('iPhone 15', 'Latest iPhone model', 'IPHONE15-001', 999.99, 100, 1),
('Samsung Galaxy', 'Android smartphone', 'SAMSUNG-001', 899.99, 50, 1),
('MacBook Pro', 'Apple laptop', 'MACBOOK-001', 1999.99, 25, 1);
```

### Database Configuration

#### H2 Configuration
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
```

#### JPA Configuration
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
```

### Migration Strategy

#### Development
- H2 in-memory database
- Schema auto-creation
- Test data seeding

#### Production
- PostgreSQL/MySQL database
- Flyway migrations
- Data backup strategies
