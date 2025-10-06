# Architecture Documentation

## Product Order Service Architecture

This document describes the overall architecture of the Product Order Service.

### System Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │    │  Mobile Client  │    │  Admin Panel   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │      API Gateway         │
                    │    (Load Balancer)       │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │   Product Order Service   │
                    │   (Spring Boot App)       │
                    └─────────────┬─────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                       │                        │
        ▼                       ▼                        ▼
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   H2 DB     │    │   Kafka Broker  │    │   AWS S3        │
│ (In-Memory) │    │   (Events)      │    │ (File Storage)  │
└─────────────┘    └─────────────────┘    └─────────────────┘
```

### Component Architecture

#### 1. Presentation Layer
- **Controllers**: REST API endpoints
- **DTOs**: Data Transfer Objects
- **Mappers**: Entity-DTO conversion
- **Exception Handlers**: Global error handling

#### 2. Business Logic Layer
- **Services**: Core business logic
- **Domain Entities**: Business entities
- **Repositories**: Data access layer
- **Event Publishers**: Kafka event publishing

#### 3. Data Layer
- **H2 Database**: In-memory database
- **JPA/Hibernate**: ORM framework
- **Repository Pattern**: Data access abstraction

#### 4. Integration Layer
- **AWS S3**: File storage
- **Kafka**: Event streaming
- **gRPC**: Internal communication
- **Payment Gateway**: External payment processing

### Technology Stack

#### Backend Framework
- **Spring Boot 3.2.0**: Main framework
- **Spring Security**: Authentication & Authorization
- **Spring Data JPA**: Data persistence
- **Spring Kafka**: Event streaming
- **Spring Web**: REST API

#### Database
- **H2 Database**: In-memory database for development
- **JPA/Hibernate**: ORM framework
- **HikariCP**: Connection pooling

#### External Services
- **AWS S3**: File storage for invoices
- **Apache Kafka**: Event streaming
- **gRPC**: High-performance RPC

#### Security
- **JWT**: Token-based authentication
- **BCrypt**: Password hashing
- **Role-based Access Control**: Authorization

#### Documentation
- **Swagger/OpenAPI**: API documentation
- **SpringDoc**: Documentation generation

### Design Patterns

#### 1. Layered Architecture
```
Controller Layer
    ↓
Service Layer
    ↓
Repository Layer
    ↓
Database Layer
```

#### 2. Repository Pattern
- Abstracts data access logic
- Provides consistent interface
- Enables easy testing

#### 3. Service Layer Pattern
- Encapsulates business logic
- Provides transaction management
- Enables service composition

#### 4. DTO Pattern
- Separates internal and external data models
- Provides data validation
- Enables API versioning

#### 5. Event-Driven Architecture
- Loose coupling between services
- Asynchronous processing
- Scalable system design

### Security Architecture

#### Authentication Flow
```
1. User Login Request
   ↓
2. Credential Validation
   ↓
3. JWT Token Generation
   ↓
4. Token Storage (Client)
   ↓
5. Token Validation (API Calls)
```

#### Authorization Flow
```
1. API Request with Token
   ↓
2. Token Validation
   ↓
3. User Role Extraction
   ↓
4. Resource Access Check
   ↓
5. Request Processing
```

### Data Flow Architecture

#### Request Processing
```
Client Request
    ↓
API Gateway
    ↓
Authentication Filter
    ↓
Authorization Check
    ↓
Controller
    ↓
Service Layer
    ↓
Repository Layer
    ↓
Database
    ↓
Response
```

#### Event Processing
```
Business Event
    ↓
Event Publisher
    ↓
Kafka Topic
    ↓
Event Consumer
    ↓
External Service
```

### Scalability Considerations

#### Horizontal Scaling
- Stateless application design
- Load balancer support
- Database connection pooling
- Event-driven architecture

#### Performance Optimization
- Connection pooling
- Caching strategies
- Asynchronous processing
- Database indexing

#### Monitoring and Observability
- Application metrics
- Health checks
- Log aggregation
- Performance monitoring

### Deployment Architecture

#### Development Environment
- Local development with H2 database
- Embedded Kafka for testing
- Local S3 simulation

#### Production Environment
- Containerized deployment
- Kubernetes orchestration
- External database
- Managed Kafka service
- AWS S3 integration
