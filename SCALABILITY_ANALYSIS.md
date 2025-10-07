# Scalability Analysis: Handling 10,000 Requests/Second

## Current Architecture Analysis

### 🔴 **CRITICAL ISSUES - Race Conditions**

#### **Problem 1: Stock Management Race Condition**

**Current Implementation** (Lines 382-398 in `OrderServiceImpl.java`):
```java
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        Product product = productRepository.findById(orderItem.getProductId())
                .orElseThrow(...);
        
        if (!product.hasSufficientStock(orderItem.getQuantity())) {
            throw new BusinessException("Insufficient stock...");
        }
        
        product.reduceStock(orderItem.getQuantity());  // ⚠️ RACE CONDITION!
        productRepository.save(product);
    }
}
```

**Race Condition Scenario:**
```
Time  | User A (wants 5 items)      | User B (wants 5 items)      | Stock
------|-----------------------------|-----------------------------|-------
T0    | Read stock = 8              |                             | 8
T1    |                             | Read stock = 8              | 8
T2    | Check: 8 >= 5 ✓             |                             | 8
T3    |                             | Check: 8 >= 5 ✓             | 8
T4    | stock = 8 - 5 = 3           |                             | 8
T5    | Save stock = 3              |                             | 3
T6    |                             | stock = 8 - 5 = 3           | 3
T7    |                             | Save stock = 3              | 3
------|-----------------------------|-----------------------------|-------
Result: Both orders succeed, but 10 items sold with only 8 in stock! ❌
```

#### **Problem 2: No Database-Level Locking**

- ❌ No pessimistic locking
- ❌ No optimistic locking (version field)
- ❌ Relies only on `@Transactional` (not sufficient for high concurrency)

#### **Problem 3: Single Database Instance**

- ❌ H2 in-memory database (not production-ready)
- ❌ No read replicas for query distribution
- ❌ Single point of failure

---

## 🚀 **SOLUTIONS FOR 10,000 RPS**

### **Solution 1: Pessimistic Locking (Immediate Fix)**

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);
}
```

**Updated OrderServiceImpl:**
```java
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        // Use pessimistic lock to prevent concurrent modifications
        Product product = productRepository.findByIdWithLock(orderItem.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(...));
        
        if (!product.hasSufficientStock(orderItem.getQuantity())) {
            throw new BusinessException("Insufficient stock...");
        }
        
        product.reduceStock(orderItem.getQuantity());
        productRepository.save(product);
        // Lock released after transaction commits
    }
}
```

**Benefits:**
- ✅ Prevents race conditions
- ✅ Guarantees data consistency
- ✅ Works with existing code

**Drawbacks:**
- ⚠️ Can cause lock contention at very high concurrency
- ⚠️ May reduce throughput (queue serializes requests)

---

### **Solution 2: Optimistic Locking (Better Performance)**

**Add version field to Product entity:**
```java
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version  // ← Add this for optimistic locking
    private Long version;
    
    private Integer stockQuantity;
    
    // ... other fields
}
```

**How it works:**
1. Each product has a version number
2. When updating, JPA checks if version matches
3. If version changed (someone else updated), throws `OptimisticLockException`
4. Application can retry the operation

**Benefits:**
- ✅ Better performance than pessimistic locking
- ✅ No blocking/waiting
- ✅ Handles concurrent updates gracefully

**Implementation:**
```java
@Transactional
public OrderResponse createOrder(CreateOrderRequest request) {
    int retries = 3;
    while (retries > 0) {
        try {
            // ... order creation logic
            return orderMapper.toResponse(savedOrder);
        } catch (OptimisticLockException e) {
            retries--;
            if (retries == 0) throw new BusinessException("High traffic, please retry");
            log.warn("Optimistic lock conflict, retrying... ({} attempts left)", retries);
            Thread.sleep(100); // Brief pause before retry
        }
    }
}
```

---

### **Solution 3: Database Atomic Operations (Best for Scale)**

**Use atomic SQL updates:**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
           "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int reduceStockAtomic(@Param("productId") Long productId, 
                          @Param("quantity") Integer quantity);
}
```

**Updated service:**
```java
private void validateAndReserveProducts(CreateOrderRequest request) {
    for (var orderItem : request.getOrderItems()) {
        int rowsUpdated = productRepository.reduceStockAtomic(
            orderItem.getProductId(), 
            orderItem.getQuantity()
        );
        
        if (rowsUpdated == 0) {
            throw new BusinessException("Insufficient stock or product not found");
        }
    }
}
```

**Benefits:**
- ✅ Atomic at database level
- ✅ No race conditions possible
- ✅ Best performance for high concurrency
- ✅ Single SQL statement (fast)

