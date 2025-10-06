# Product & Order Service - Enterprise E-commerce Platform

A comprehensive, enterprise-grade e-commerce platform built with Spring Boot, featuring microservices architecture, event-driven design, and cloud-native deployment capabilities.

## 🏗️ Architecture Overview

This service implements a sophisticated e-commerce platform with the following key components:

### Core Services
- **Product Service**: Manages products, categories, pricing, and inventory
- **Order Service**: Handles order placement, processing, and lifecycle management
- **Payment Service**: Processes payments via gRPC APIs with gateway integration
- **Notification Service**: Sends email notifications for order and payment events
- **Inventory Service**: Manages stock levels and inventory operations
- **Invoice Service**: Generates PDF invoices and uploads to AWS S3

### Technology Stack
- **Backend**: Spring Boot 3.x, Java 17
- **Database**: MySQL 8.0, H2 (development)
- **Caching**: Redis, Caffeine
- **Message Broker**: Apache Kafka
- **API Communication**: gRPC, REST APIs
- **Security**: Spring Security with JWT
- **Cloud**: AWS S3, AWS RDS, AWS ElastiCache
- **Monitoring**: Prometheus, Grafana, CloudWatch
- **Infrastructure**: Docker, Terraform, AWS EC2

## 🚀 Features

### Product Management
- ✅ Product CRUD operations with categories
- ✅ Advanced search and filtering
- ✅ Price range queries
- ✅ Inventory management
- ✅ Low stock alerts
- ✅ Product categorization

### Order Management
- ✅ Order placement and processing
- ✅ Order status tracking
- ✅ Order cancellation
- ✅ Order history and analytics
- ✅ Customer-specific orders

### Payment Processing
- ✅ gRPC-based payment service
- ✅ Multiple payment methods
- ✅ Payment gateway integration
- ✅ Payment status tracking
- ✅ Refund processing
- ✅ Payment event publishing

### Event-Driven Architecture
- ✅ Kafka event publishing
- ✅ Asynchronous service communication
- ✅ Event sourcing for audit trails
- ✅ Real-time notifications

### Security & Authentication
- ✅ JWT-based authentication
- ✅ Role-based authorization (ADMIN/CUSTOMER)
- ✅ Secure API endpoints
- ✅ Input validation and sanitization

### Cloud Integration
- ✅ AWS S3 for file storage
- ✅ PDF invoice generation
- ✅ Public access URLs
- ✅ Cloud-native deployment

### Monitoring & Observability
- ✅ Health check endpoints
- ✅ Prometheus metrics
- ✅ Comprehensive logging
- ✅ CloudWatch integration
- ✅ Performance monitoring

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- MySQL 8.0+ (for production)
- Redis (for caching)
- Apache Kafka
- AWS Account (for cloud deployment)

## 🛠️ Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd product-order-service
```

### 2. Start Infrastructure Services
```bash
docker-compose up -d mysql redis kafka zookeeper
```

### 3. Configure Environment Variables
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ecommerce_db
export DB_USERNAME=root
export DB_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

### 4. Build and Run
```bash
mvn clean package
java -jar target/product-order-service-1.0.0.jar
```

### 5. Access the Application
- **Application**: http://localhost:8080/product-order-service
- **Health Check**: http://localhost:8080/product-order-service/actuator/health
- **H2 Console**: http://localhost:8080/product-order-service/h2-console

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -t product-order-service:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Access Services
- **Application**: http://localhost:8080
- **Grafana**: http://localhost:3000 (admin/admin)
- **Kibana**: http://localhost:5601
- **Prometheus**: http://localhost:9090

## ☁️ AWS Deployment

### 1. Prerequisites
- AWS CLI configured
- Terraform installed
- SSH key pair created

### 2. Deploy Infrastructure
```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### 3. Configure Environment
```bash
export DB_HOST=$(terraform output -raw database_endpoint)
export REDIS_HOST=$(terraform output -raw redis_endpoint)
export KAFKA_BOOTSTRAP_SERVERS=$(terraform output -raw kafka_bootstrap_brokers)
export S3_BUCKET_NAME=$(terraform output -raw s3_bucket_name)
```

### 4. Access Application
```bash
echo "Application URL: $(terraform output -raw application_url)"
```

## 📚 API Documentation

### Product APIs
```bash
# Get all products
GET /api/v1/products

# Get product by ID
GET /api/v1/products/{id}

# Search products
GET /api/v1/products/search?name={name}

# Create product (ADMIN only)
POST /api/v1/products
```

