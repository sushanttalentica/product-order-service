# Product Order Service - Database Schema

## Overview

The Product Order Service uses a relational database design with MySQL as the primary database. The schema follows Domain-Driven Design (DDD) principles and implements proper normalization with foreign key relationships.

## Entity Relationship Diagram (ERD)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATABASE SCHEMA                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ENTITY RELATIONSHIP DIAGRAM                      │
└─────────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────┐
                    │    CATEGORY     │
                    │                 │
                    │ + id (PK)       │
                    │ + name          │
                    │ + description   │
                    │ + is_active     │
                    │ + created_at    │
                    │ + updated_at    │
                    └─────────┬───────┘
                              │
                              │ 1:N
                              │
                    ┌─────────▼───────┐
                    │     PRODUCT     │
                    │                 │
                    │ + id (PK)       │
                    │ + name          │
                    │ + description   │
                    │ + price         │
                    │ + sku           │
                    │ + stock_quantity│
                    │ + is_active     │
                    │ + category_id   │
                    │ + created_at    │
                    │ + updated_at    │
                    └─────────┬───────┘
                              │
                              │ 1:N
                              │
                    ┌─────────▼───────┐
                    │      ORDER      │
                    │                 │
                    │ + id (PK)       │
                    │ + order_number  │
                    │ + customer_id   │
                    │ + total_amount  │
                    │ + status        │
                    │ + shipping_addr│
                    │ + created_at    │
                    │ + updated_at    │
                    └─────────┬───────┘
                              │
                              │ 1:N
                              │
                    ┌─────────▼───────┐
                    │   ORDER_ITEM    │
                    │                 │
                    │ + id (PK)       │
                    │ + order_id (FK) │
                    │ + product_id (FK)│
                    │ + quantity      │
                    │ + unit_price   │
                    │ + total_price   │
                    │ + created_at   │
                    │ + updated_at    │
                    └─────────────────┘
```

## Table Definitions

### 1. Categories Table

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_categories_name (name),
    INDEX idx_categories_active (is_active),
    INDEX idx_categories_created (created_at)
);
```

**Purpose**: Stores product categories for organization and filtering.

**Key Features**:
- Unique category names
- Soft delete with `is_active` flag
- Optimized indexes for common queries
- Audit fields for tracking changes

### 2. Products Table

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    stock_quantity INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    
    INDEX idx_products_name (name),
    INDEX idx_products_sku (sku),
    INDEX idx_products_category (category_id),
    INDEX idx_products_active (is_active),
    INDEX idx_products_price (price),
    INDEX idx_products_stock (stock_quantity),
    INDEX idx_products_created (created_at),
    
    CONSTRAINT chk_products_price CHECK (price >= 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0)
);
```

**Purpose**: Stores product information including pricing, inventory, and categorization.

**Key Features**:
- Unique SKU constraint
- Foreign key relationship with categories
- Price and stock validation
- Comprehensive indexing for search and filtering
- Soft delete capability

### 3. Orders Table

```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    shipping_address JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_orders_number (order_number),
    INDEX idx_orders_customer (customer_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at),
    INDEX idx_orders_amount (total_amount),
    
    CONSTRAINT chk_orders_amount CHECK (total_amount >= 0)
);
```

**Purpose**: Stores order information including customer details, status, and shipping information.

**Key Features**:
- Unique order number generation
- JSON storage for flexible shipping address
- Status enum for order lifecycle management
- Customer-based indexing for queries
- Amount validation

### 4. Order Items Table

```sql
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id),
    INDEX idx_order_items_created (created_at),
    
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_order_items_total_price CHECK (total_price >= 0)
);
```

**Purpose**: Stores individual items within orders, maintaining product details at the time of purchase.

**Key Features**:
- Cascade delete with orders
- Restrict delete with products (preserve order history)
- Quantity and price validation
- Optimized indexing for order and product queries

## Database Indexes Strategy

### Primary Indexes
- **Primary Keys**: All tables have auto-incrementing BIGINT primary keys
- **Unique Constraints**: SKU, order_number for data integrity

### Secondary Indexes
- **Search Indexes**: Name, description fields for text search
- **Filter Indexes**: Status, is_active for filtering
- **Foreign Key Indexes**: All foreign key relationships
- **Time-based Indexes**: created_at for temporal queries
- **Business Indexes**: Price, stock_quantity for business logic

### Composite Indexes
```sql
-- For product search and filtering
CREATE INDEX idx_products_search ON products (is_active, category_id, price);