---

### **Solution 4: Redis Distributed Locking**

**For microservices/multiple instances:**

```java
@Service
public class DistributedLockService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public boolean acquireLock(String lockKey, long timeoutMs) {
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofMillis(timeoutMs));
        return Boolean.TRUE.equals(acquired);
    }
    
    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
```

**Usage:**
```java
String lockKey = "product:stock:" + productId;
if (!distributedLockService.acquireLock(lockKey, 5000)) {
    throw new BusinessException("Product is being updated, please retry");
}
try {
    // Reduce stock safely
    product.reduceStock(quantity);
    productRepository.save(product);
} finally {
    distributedLockService.releaseLock(lockKey);
}
```

---

## 🔄 **Real-Time Stock Updates**

### **Current Implementation Issues:**

1. ❌ **No Real-Time Updates**: Clients must poll for stock changes
2. ❌ **Cache Eviction**: `@CacheEvict` clears entire cache (inefficient)
3. ❌ **No WebSocket/SSE**: No push notifications

### **Solutions for Real-Time Updates:**

#### **Solution 1: WebSocket Integration**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
```

**Stock update broadcast:**
```java
@Service
public class StockUpdateService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public void broadcastStockUpdate(Long productId, Integer newStock) {
        Map<String, Object> update = Map.of(
            "productId", productId,
            "stockQuantity", newStock,
            "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/stock/" + productId, update);
    }
}
```

**Client receives updates in real-time:**
```javascript
// Frontend code
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.subscribe('/topic/stock/123', (message) => {
    const update = JSON.parse(message.body);
    console.log('Stock updated:', update.stockQuantity);
    // Update UI immediately
});
```

---

#### **Solution 2: Server-Sent Events (SSE)**

```java
@RestController
@RequestMapping("/api/v1/stock")
public class StockStreamController {
    
    @GetMapping(value = "/updates/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StockUpdate>> streamStockUpdates(@PathVariable Long productId) {
        return Flux.interval(Duration.ofSeconds(1))
            .map(sequence -> {
                Integer currentStock = productService.getProductStock(productId);
                return ServerSentEvent.<StockUpdate>builder()
                    .id(String.valueOf(sequence))
                    .event("stock-update")
                    .data(new StockUpdate(productId, currentStock))
                    .build();
            });
    }
}
```

---

#### **Solution 3: Kafka Event Streaming**

**Already partially implemented!** Enhance it:

```java
// In ProductServiceImpl
@Override
@CacheEvict(value = "products", key = "#productId")
public void reduceProductStock(Long productId, Integer quantity) {
    Product product = productRepository.findByIdWithLock(productId)
            .orElseThrow(...);
    
    product.reduceStock(quantity);
    productRepository.save(product);
    
    // Publish stock update event
    kafkaTemplate.send("product.stock.updated", productId.toString(), 
        Map.of(
            "productId", productId,
            "newStock", product.getStockQuantity(),
            "timestamp", System.currentTimeMillis()
        )
    );
}
```

**Frontend consumes Kafka events** (via WebSocket bridge or SSE):
- Clients subscribe to stock updates
- Real-time notifications when stock changes
- No polling required

---

## 📊 **Handling 10,000 RPS**

### **Current Bottlenecks:**

| **Component** | **Current Limit** | **Issue** |
|---------------|-------------------|-----------|
| **Database** | ~1,000 RPS | Single H2 instance, no connection pooling optimization |
| **App Server** | ~2,000 RPS | Single Tomcat instance, limited threads (200) |
| **Redis Cache** | ~10,000 RPS | ✅ Can handle, but cache eviction strategy needs improvement |
| **Kafka** | ~100,000+ RPS | ✅ Can handle, already scalable |

### **Architecture for 10,000 RPS:**

```
┌─────────────────────────────────────────────────────────────┐
│                    Load Balancer (Nginx)                    │
│              (Distributes 10,000 RPS)                       │
└─────────────────┬───────────────────────────────────────────┘
                  │
        ┌─────────┼─────────┬─────────┬─────────┐
        │         │         │         │         │
        ▼         ▼         ▼         ▼         ▼
    ┌────────┐ ┌────────┐ ┌────────┐ ... (10 instances)
    │App Srv1│ │App Srv2│ │App Srv3│     Each handles ~1,000 RPS
    └────┬───┘ └────┬───┘ └────┬───┘
         │          │          │
         └──────────┼──────────┘
                    │
        ┌───────────┼────────────┬────────────┐
        │           │            │            │
        ▼           ▼            ▼            ▼
    ┌─────┐   ┌─────────┐  ┌────────┐  ┌────────┐
    │Redis│   │PostgreSQL│  │ Kafka  │  │  S3    │
    │Cluster   │Read/Write│  │Cluster │  │Storage │
    └─────┘   └─────────┘  └────────┘  └────────┘
              │         │
            Master  Replicas (5)
