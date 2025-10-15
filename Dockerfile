# Multi-stage Docker build for Product Order Service
# Multi-module Maven project structure
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn

COPY core/pom.xml core/
COPY payment/pom.xml payment/
COPY invoice/pom.xml invoice/
COPY inventory/pom.xml inventory/
COPY notification/pom.xml notification/
COPY api/pom.xml api/
COPY application/pom.xml application/

COPY core/src core/src
COPY payment/src payment/src
COPY invoice/src invoice/src
COPY inventory/src inventory/src
COPY notification/src notification/src
COPY api/src api/src
COPY application/src application/src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=build /app/application/target/application-*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/product-order-service/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
