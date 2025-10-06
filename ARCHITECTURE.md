# Product Order Service - System Architecture

## Overview

The Product Order Service is a microservice-based e-commerce system designed for scalability, reliability, and maintainability. It follows Domain-Driven Design (DDD) principles and implements modern architectural patterns.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           PRODUCT ORDER SERVICE ECOSYSTEM                        │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile Client  │    │  Admin Panel    │    │  External APIs  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │                      │
          └──────────────────────┼──────────────────────┼──────────────────────┘
                                 │                      │
                    ┌─────────────▼─────────────┐       │
                    │     API Gateway          │       │
                    │   (Load Balancer)        │       │
                    └─────────────┬─────────────┘       │
                                  │                     │
                    ┌─────────────▼─────────────┐       │
                    │   Product Order Service  │       │
                    │   (Spring Boot App)      │       │
                    └─────────────┬─────────────┘       │
                                  │                     │
        ┌─────────────────────────┼─────────────────────┼─────────────────────────┐
        │                         │                     │                         │
        ▼                         ▼                     ▼                         ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Database  │    │    Kafka    │    │   Payment  │    │ Notification│
│   (MySQL)   │    │   Cluster   │    │  Service   │    │   Service   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                 │                     │                 │
        │                 │                     │                 │
        ▼                 ▼                     ▼                 ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Cache     │    │   Events    │    │   gRPC      │    │   Email     │
│ (Caffeine)  │    │  Publisher  │    │   APIs      │    │  Service    │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## Microservices Architecture

### 1. Product Order Service (Core Service)
- **Technology**: Spring Boot 3.3.0, Java 17
- **Database**: MySQL (Production), H2 (Development)
- **Cache**: Redis (Distributed)
- **Security**: JWT Authentication
- **API**: RESTful APIs

### 2. Payment Service
- **Technology**: gRPC, Spring Boot
- **Functionality**: Payment processing, authorization
- **Integration**: gRPC communication
- **Events**: Kafka event publishing

### 3. Notification Service
- **Technology**: Spring Boot, Kafka Consumer
- **Functionality**: Email notifications, SMS
- **Integration**: Kafka event consumption

### 4. Inventory Service
- **Technology**: Spring Boot, Kafka Consumer
- **Functionality**: Stock management, inventory updates
- **Integration**: Kafka event consumption

## Data Flow Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DATA FLOW DIAGRAM                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service   │───▶│  Database   │
│  Request    │    │   (Nginx)   │    │   Layer    │    │   (MySQL)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                           │                   │
                           │                   ▼
                           │            ┌─────────────┐
                           │            │   Cache     │
                           │            │ (Caffeine) │
                           │            └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │    Kafka    │
                   │   Events    │
                   └─────────────┘
                           │
                           ▼
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Payment    │  │Notification│  │ Inventory   │
│  Service    │  │   Service   │  │   Service   │
└─────────────┘  └─────────────┘  └─────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DEPLOYMENT ARCHITECTURE                               │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              JENKINS CI/CD                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   Build     │  │   Test      │  │   Package   │  │   Deploy    │          │
│  │   Stage     │  │   Stage     │  │   Stage     │  │   Stage     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              KUBERNETES CLUSTER                               │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                        PRODUCTION ENVIRONMENT                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │   Namespace │  │   Service    │  │   Ingress   │  │   HPA       │    │    │
│  │  │   (prod)    │  │   (5 pods)   │  │   (SSL)     │  │   (5-20)    │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                         UAT ENVIRONMENT                                │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │   Namespace │  │   Service    │  │   Ingress   │  │   HPA       │    │    │
│  │  │   (uat)     │  │   (3 pods)   │  │   (HTTP)    │  │   (3-10)    │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                      DEVELOPMENT ENVIRONMENT                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │   Namespace │  │   Service    │  │   Ingress   │  │   HPA       │    │    │
│  │  │   (dev)     │  │   (2 pods)   │  │   (HTTP)    │  │   (2-5)     │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              INFRASTRUCTURE                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │   MySQL     │  │    Kafka    │  │     S3      │  │ Monitoring │          │
│  │  Database   │  │   Cluster   │  │   Storage   │  │  (Prometheus│          │
│  │  Cluster    │  │             │  │             │  │   + Grafana)│          │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Backend Services
- **Framework**: Spring Boot 3.3.0
- **Language**: Java 17
- **Database**: MySQL 8.0 (Production), H2 (Development)
- **Cache**: Caffeine
- **Message Queue**: Apache Kafka
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3.0 (Swagger)

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: Jenkins
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Storage**: AWS S3

### External Integrations
- **Payment**: gRPC APIs
- **Notifications**: Email/SMS services
- **Inventory**: Stock management APIs
- **Storage**: AWS S3 for PDF invoices

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SECURITY LAYERS                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───▶│   Gateway   │───▶│   Service   │───▶│  Database   │
│  Request    │    │   (SSL/TLS) │    │   (JWT)     │    │ (Encrypted) │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   │
        ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Rate      │    │   CORS      │    │   Role      │    │   Data      │
│  Limiting   │    │  Control    │    │   Based     │    │ Encryption  │
│             │    │             │    │   Access    │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## Performance Architecture

### Caching Strategy
- **Primary Cache**: Redis (Distributed)
- **Cache TTL**: 10 minutes (Dev), 30 minutes (UAT), 1 hour (Prod)
- **Database**: Connection pooling with HikariCP

### Scaling Strategy
- **Horizontal**: Kubernetes HPA (5-20 replicas)
- **Vertical**: Resource limits and requests
- **Database**: Read replicas, connection pooling

### Monitoring
- **Metrics**: Prometheus + Grafana
- **Logs**: ELK Stack
- **Tracing**: Jaeger
- **Health**: Spring Actuator

## API Architecture

### RESTful APIs
- **Products**: CRUD operations, search, filtering
- **Orders**: Order creation, status updates, cancellation
- **Categories**: Category management
- **Health**: Health checks, metrics, info

### gRPC APIs
- **Payment**: Payment processing, authorization
- **Inventory**: Stock updates, availability checks

### Event-Driven Architecture
- **Order Events**: Order created, updated, cancelled
- **Payment Events**: Payment authorized, failed, completed
- **Notification Events**: Email/SMS notifications
- **Inventory Events**: Stock updates, low stock alerts

## Data Architecture

### Database Design
- **Primary Database**: MySQL (ACID compliance)
- **Cache**: Redis (Distributed caching)
- **Message Queue**: Kafka (Event streaming)
- **Storage**: AWS S3 (File storage)

### Data Flow
1. **Read Operations**: Cache → Database
2. **Write Operations**: Database → Cache invalidation
3. **Events**: Service → Kafka → Consumer services
4. **Files**: Service → S3 → Public URLs

## Deployment Strategy

### Environment-Specific Configurations
- **Development**: H2 database, mock services, debug logging
- **UAT**: MySQL database, external services, performance testing
- **Production**: High availability, monitoring, security hardening

### Deployment Pipeline
1. **Build**: Maven build with tests
2. **Package**: Docker image creation
3. **Deploy**: Kubernetes deployment
4. **Verify**: Health checks and smoke tests
5. **Monitor**: Continuous monitoring and alerting

This architecture provides a scalable, maintainable, and production-ready e-commerce microservice system with comprehensive monitoring, security, and deployment capabilities.