-- For order status and customer queries
CREATE INDEX idx_orders_customer_status ON orders (customer_id, status, created_at);

-- For order item analysis
CREATE INDEX idx_order_items_analysis ON order_items (order_id, product_id, quantity);
```

## Data Types and Constraints

### Numeric Types
- **BIGINT**: Primary keys and foreign keys (64-bit integers)
- **DECIMAL(10,2)**: Monetary values with precision
- **INT**: Quantities and counts

### String Types
- **VARCHAR(255)**: Names and descriptions
- **VARCHAR(100)**: SKUs and codes
- **VARCHAR(50)**: Order numbers
- **TEXT**: Long descriptions

### Special Types
- **JSON**: Flexible shipping address storage
- **ENUM**: Status values for data integrity
- **BOOLEAN**: Active flags and boolean values
- **TIMESTAMP**: Audit and temporal fields

### Constraints
- **NOT NULL**: Required fields
- **UNIQUE**: Unique identifiers
- **CHECK**: Business rule validation
- **FOREIGN KEY**: Referential integrity

## Database Views

### 1. Product Summary View
```sql
CREATE VIEW product_summary AS
SELECT 
    p.id,
    p.name,
    p.sku,
    p.price,
    p.stock_quantity,
    p.is_active,
    c.name as category_name,
    CASE 
        WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN p.stock_quantity < 10 THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
    END as stock_status
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.is_active = TRUE;
```

### 2. Order Summary View
```sql
CREATE VIEW order_summary AS
SELECT 
    o.id,
    o.order_number,
    o.customer_id,
    o.total_amount,
    o.status,
    o.created_at,
    COUNT(oi.id) as item_count,
    SUM(oi.quantity) as total_quantity
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, o.order_number, o.customer_id, o.total_amount, o.status, o.created_at;
```

### 3. Sales Analytics View
```sql
CREATE VIEW sales_analytics AS
SELECT 
    DATE(o.created_at) as sale_date,
    COUNT(o.id) as order_count,
    SUM(o.total_amount) as total_revenue,
    AVG(o.total_amount) as average_order_value,
    COUNT(DISTINCT o.customer_id) as unique_customers