```

### **Implementation Changes Needed:**

#### **1. Database Scaling**

**Current Configuration** (application.yml):
```yaml
datasource:
  hikari:
    maximum-pool-size: 20      # ❌ Too small for 10,000 RPS
    minimum-idle: 5
```

**Optimized Configuration:**
```yaml
datasource:
  hikari:
    maximum-pool-size: 100     # ✅ Increase pool size
    minimum-idle: 20
    connection-timeout: 20000
    idle-timeout: 300000
    max-lifetime: 1200000
    leak-detection-threshold: 60000

  # Read-Write Splitting
  read-datasource:
    url: jdbc:postgresql://read-replica:5432/productorder
    hikari:
      maximum-pool-size: 150   # More connections for reads

jpa:
  properties:
    hibernate:
      jdbc:
        batch_size: 50         # ✅ Batch operations
        fetch_size: 100
      order_inserts: true
      order_updates: true
      query:
        in_clause_parameter_padding: true
```

#### **2. Application Server Tuning**

**Current** (application.yml):
```yaml
server:
  tomcat:
    max-threads: 200           # ❌ Default, not optimized
    min-spare-threads: 10
```

**Optimized:**
```yaml
server:
  tomcat:
    max-threads: 500           # ✅ Handle more concurrent requests
    min-spare-threads: 50
    accept-count: 200
    max-connections: 10000
    connection-timeout: 20000
  
  # Use async processing
  servlet:
    async:
      request-timeout: 30000
