# High Concurrency Guide: Stock Management

## 🔴 Current Race Condition Problem

### **Scenario: Two Users Buy the Last Item**

```
Product: iPhone 15
Current Stock: 1

Timeline:
---------
T0: User A starts checkout (reads stock = 1)
T1: User B starts checkout (reads stock = 1)
T2: User A checks: 1 >= 1 ✓ (passes)
T3: User B checks: 1 >= 1 ✓ (passes)
T4: User A reduces: stock = 1 - 1 = 0
T5: User A saves stock = 0
T6: User B reduces: stock = 1 - 1 = 0 (still uses old value!)
T7: User B saves stock = 0
---------
Result: 2 orders created, but only 1 item in stock! 
Stock goes to 0, but should be -1 or reject one order.
```

### **Why This Happens:**

```java
// Current code in OrderServiceImpl.java (lines 382-398)
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        // Step 1: Read product (NOT locked)
        Product product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(...);
        
        // Step 2: Check stock (using potentially stale data)
        if (!product.hasSufficientStock(orderItem.getQuantity())) {
            throw new BusinessException("Insufficient stock...");
        }
        
        // Step 3: Reduce stock (another user might have reduced it already!)
        product.reduceStock(orderItem.getQuantity());
        
        // Step 4: Save (overwrites other user's changes!)
        productRepository.save(product);
    }
}
```

**Gap between read and write = RACE CONDITION WINDOW**

---

## ✅ Solution 1: Pessimistic Locking (Recommended for Immediate Fix)

### **How It Works:**

```
Product: iPhone 15
Stock: 1

Timeline with Pessimistic Locking:
----------------------------------
T0: User A starts checkout
T1: User A acquires WRITE LOCK on product row
T2: User B starts checkout
T3: User B tries to read product → BLOCKED (waiting for lock)
T4: User A reads stock = 1
T5: User A checks: 1 >= 1 ✓
T6: User A reduces: stock = 0
T7: User A saves & RELEASES LOCK
T8: User B acquires lock (now can proceed)
T9: User B reads stock = 0 (updated value!)
T10: User B checks: 0 >= 1 ✗
T11: User B gets error: "Insufficient stock"
----------------------------------
Result: Only 1 order succeeds ✓
Stock management is correct ✓
```

### **Implementation:**

```java
// 1. Add lock method to ProductRepository
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);
}

// 2. Update OrderServiceImpl
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        // Acquire exclusive lock on product row
        Product product = productRepository.findByIdWithLock(orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(...));
        
        if (!product.hasSufficientStock(orderItem.getQuantity())) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }
        
        product.reduceStock(orderItem.getQuantity());
        productRepository.save(product);
        // Lock automatically released when transaction commits
    }
}
```

**SQL Generated:**
```sql
SELECT * FROM products WHERE id = ? FOR UPDATE;
-- FOR UPDATE creates exclusive lock
-- Other transactions must wait
```

---

## ✅ Solution 2: Optimistic Locking (Better Performance)

### **How It Works:**

Uses a version field to detect concurrent modifications:

```
Product: iPhone 15
Stock: 1, Version: 10

Timeline with Optimistic Locking:
----------------------------------
T0: User A reads: stock=1, version=10
T1: User B reads: stock=1, version=10
T2: User A updates: stock=0, version=11 (checks version=10) ✓
T3: User A commits successfully
T4: User B tries update: stock=0, version=11 (checks version=10) ✗
T5: User B gets OptimisticLockException
T6: User B retries → reads stock=0, version=11
T7: User B checks: 0 >= 1 ✗
T8: User B gets "Insufficient stock"
----------------------------------
Result: Only 1 order succeeds ✓
No waiting/blocking (better performance) ✓
```

### **Implementation:**

```java
// 1. Add @Version to Product entity
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version  // ← Optimistic locking
    private Long version;
    
    private Integer stockQuantity;
    
    // ... rest of fields
}

// 2. Add retry logic in OrderServiceImpl
@Override
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    int maxRetries = 3;
    int attempt = 0;
    
    while (attempt < maxRetries) {
        try {
            return createOrderInternal(request);
        } catch (OptimisticLockException e) {
            attempt++;
            if (attempt >= maxRetries) {
                throw new BusinessException("High traffic detected. Please try again.");
            }
            log.warn("Optimistic lock conflict, retrying... (attempt {}/{})", attempt, maxRetries);
            try {
                Thread.sleep(50 * attempt); // Exponential backoff
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    throw new BusinessException("Failed to create order after retries");
}
```

---

## ✅ Solution 3: Database Atomic Update (Best for High Concurrency)

### **How It Works:**

Single atomic SQL operation - no race condition possible:

```sql
-- Atomic update: decrement only if stock is sufficient
UPDATE products 
SET stock_quantity = stock_quantity - 3 
WHERE id = 123 
  AND stock_quantity >= 3;

-- Returns affected rows: 1 if successful, 0 if failed
```

### **Implementation:**

