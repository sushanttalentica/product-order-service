# Product Order Service

E-commerce order management system.

## Setup

```bash
mvn clean package
java -jar target/product-order-service-1.0.0.jar
```

## Run with Docker

```bash
docker-compose -f docker-compose.dev.yml up --build
```

## API

- Swagger UI: http://localhost:8080/product-order-service/swagger-ui.html
- Health: http://localhost:8080/product-order-service/actuator/health

## Default Credentials

- Admin: `admin` / `admin123`
- Customer: `customer` / `customer123`

## Tech

Spring Boot 3.1.5, Java 17, MySQL, Redis, Kafka, AWS S3
