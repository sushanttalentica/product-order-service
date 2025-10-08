# Docker Local Setup Guide

Run the application in Docker - **no Java or Maven installation needed!**

Works identically on **any machine** (Mac, Windows, Linux).

---

## 📋 Prerequisites

Only Docker is required:

```bash
# Check if Docker is installed
docker --version
docker-compose --version
```

If not installed: [Install Docker Desktop](https://www.docker.com/products/docker-desktop)

---

## 🚀 Quick Start (3 Options)

### Option 1: Minimal Setup (Recommended for Testing)

**Just the app - no Kafka/Redis complexity**

```bash
# 1. Set AWS credentials (for S3 invoice generation)
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
export AWS_REGION=us-east-1
export AWS_S3_BUCKET_NAME=my-pos-bucket-125

# 2. Run
docker-compose -f docker-compose.dev.yml up --build

# 3. Access application
# App: http://localhost:8080/product-order-service
# Swagger: http://localhost:8080/product-order-service/swagger-ui.html
# H2 Console: http://localhost:8080/product-order-service/h2-console
```

**That's it!** Application runs with:
- ✅ H2 in-memory database
- ✅ In-memory caching (Caffeine)
- ✅ S3 invoice generation
- ✅ All APIs working

---

### Option 2: Full Stack (App + Redis + Kafka)

**Complete environment with event streaming**

```bash
# 1. Set AWS credentials
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
export AWS_REGION=us-east-1
export AWS_S3_BUCKET_NAME=my-pos-bucket-125

# 2. Run full stack
docker-compose -f docker-compose.local.yml up --build

# 3. Access services
# App: http://localhost:8080/product-order-service
# Swagger: http://localhost:8080/product-order-service/swagger-ui.html
# Redis: localhost:6379
# Kafka: localhost:9093
```

Includes:
- ✅ Application
- ✅ Redis (caching)
- ✅ Kafka + Zookeeper (event streaming)
- ✅ WebSocket support (real-time stock updates)

---

### Option 3: Complete Production-Like Setup

**Full stack with monitoring and load balancing**

```bash
docker-compose up --build
```

Includes everything + Elasticsearch, Kibana, Prometheus, Grafana, Nginx

⚠️ **Note**: Requires `nginx.conf` and `prometheus.yml` configuration files

---

## 🔧 Using Environment Variables

### Method 1: Export (Quick)

```bash
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
docker-compose -f docker-compose.dev.yml up --build
```

### Method 2: .env File (Recommended)

```bash
# 1. Copy example
cp env.example .env

# 2. Edit .env file with your values
nano .env

# 3. Run (Docker Compose auto-loads .env)
docker-compose -f docker-compose.dev.yml up
```

---

## 📱 Testing the Application

### 1. Login and Get Token

```bash
# Admin login
curl -X POST http://localhost:8080/product-order-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Save the token
TOKEN="<your-jwt-token-here>"
```

### 2. Create Product

```bash
curl -X POST http://localhost:8080/product-order-service/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name":"Test Product",
    "description":"Docker test",
    "price":99.99,
    "stockQuantity":100,
    "sku":"TEST-001",
    "categoryId":1
  }'
```

### 3. Create Order

```bash
curl -X POST http://localhost:8080/product-order-service/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -d '{
    "customerId":1,
    "customerEmail":"test@example.com",
    "shippingAddress":"123 Test St",
    "orderItems":[{"productId":1,"quantity":2}]
  }'
```

### 4. Generate Invoice

```bash
ORDER_ID=1

curl -X POST "http://localhost:8080/product-order-service/api/v1/invoices/order/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔍 Monitoring & Debugging

### View Logs

```bash
# Follow logs
docker-compose -f docker-compose.dev.yml logs -f app

# Last 100 lines
docker-compose -f docker-compose.dev.yml logs --tail=100 app
```

### Access Container Shell

```bash
docker exec -it product-order-service-dev /bin/sh

# Inside container
ls -la /app
java -version
```

### Health Check

```bash
curl http://localhost:8080/product-order-service/actuator/health
```

### H2 Database Console

1. Open: http://localhost:8080/product-order-service/h2-console
2. **JDBC URL**: `jdbc:h2:mem:testdb`
3. **Username**: `sa`
4. **Password**: (leave empty)
5. Click "Connect"

---

## 🐛 Remote Debugging with IntelliJ

The application exposes debug port **5005** (only in `docker-compose.local.yml`):

### Setup in IntelliJ:

1. **Run → Edit Configurations**
2. **+ → Remote JVM Debug**
3. **Name**: Docker Debug
4. **Host**: localhost
5. **Port**: 5005
6. **Module classpath**: product-order-service
7. **Click OK**

### Start Debugging:

```bash
# 1. Start app with debug port
docker-compose -f docker-compose.local.yml up --build

# 2. In IntelliJ, set breakpoints
# 3. Run → Debug 'Docker Debug'
# 4. Make API calls - breakpoints will hit!
```

---

## 🛑 Stop & Clean Up

### Stop Services

```bash
# Stop (keeps containers)
docker-compose -f docker-compose.dev.yml stop

# Stop and remove containers
docker-compose -f docker-compose.dev.yml down

# Stop and remove everything (including volumes)
docker-compose -f docker-compose.dev.yml down -v
```

### Clean Docker Cache

```bash
# Remove old images
docker system prune -a

# Remove volumes
docker volume prune
```

---

## 🔄 Rebuild After Code Changes

```bash
# Rebuild and restart
docker-compose -f docker-compose.dev.yml up --build

# Or force clean rebuild
docker-compose -f docker-compose.dev.yml build --no-cache
docker-compose -f docker-compose.dev.yml up
```

---

## 📊 Which Docker Compose to Use?

| Use Case | File | Services | When to Use |
|----------|------|----------|-------------|
| **Quick API Testing** | `docker-compose.dev.yml` | App only | Testing APIs, invoice generation |
| **Full Development** | `docker-compose.local.yml` | App + Redis + Kafka | Testing real-time features, events |
| **Production-Like** | `docker-compose.yml` | All services | Testing monitoring, load balancing |

---

## ✅ Advantages of Docker Approach

1. **No Local Java/Maven** - Works on any machine
2. **Consistent Environment** - Same Java 17, same dependencies
3. **No IntelliJ Issues** - Bypasses IDE configuration problems
4. **Quick Setup** - One command to start everything
5. **Easy Testing** - Spin up/down in seconds
6. **Team Consistency** - Everyone has identical setup
7. **CI/CD Ready** - Same Docker image for dev and prod

---

## 🆚 Docker vs Local Development

### Use Docker When:
- ✅ Setting up on new machine
- ✅ Avoiding Java/Maven installation
- ✅ Need consistent environment
- ✅ Testing full stack
- ✅ IntelliJ configuration issues

### Use Local When:
- ✅ Need faster build iterations
- ✅ Want IDE debugging features
- ✅ Prefer hot-reload development
- ✅ Working on code changes only

**Pro Tip**: Many developers use **Docker for running** the app and **IntelliJ for editing** code!

---

## 🚨 Troubleshooting

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill it
kill -9 <PID>

# Or change port in docker-compose
ports:
  - "8081:8080"  # Use 8081 instead
```

### Out of Memory

```bash
# Increase Docker memory
# Docker Desktop → Settings → Resources → Memory: 4GB+
```

### AWS Credentials Not Working

```bash
# Check if env vars are set
docker-compose -f docker-compose.dev.yml config

# Or pass directly
AWS_ACCESS_KEY_ID=xxx AWS_SECRET_ACCESS_KEY=yyy docker-compose -f docker-compose.dev.yml up
```

### Build Fails

```bash
# Clean everything and rebuild
docker-compose -f docker-compose.dev.yml down -v
docker system prune -a
docker-compose -f docker-compose.dev.yml build --no-cache
docker-compose -f docker-compose.dev.yml up
```

---

## 📝 Summary

**For quick local testing:**
```bash
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
docker-compose -f docker-compose.dev.yml up --build
```

**Access at:** http://localhost:8080/product-order-service/swagger-ui.html

**That's it!** No Java, no Maven, no IntelliJ configuration needed! 🎉

---

## 🔗 Related Documentation

- `README.md` - Main project documentation
- `ARCHITECTURE.md` - System architecture
- `AWS_EC2_DEPLOYMENT.md` - Production deployment (on main branch)
- `terraform/` - Infrastructure as Code