FROM orders o
WHERE o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED')
GROUP BY DATE(o.created_at);
```

## Database Triggers

### 1. Order Number Generation
```sql
DELIMITER //
CREATE TRIGGER tr_orders_generate_number
BEFORE INSERT ON orders
FOR EACH ROW
BEGIN
    IF NEW.order_number IS NULL OR NEW.order_number = '' THEN
        SET NEW.order_number = CONCAT('ORD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(LAST_INSERT_ID(), 6, '0'));
    END IF;
END//
DELIMITER ;
```

### 2. Stock Update Trigger
```sql
DELIMITER //
CREATE TRIGGER tr_order_items_update_stock
AFTER INSERT ON order_items
FOR EACH ROW
BEGIN
    UPDATE products 
    SET stock_quantity = stock_quantity - NEW.quantity,
        updated_at = NOW()
    WHERE id = NEW.product_id;
END//
DELIMITER ;
```

### 3. Order Total Calculation
```sql
DELIMITER //
CREATE TRIGGER tr_order_items_calculate_total
BEFORE INSERT ON order_items
FOR EACH ROW
BEGIN
    SET NEW.total_price = NEW.quantity * NEW.unit_price;
END//
DELIMITER ;
```

## Database Procedures

### 1. Create Order Procedure
```sql
DELIMITER //
CREATE PROCEDURE sp_create_order(
    IN p_customer_id VARCHAR(100),
    IN p_shipping_address JSON,
    OUT p_order_id BIGINT,
    OUT p_order_number VARCHAR(50)
)
BEGIN
    DECLARE v_order_id BIGINT;
    DECLARE v_order_number VARCHAR(50);
    
    -- Generate order number
    SET v_order_number = CONCAT('ORD-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(LAST_INSERT_ID(), 6, '0'));
    
    -- Insert order
    INSERT INTO orders (order_number, customer_id, shipping_address)
    VALUES (v_order_number, p_customer_id, p_shipping_address);
    
    SET v_order_id = LAST_INSERT_ID();
    
    SET p_order_id = v_order_id;
    SET p_order_number = v_order_number;
END//
DELIMITER ;
```

### 2. Update Order Status Procedure
```sql
DELIMITER //
CREATE PROCEDURE sp_update_order_status(
    IN p_order_id BIGINT,
    IN p_new_status VARCHAR(20)
)
BEGIN
    DECLARE v_current_status VARCHAR(20);
    
    -- Get current status
    SELECT status INTO v_current_status FROM orders WHERE id = p_order_id;
    
    -- Validate status transition
    IF v_current_status = 'DELIVERED' AND p_new_status != 'DELIVERED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot change status of delivered order';
    END IF;
    
    IF v_current_status = 'CANCELLED' AND p_new_status != 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot change status of cancelled order';
    END IF;
    
    -- Update status
    UPDATE orders 
    SET status = p_new_status, updated_at = NOW()
    WHERE id = p_order_id;
END//
DELIMITER ;
```

## Database Functions

### 1. Calculate Order Total
```sql
DELIMITER //
CREATE FUNCTION fn_calculate_order_total(p_order_id BIGINT)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_total DECIMAL(10,2) DEFAULT 0;
    
    SELECT COALESCE(SUM(total_price), 0) INTO v_total
    FROM order_items
    WHERE order_id = p_order_id;
    
    RETURN v_total;
END//
DELIMITER ;
```

### 2. Check Product Availability
```sql
DELIMITER //
CREATE FUNCTION fn_check_product_availability(p_product_id BIGINT, p_quantity INT)
RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_stock INT DEFAULT 0;
    DECLARE v_is_active BOOLEAN DEFAULT FALSE;
    
    SELECT stock_quantity, is_active INTO v_stock, v_is_active
    FROM products
    WHERE id = p_product_id;
    
    RETURN v_is_active AND v_stock >= p_quantity;
END//
DELIMITER ;
```

## Database Optimization

### 1. Partitioning Strategy
```sql
-- Partition orders table by creation date (monthly partitions)
ALTER TABLE orders PARTITION BY RANGE (YEAR(created_at) * 100 + MONTH(created_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    -- Add more partitions as needed
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 2. Query Optimization
```sql
-- Optimized product search query
EXPLAIN SELECT p.*, c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.is_active = TRUE
  AND p.stock_quantity > 0
  AND p.price BETWEEN 10.00 AND 100.00
  AND c.name = 'Electronics'
ORDER BY p.created_at DESC
LIMIT 20;
```

### 3. Connection Pooling
```yaml
# HikariCP Configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

## Database Security

### 1. User Roles and Permissions
```sql
-- Application user with limited permissions
CREATE USER 'product_order_app'@'%' IDENTIFIED BY 'secure_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON ecommerce_db.* TO 'product_order_app'@'%';

-- Read-only user for reporting
CREATE USER 'product_order_readonly'@'%' IDENTIFIED BY 'readonly_password';
GRANT SELECT ON ecommerce_db.* TO 'product_order_readonly'@'%';

-- Admin user for maintenance
CREATE USER 'product_order_admin'@'%' IDENTIFIED BY 'admin_password';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'product_order_admin'@'%';
```

### 2. Data Encryption
```sql
-- Encrypt sensitive data
ALTER TABLE orders 
ADD COLUMN customer_email_encrypted VARBINARY(255);

-- Create encryption function
DELIMITER //
CREATE FUNCTION fn_encrypt_data(data VARCHAR(255))
RETURNS VARBINARY(255)
DETERMINISTIC
BEGIN
    RETURN AES_ENCRYPT(data, 'encryption_key');
END//
DELIMITER ;
```

## Database Backup and Recovery

### 1. Backup Strategy
```bash
# Full database backup
mysqldump --single-transaction --routines --triggers ecommerce_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Incremental backup
mysqlbinlog --start-datetime="2024-01-01 00:00:00" mysql-bin.000001 > incremental_backup.sql
```

### 2. Recovery Procedures
```bash
# Restore from full backup
mysql ecommerce_db < backup_20240101_120000.sql

# Restore from incremental backup
mysqlbinlog incremental_backup.sql | mysql ecommerce_db
```

## Database Monitoring

### 1. Performance Monitoring
```sql
-- Monitor slow queries
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- Monitor connection usage
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';
```

### 2. Health Checks
```sql
-- Database health check
SELECT 
    'Database Status' as check_type,
    CASE 
        WHEN COUNT(*) > 0 THEN 'HEALTHY'
        ELSE 'UNHEALTHY'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'ecommerce_db';
```

This database schema provides a robust, scalable, and maintainable foundation for the Product Order Service with proper indexing, constraints, and optimization strategies.