### Order APIs
```bash
# Create order
POST /api/v1/orders

# Get order by ID
GET /api/v1/orders/{id}

# Get orders by customer
GET /api/v1/orders/customer/{customerId}

# Update order status (ADMIN only)
PATCH /api/v1/orders/{id}/status?status={status}
```

### Payment APIs (gRPC)
```protobuf
service PaymentService {
  rpc ProcessPayment(ProcessPaymentRequest) returns (ProcessPaymentResponse);
  rpc GetPayment(GetPaymentRequest) returns (GetPaymentResponse);
  rpc RefundPayment(RefundPaymentRequest) returns (RefundPaymentResponse);
}
```

## 🔧 Configuration

### Application Properties
```yaml
server:
  port: 8080
  servlet:
    context-path: /product-order-service

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: root
    password: password
  
  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092

security:
  jwt:
    secret: mySecretKey
    expiration: 86400000

aws:
  s3:
    bucket-name: product-order-invoices
    region: us-east-1
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Load Testing
```bash
# Install k6
curl https://github.com/grafana/k6/releases/download/v0.45.0/k6-v0.45.0-linux-amd64.tar.gz -L | tar xvz --strip-components 1

# Run load tests
k6 run load-test.js
```

## 📊 Monitoring

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: `/actuator/health/db`
- **Cache Health**: `/actuator/health/redis`
- **Kafka Health**: `/actuator/health/kafka`

### Metrics
- **Prometheus Metrics**: `/actuator/prometheus`
- **Custom Metrics**: Business metrics for orders, payments, etc.
- **JVM Metrics**: Memory, CPU, GC metrics

### Logging
- **Application Logs**: Structured JSON logging
- **Access Logs**: HTTP request/response logging
- **Audit Logs**: Security and business event logging

## 🔒 Security

### Authentication
- JWT tokens with configurable expiration
- Role-based access control (RBAC)
- Secure password encoding with BCrypt

### Authorization
- **CUSTOMER**: Can place orders, view own orders
- **ADMIN**: Full access to all operations

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- CSRF protection

## 🚀 Performance

### Caching Strategy
- **Product Cache**: Caffeine in-memory cache
- **Redis Cache**: Distributed caching
- **Database Cache**: Query result caching

### Database Optimization
- Connection pooling
- Query optimization
- Index optimization
- Read replicas for scaling

### Message Processing
- Asynchronous event processing
- Batch processing for high throughput
- Dead letter queues for error handling

## 🔄 Event Flow

### Order Processing Flow
1. **Order Created** → Kafka Event → Inventory Service
2. **Payment Processed** → Kafka Event → Notification Service
3. **Order Completed** → Kafka Event → Invoice Service
4. **Invoice Generated** → S3 Upload → Customer Notification

### Event Topics
- `order.created`
- `order.status.updated`
- `order.cancelled`
- `payment.processed`
- `payment.failed`
- `inventory.reserved`
- `inventory.released`
- `notification.email`

## 📈 Scalability

### Horizontal Scaling
- Auto Scaling Groups for EC2 instances
- Load balancer for traffic distribution
- Database read replicas
- Redis cluster for caching

### Vertical Scaling
- Configurable JVM heap size
- Database instance scaling
- Cache memory optimization

## 🛡️ Disaster Recovery

### Backup Strategy
- Automated database backups
- S3 cross-region replication
- Configuration backup
- Log archival

### High Availability
- Multi-AZ deployment
- Health check monitoring
- Automatic failover
- Circuit breakers

## 📝 Development Guidelines

### Code Standards
- Java 17 features (Streams, Optional, Records)
- Clean Code principles
- SOLID design patterns
- Comprehensive documentation

### Testing Strategy
- Unit tests with 80%+ coverage
- Integration tests for APIs
- End-to-end tests for workflows
- Performance tests for scalability

### CI/CD Pipeline
- Automated testing
- Code quality checks
- Security scanning
- Automated deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation
- Review the API specifications
- Contact the development team

## 🔮 Future Enhancements

- [ ] GraphQL API support
- [ ] Microservices mesh
- [ ] Advanced analytics
- [ ] Machine learning integration
- [ ] Mobile app support
- [ ] Multi-tenant architecture
- [ ] Advanced security features
- [ ] Real-time dashboards

---

**Built with ❤️ using Spring Boot, Java 17, and modern cloud technologies**