```

#### **3. Redis Caching Strategy**

**Current Issues:**
```java
@CacheEvict(value = "products", allEntries = true)  // ❌ Clears ALL products
public ProductResponse updateProductStock(Long productId, Integer newStock) {
```

**Optimized:**
```java
@CacheEvict(value = "products", key = "#productId")  // ✅ Only evict specific product
@CachePut(value = "products", key = "#productId")    // ✅ Update cache
public ProductResponse updateProductStock(Long productId, Integer newStock) {
    // ... implementation
}

// Cache stock separately with TTL
@Cacheable(value = "stock", key = "#productId", unless = "#result == null")
public Integer getProductStock(Long productId) {
    return productRepository.findById(productId)
        .map(Product::getStockQuantity)
        .orElse(0);
}
```

**Redis configuration for high throughput:**
```yaml
redis:
  lettuce:
    pool:
      max-active: 50      # ✅ Increase connection pool
      max-idle: 20
      min-idle: 10
  timeout: 2000ms
  
  # Use Redis cluster for production
  cluster:
    nodes:
      - redis-node-1:6379
      - redis-node-2:6379
      - redis-node-3:6379
```

---

## 🔒 **Recommended Implementation**

### **Hybrid Approach: Pessimistic Lock + Redis Cache**

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OptimizedOrderServiceImpl implements OrderService {
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String STOCK_CACHE_KEY = "stock:";
    private static final Duration STOCK_CACHE_TTL = Duration.ofSeconds(30);
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponse createOrder(CreateOrderRequest request) {
        
        // Step 1: Quick stock check from Redis cache
        for (var item : request.getOrderItems()) {
            Integer cachedStock = redisTemplate.opsForValue()
                .get(STOCK_CACHE_KEY + item.getProductId());
            
            if (cachedStock != null && cachedStock < item.getQuantity()) {
                throw new BusinessException("Insufficient stock (cached check)");
            }
        }
        
        // Step 2: Atomic stock reservation with pessimistic lock
        validateAndReserveProductsWithLock(request);
        
        // Step 3: Create order
        Order order = createOrderEntity(request);
        Order savedOrder = orderRepository.save(order);
        
        // Step 4: Update cache and broadcast
        for (var item : request.getOrderItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            
            // Update Redis cache
            redisTemplate.opsForValue().set(
                STOCK_CACHE_KEY + product.getId(),
                product.getStockQuantity(),
                STOCK_CACHE_TTL
            );
            
            // Broadcast stock update via Kafka
            kafkaTemplate.send("product.stock.updated", 
                product.getId().toString(),
                Map.of(
                    "productId", product.getId(),
                    "stockQuantity", product.getStockQuantity(),
                    "timestamp", System.currentTimeMillis()
                )
            );
        }
        
        // Step 5: Publish order event
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        
        return orderMapper.toResponse(savedOrder);
    }
    
    private void validateAndReserveProductsWithLock(CreateOrderRequest request) {
        for (var orderItem : request.getOrderItems()) {
            // Pessimistic lock prevents concurrent modifications
            Product product = productRepository.findByIdWithLock(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(...));
            
            if (!product.hasSufficientStock(orderItem.getQuantity())) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            
            product.reduceStock(orderItem.getQuantity());
            productRepository.save(product);
        }
    }
}
```

---

## 🌐 **Real-Time Stock Updates for Multiple Users**

### **Complete Solution: WebSocket + Kafka + Redis**

```java
@Service
@RequiredArgsConstructor
public class StockUpdateBroadcaster {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @KafkaListener(topics = "product.stock.updated", groupId = "websocket-broadcaster")
    public void handleStockUpdate(Map<String, Object> event) {
        Long productId = ((Number) event.get("productId")).longValue();
        Integer newStock = ((Number) event.get("stockQuantity")).intValue();
        
        // Broadcast to all connected WebSocket clients
        messagingTemplate.convertAndSend(
            "/topic/stock/" + productId,
            Map.of(
                "productId", productId,
                "stockQuantity", newStock,
                "timestamp", System.currentTimeMillis()
            )
        );
    }
}
```

**Frontend Implementation:**
```javascript
// React/Vue/Angular component
const ProductDisplay = ({ productId }) => {
    const [stock, setStock] = useState(null);
    
    useEffect(() => {
        // Connect to WebSocket
        const socket = new SockJS('/ws');
        const client = Stomp.over(socket);
        
        client.connect({}, () => {
            // Subscribe to stock updates for this product
            client.subscribe(`/topic/stock/${productId}`, (message) => {
                const update = JSON.parse(message.body);
                setStock(update.stockQuantity);  // ✅ Real-time update!
                
                // Show notification
                if (update.stockQuantity < 10) {
                    showNotification('Only ' + update.stockQuantity + ' left!');
                }
            });
        });
        
        return () => client.disconnect();
    }, [productId]);
    
    return (
        <div>
            <h3>Stock: {stock}</h3>
            {stock < 10 && <p className="low-stock">Hurry! Only {stock} left!</p>}
        </div>
    );
};
```

---

## 📈 **Performance Optimization Summary**

### **For 10,000 RPS:**

| **Component** | **Current** | **Optimized** | **Impact** |
|---------------|-------------|---------------|------------|
| **App Instances** | 1 | 10-20 (auto-scaled) | ✅ 10-20x throughput |
| **Database** | H2 (memory) | PostgreSQL + 5 read replicas | ✅ 10x read capacity |
| **Connections** | 20 | 100 per instance | ✅ 5x concurrent requests |
| **Locking** | None | Pessimistic/Optimistic | ✅ Prevents race conditions |
| **Caching** | Basic | Redis cluster + smart eviction | ✅ 90% cache hit rate |
| **Stock Updates** | Poll | WebSocket push | ✅ Real-time, 0 latency |
| **Async Processing** | Kafka | Kafka + async endpoints | ✅ Non-blocking operations |

### **Expected Performance:**

- **Baseline (current)**: ~500-1,000 RPS (single instance, no optimizations)
- **With optimizations**: ~10,000-50,000 RPS (horizontal scaling + all optimizations)

### **Scalability Checklist:**

- ✅ **Stateless Design**: Already implemented
- ✅ **Event-Driven**: Kafka already integrated
- ⚠️ **Database Locking**: Needs pessimistic/optimistic locking
- ⚠️ **Connection Pooling**: Needs optimization
- ⚠️ **Caching Strategy**: Needs granular cache eviction
- ❌ **Real-Time Updates**: Needs WebSocket/SSE implementation
- ❌ **Horizontal Scaling**: Needs load balancer and multiple instances
- ❌ **Database Replicas**: Needs read/write splitting

---

## 🎯 **Next Steps for Production**

### **Priority 1: Fix Race Conditions**
1. Add `@Version` field to Product entity
2. Implement pessimistic locking in ProductRepository
3. Add retry logic for optimistic lock failures

### **Priority 2: Scale Infrastructure**
1. Deploy on Kubernetes with auto-scaling (HPA)
2. Use PostgreSQL with read replicas
3. Implement Redis cluster
4. Configure load balancer

### **Priority 3: Real-Time Updates**
1. Add WebSocket configuration
2. Implement stock update broadcaster
3. Integrate with Kafka consumers
4. Update frontend for real-time display

### **Priority 4: Performance Monitoring**
1. Add Prometheus metrics for stock operations
2. Monitor lock contention
3. Track cache hit rates
4. Alert on high latency

---

**The current code is a good foundation but requires these enhancements for 10,000 RPS production workload!**
