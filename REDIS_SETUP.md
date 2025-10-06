# Redis Setup Guide for Product Order Service

## Overview

This guide provides instructions for setting up Redis as the caching layer for the Product Order Service across different environments.

## Redis Configuration

### Environment-Specific Configurations

#### Development Environment
```yaml
# Redis Configuration
data:
  redis:
    host: localhost
    port: 6379
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 5
        min-idle: 2
        max-wait: 1000ms

# Cache Configuration
cache:
  type: redis
  redis:
    time-to-live: 600000 # 10 minutes
    cache-null-values: false
    enable-statistics: true
```

#### UAT Environment
```yaml
# Redis Configuration
data:
  redis:
    host: ${REDIS_HOST:uat-redis-server}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 2000ms

# Cache Configuration
cache:
  type: redis
  redis:
    time-to-live: 1800000 # 30 minutes
    cache-null-values: false
    enable-statistics: true
```

#### Production Environment
```yaml
# Redis Configuration
data:
  redis:
    host: ${REDIS_HOST:prod-redis-cluster}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10
        max-wait: 2000ms

# Cache Configuration
cache:
  type: redis
  redis:
    time-to-live: 3600000 # 1 hour
    cache-null-values: false
    enable-statistics: true
```

## Redis Installation

### Local Development

#### Using Docker
```bash
# Run Redis container
docker run -d --name redis-dev \
  -p 6379:6379 \
  redis:7-alpine

# With password
docker run -d --name redis-dev \
  -p 6379:6379 \
  redis:7-alpine redis-server --requirepass yourpassword
```

#### Using Homebrew (macOS)
```bash
# Install Redis
brew install redis

# Start Redis
brew services start redis

# Test connection
redis-cli ping
```

#### Using apt (Ubuntu/Debian)
```bash
# Install Redis
sudo apt update
sudo apt install redis-server

# Start Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Test connection
redis-cli ping
```

### Kubernetes Deployment

#### Development Redis
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-dev
  namespace: dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis-dev
  template:
    metadata:
      labels:
        app: redis-dev
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-dev
  namespace: dev
spec:
  selector:
    app: redis-dev
  ports:
  - port: 6379
    targetPort: 6379
```

#### UAT Redis
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-uat
  namespace: uat
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis-uat
  template:
    metadata:
      labels:
        app: redis-uat
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: redis-uat
  namespace: uat
spec:
  selector:
    app: redis-uat
  ports:
  - port: 6379
    targetPort: 6379
```

#### Production Redis Cluster
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis-prod
  namespace: prod
spec:
  serviceName: redis-prod
  replicas: 3
  selector:
    matchLabels:
      app: redis-prod
  template:
    metadata:
      labels:
        app: redis-prod
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        volumeMounts:
        - name: redis-data
          mountPath: /data
  volumeClaimTemplates:
  - metadata:
      name: redis-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
---
apiVersion: v1
kind: Service
metadata:
  name: redis-prod
  namespace: prod
spec:
  selector:
    app: redis-prod
  ports:
  - port: 6379
    targetPort: 6379
```

## Redis Configuration

### Basic Configuration
```conf
# redis.conf
# Network
bind 0.0.0.0
port 6379
timeout 300
tcp-keepalive 300

# Memory
maxmemory 256mb
maxmemory-policy allkeys-lru

# Persistence
save 900 1
save 300 10
save 60 10000

# Security
requirepass yourpassword

# Logging
loglevel notice
logfile /var/log/redis/redis-server.log
```

### Production Configuration
```conf
# redis.conf for production
# Network
bind 0.0.0.0
port 6379
timeout 300
tcp-keepalive 300

# Memory
maxmemory 2gb
maxmemory-policy allkeys-lru

# Persistence
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec

# Security
requirepass yourpassword

# Logging
loglevel notice
logfile /var/log/redis/redis-server.log

# Performance
tcp-backlog 511
databases 16
```

## Redis Monitoring

### Health Checks
```bash
# Check Redis status
redis-cli ping

# Check memory usage
redis-cli info memory

# Check connected clients
redis-cli info clients

# Check keyspace
redis-cli info keyspace
```

### Monitoring Commands
```bash
# Monitor real-time commands
redis-cli monitor

# Check slow queries
redis-cli slowlog get 10

# Check configuration
redis-cli config get "*"
```

### Prometheus Metrics
```yaml
# redis-exporter configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-exporter-config
data:
  redis-exporter.yml: |
    redis_addr: "redis://redis-prod:6379"
    redis_password: "${REDIS_PASSWORD}"
    check_keys: "product:*,category:*,order:*"
```

## Redis Security

### Authentication
```bash
# Set password
redis-cli config set requirepass yourpassword

# Authenticate
redis-cli auth yourpassword
```

### Network Security
```bash
# Bind to specific interface
redis-cli config set bind 127.0.0.1

# Disable dangerous commands
redis-cli config set rename-command FLUSHDB ""
redis-cli config set rename-command FLUSHALL ""
redis-cli config set rename-command KEYS ""
```

### SSL/TLS Configuration
```conf
# redis.conf
port 0
tls-port 6380
tls-cert-file /path/to/redis.crt
tls-key-file /path/to/redis.key
tls-ca-cert-file /path/to/ca.crt
```

## Redis Performance Tuning

### Memory Configuration
```conf
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
```

### Persistence Configuration
```conf
# redis.conf
save 900 1
save 300 10
save 60 10000
appendonly yes
appendfsync everysec
```

### Network Configuration
```conf
# redis.conf
tcp-backlog 511
timeout 300
tcp-keepalive 300
```

## Redis Backup and Recovery

### Backup Strategy
```bash
# Create backup
redis-cli bgsave

# Copy RDB file
cp /var/lib/redis/dump.rdb /backup/redis-$(date +%Y%m%d).rdb
```

### Recovery Procedure
```bash
# Stop Redis
systemctl stop redis-server

# Restore RDB file
cp /backup/redis-20240101.rdb /var/lib/redis/dump.rdb

# Start Redis
systemctl start redis-server
```

## Redis Troubleshooting

### Common Issues

#### Connection Issues
```bash
# Check Redis status
systemctl status redis-server

# Check logs
tail -f /var/log/redis/redis-server.log

# Test connection
redis-cli ping
```

#### Memory Issues
```bash
# Check memory usage
redis-cli info memory

# Check memory fragmentation
redis-cli memory usage keyname

# Clear cache
redis-cli flushdb
```

#### Performance Issues
```bash
# Check slow queries
redis-cli slowlog get 10

# Check connected clients
redis-cli info clients

# Monitor commands
redis-cli monitor
```

### Debug Commands
```bash
# Check Redis configuration
redis-cli config get "*"

# Check Redis info
redis-cli info

# Check keyspace
redis-cli info keyspace

# Check replication
redis-cli info replication
```

## Redis Best Practices

### 1. Key Naming
```bash
# Use consistent naming
product:123
category:electronics
order:456
user:789:profile
```

### 2. TTL Management
```bash
# Set appropriate TTL
EXPIRE product:123 3600  # 1 hour
EXPIRE category:electronics 86400  # 1 day
EXPIRE order:456 604800  # 1 week
```

### 3. Memory Management
```bash
# Monitor memory usage
redis-cli info memory

# Use appropriate eviction policy
CONFIG SET maxmemory-policy allkeys-lru
```

### 4. Connection Pooling
```yaml
# application.yml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms
```

This Redis setup guide provides comprehensive instructions for deploying and managing Redis across all environments for the Product Order Service.