```java
// 1. Add atomic update method to ProductRepository
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
           "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int reduceStockAtomic(@Param("productId") Long productId, 
                          @Param("quantity") Integer quantity);
    
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity " +
           "WHERE p.id = :productId")
    int restoreStockAtomic(@Param("productId") Long productId, 
                           @Param("quantity") Integer quantity);
}

// 2. Update OrderServiceImpl
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        // Atomic database operation - thread-safe!
        int rowsUpdated = productRepository.reduceStockAtomic(
            orderItem.getProductId(),
            orderItem.getQuantity()
        );
        
        if (rowsUpdated == 0) {
            // Either product doesn't exist OR insufficient stock
            Product product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
            throw new BusinessException("Insufficient stock for product: " + product.getName() + 
                                      " (Available: " + product.getStockQuantity() + ")");
        }
    }
}
```

**Benefits:**
- ✅ **No race conditions**: Database handles atomicity
- ✅ **Best performance**: Single SQL statement per product
- ✅ **No blocking**: No locks held
- ✅ **Scalable**: Works with any number of concurrent requests

---

## 🚀 Real-Time Stock Updates Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    User Purchase Flow                       │
└─────────────────────────────────────────────────────────────┘

User A buys product → OrderService.createOrder()
                           ↓
                  Atomic Stock Reduction
                           ↓
                  productRepository.reduceStockAtomic()
                           ↓
                  UPDATE products SET stock = stock - 3
                           ↓
                  Kafka Event Published
                           ↓
              "product.stock.updated" topic
                           ↓
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
  WebSocket         Redis Cache        Inventory
  Broadcaster       Updater            Service
        ↓                  ↓                  ↓
  All Users        Cache Refresh      Stock Alerts
  (Real-time)      (30sec TTL)        (Low stock)
```

### **Code Flow:**

```java
// Step 1: User places order
OrderService.createOrder(request)
    ↓
// Step 2: Atomic stock reduction
productRepository.reduceStockAtomic(productId, quantity)
    ↓ (Database ensures atomicity)
// Step 3: Publish Kafka event
kafkaTemplate.send("product.stock.updated", event)
    ↓
// Step 4: Multiple consumers receive event
┌────────────────┬─────────────────┬──────────────────┐
│                │                 │                  │
↓                ↓                 ↓                  ↓
WebSocket        Redis Cache       Notification     Inventory
Broadcaster      Update            Service          Analytics
│                │                 │                  │
↓                ↓                 ↓                  ↓
Users get        Cache             Email if         Update
real-time        refreshed         low stock        dashboard
updates          for fast reads    alert
```

---

## 🔥 Performance Under Load

### **10,000 Requests Per Second Breakdown:**

#### **With Current Implementation:**
```
10,000 RPS × 1 instance × 200 threads = BOTTLENECK
Result: Most requests timeout or fail ❌
```

#### **With Optimizations:**
```
Architecture:
- 20 app instances (auto-scaled)
- Each handles 500 RPS (comfortable load)
- Load balancer distributes traffic
- Database with connection pool of 100 per instance
- Redis cluster for caching
- Kafka for async processing

Calculation:
- 20 instances × 500 RPS = 10,000 RPS ✓
- Cache hit rate: 90% (9,000 requests from cache)
- Database writes: ~1,000 RPS (manageable)
- Response time: <100ms for 95th percentile ✓
```

---

## 💡 Quick Wins (Implement First)

### **1. Add Pessimistic Locking (30 minutes)**
```java
// ProductRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :productId")
Optional<Product> findByIdWithLock(@Param("productId") Long productId);

// OrderServiceImpl.java
Product product = productRepository.findByIdWithLock(productId)
```

### **2. Optimize Connection Pool (10 minutes)**
```yaml
# application.yml
datasource:
  hikari:
    maximum-pool-size: 100
    minimum-idle: 20

server:
  tomcat:
    max-threads: 500
```

### **3. Smart Cache Eviction (20 minutes)**
```java
// Change from:
@CacheEvict(value = "products", allEntries = true)  // ❌ Clears all

// To:
@CacheEvict(value = "products", key = "#productId")  // ✅ Only one product
@CachePut(value = "products", key = "#result.id")    // ✅ Update cache
```

### **4. Horizontal Scaling (1 hour)**
```yaml
# k8s/prod/deployment.yaml
spec:
  replicas: 10  # ← Increase replicas
  
  # Auto-scaling
  autoscaling:
    minReplicas: 5
    maxReplicas: 50
    targetCPUUtilizationPercentage: 70
```

---

## 📊 Performance Comparison

| **Scenario** | **Without Optimizations** | **With Optimizations** |
|--------------|---------------------------|------------------------|
| **10 concurrent users** | ✅ Works | ✅ Works (faster) |
| **100 concurrent users** | ⚠️ Slow, some race conditions | ✅ Works smoothly |
| **1,000 concurrent users** | ❌ Fails, many race conditions | ✅ Works with load balancer |
| **10,000 concurrent users** | ❌ Complete failure | ✅ Works with full architecture |

---

## 🎯 Immediate Action Items

### **Critical (Fix Now):**
1. ✅ Implement pessimistic locking on stock updates
2. ✅ Add database indexes (already have some)
3. ✅ Increase connection pool size

### **Important (Next Sprint):**
1. ⏳ Add `@Version` field for optimistic locking
2. ⏳ Implement WebSocket for real-time updates
3. ⏳ Deploy multiple app instances with load balancer

### **Nice to Have (Future):**
1. 📋 Implement read replicas for database
2. 📋 Add Redis cluster
3. 📋 Implement circuit breakers
4. 📋 Add rate limiting

---

**Current Status**: Code works for development/testing but needs concurrency improvements for production at scale!
