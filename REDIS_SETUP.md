# Redis Setup Documentation

## Redis Configuration for Product Order Service

This document describes the Redis setup and configuration for the Product Order Service.

### Redis Overview

Redis is used for caching and session management in the Product Order Service to improve performance and scalability.

### Redis Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Redis Architecture                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐│
│  │   Cache     │  │  Session    │  │   Rate      │  │   Pub/Sub   ││
│  │   Layer     │  │  Storage    │  │  Limiting   │  │   Events    ││
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### Redis Use Cases

#### 1. Caching
- **Product Cache**: Frequently accessed products
- **Category Cache**: Product categories
- **Customer Cache**: Customer information
- **Order Cache**: Recent orders

#### 2. Session Management
- **JWT Token Storage**: Token blacklisting
- **User Sessions**: Active user sessions
- **Rate Limiting**: API rate limiting
- **Temporary Data**: Short-lived data storage

#### 3. Event Publishing
- **Order Events**: Order status changes
- **Payment Events**: Payment notifications
- **Inventory Events**: Stock updates
- **Notification Events**: User notifications

### Redis Configuration

#### Application Configuration
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

#### Redis Properties
```properties
# Redis Connection
redis.host=localhost
redis.port=6379
redis.password=
redis.database=0

# Connection Pool
redis.pool.max-active=8
redis.pool.max-idle=8
redis.pool.min-idle=0
redis.pool.max-wait=-1

# Timeout Settings
redis.timeout=2000
redis.command-timeout=2000
```

### Redis Setup

#### 1. Local Development Setup
```bash
# Install Redis
brew install redis

# Start Redis
redis-server

# Test Connection
redis-cli ping
```

#### 2. Docker Setup
```yaml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

volumes:
  redis_data:
```

#### 3. Kubernetes Setup
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-data
          mountPath: /data
      volumes:
      - name: redis-data
        persistentVolumeClaim:
          claimName: redis-pvc
```

### Redis Data Structures

#### 1. String Operations
```java
// Cache product information
redisTemplate.opsForValue().set("product:1", productJson, Duration.ofMinutes(30));

// Get cached product
String productJson = redisTemplate.opsForValue().get("product:1");
```

#### 2. Hash Operations
```java
// Cache customer information
redisTemplate.opsForHash().putAll("customer:1", customerMap);

// Get customer field
String email = (String) redisTemplate.opsForHash().get("customer:1", "email");
```

#### 3. List Operations
```java
// Cache recent orders
redisTemplate.opsForList().leftPush("recent_orders:user:1", orderJson);

// Get recent orders
List<String> recentOrders = redisTemplate.opsForList().range("recent_orders:user:1", 0, 9);
```

#### 4. Set Operations
```java
// Cache product categories
redisTemplate.opsForSet().add("categories", "Electronics", "Clothing", "Books");

// Check category membership
boolean isMember = redisTemplate.opsForSet().isMember("categories", "Electronics");
```

### Caching Strategy

#### 1. Cache Keys
```
# Product Cache
product:{id}                    # Product details
product:category:{categoryId}  # Products by category
product:search:{query}         # Search results

# Customer Cache
customer:{id}                  # Customer details
customer:email:{email}         # Customer by email

# Order Cache
order:{id}                    # Order details
order:customer:{customerId}   # Orders by customer
order:recent:{customerId}     # Recent orders

# Session Cache
session:{token}               # JWT token blacklist
rate_limit:{ip}:{endpoint}    # Rate limiting
```

#### 2. Cache TTL
```java
// Product cache - 30 minutes
redisTemplate.opsForValue().set("product:1", productJson, Duration.ofMinutes(30));

// Customer cache - 1 hour
redisTemplate.opsForValue().set("customer:1", customerJson, Duration.ofHours(1));

// Order cache - 2 hours
redisTemplate.opsForValue().set("order:1", orderJson, Duration.ofHours(2));

// Session cache - 24 hours
redisTemplate.opsForValue().set("session:token123", "blacklisted", Duration.ofHours(24));
```

### Redis Configuration Classes

#### 1. Redis Configuration
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);
        config.setDatabase(0);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        return builder.build();
    }

    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

#### 2. Cache Service
```java
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void cacheProduct(Product product) {
        String key = "product:" + product.getId();
        redisTemplate.opsForValue().set(key, product, Duration.ofMinutes(30));
    }

    public Optional<Product> getCachedProduct(Long productId) {
        String key = "product:" + productId;
        Product product = (Product) redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(product);
    }

    public void evictProduct(Long productId) {
        String key = "product:" + productId;
        redisTemplate.delete(key);
    }
}
```

### Rate Limiting

#### 1. Rate Limiting Implementation
```java
@Component
public class RateLimitingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean isAllowed(String ip, String endpoint) {
        String key = "rate_limit:" + ip + ":" + endpoint;
        String count = (String) redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return true;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= 100) { // 100 requests per minute
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
}
```

### Monitoring and Maintenance

#### 1. Redis Monitoring
```bash
# Monitor Redis
redis-cli monitor

# Check Redis info
redis-cli info

# Check memory usage
redis-cli info memory

# Check connected clients
redis-cli client list
```

#### 2. Redis Maintenance
```bash
# Clear all data
redis-cli flushall

# Clear current database
redis-cli flushdb

# Save data to disk
redis-cli bgsave

# Check Redis logs
tail -f /var/log/redis/redis-server.log
```

### Performance Optimization

#### 1. Memory Optimization
- **Max Memory**: Set appropriate max memory
- **Eviction Policy**: Use LRU eviction policy
- **Data Compression**: Enable compression for large values
- **Memory Monitoring**: Monitor memory usage

#### 2. Network Optimization
- **Connection Pooling**: Use connection pooling
- **Pipelining**: Use Redis pipelining for batch operations
- **Compression**: Enable network compression
- **Keep-Alive**: Use TCP keep-alive

#### 3. Persistence Optimization
- **RDB Snapshots**: Configure RDB snapshots
- **AOF Logging**: Use AOF for durability
- **Background Saving**: Use background saving
- **Compression**: Enable RDB compression
