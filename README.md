# Product Order Service - Multi-Module Application

E-commerce platform with microservices-ready architecture.

## 🏗️ Architecture

Multi-module Maven project (7 modules):

```
├── core/           Domain entities, services, repositories
├── payment/        Payment processing + gRPC
├── invoice/        Invoice generation + S3
├── inventory/      Stock management + Kafka
├── notification/   Notifications + Kafka
├── api/            REST controllers + OpenAPI
└── application/    Main Spring Boot app
```

## 🚀 Quick Start

### Build
```bash
mvn clean install -DskipTests
```

### Run
```bash
source set-env.sh
cd application && mvn spring-boot:run
```

### Access
- API: `http://localhost:8080/product-order-service/api/v1`
- Swagger: `http://localhost:8080/product-order-service/swagger-ui.html`
- H2 Console: `http://localhost:8080/product-order-service/h2-console`

### Credentials
- Admin: `admin / admin123`

## 📋 Features

- JWT Authentication
- Product Catalog
- Order Management
- Payment Processing (Simulated)
- Invoice Generation (PDF to S3)
- Real-time Stock Updates (WebSocket)
- Event-Driven Architecture (Kafka)
- Caching (Redis)

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.1.5
- Maven Multi-Module (7 modules)
- H2 Database
- Redis
- Kafka
- gRPC
- AWS S3
- OpenAPI 3.0

## 🏛️ Architecture Principles

- ✅ Constructor injection (no @Autowired, no @RequiredArgsConstructor on services)
- ✅ Explicit constructors in all service classes
- ✅ Lombok restricted to @Slf4j, @Getter, @Setter, @Data
- ✅ Multi-module Maven structure
- ✅ OpenAPI-first API contracts

## 📝 Important Notes

**Order Status Update:** Use `PATCH` method
```bash
curl -X PATCH /api/v1/orders/{id}/status?status=PROCESSING
```

**Invoice Generation:** Requires order status = DELIVERED or COMPLETED

**Payment Simulation:** ~90% success rate (simulated gateway)

## 🐳 Docker

```bash
docker-compose -f docker-compose.local.yml up
```

## ☁️ AWS Deployment

See `terraform/` directory for Infrastructure as Code.

## 📄 License

Private Project

