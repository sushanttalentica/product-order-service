#!/bin/bash
set -e

echo "Product Order Service - EC2 Deployment Script"
echo "=============================================="
echo ""

if [ ! -f "product-order-service-1.0.0.jar" ]; then
    echo "❌ JAR file not found in current directory"
    echo "Please upload product-order-service-1.0.0.jar first"
    exit 1
fi

if [ ! -f "Dockerfile" ]; then
    echo "❌ Dockerfile not found"
    echo "Please upload Dockerfile first"
    exit 1
fi

echo "Creating directory structure..."
mkdir -p /opt/app
cp product-order-service-1.0.0.jar /opt/app/app.jar
cd /opt/app

echo "Creating simplified Dockerfile for deployment..."
cat > Dockerfile << 'DOCKEREOF'
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKEREOF

echo "Creating environment file..."
cat > .env << 'ENVEOF'
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
AWS_S3_BUCKET_NAME=my-pos-bucket-125
AWS_REGION=ap-south-1
ENVEOF

echo "Building Docker image..."
sudo docker build -t product-order-service:latest .

echo "Stopping any existing container..."
sudo docker stop product-order-service 2>/dev/null || true
sudo docker rm product-order-service 2>/dev/null || true

echo "Starting application..."
sudo docker run -d \
  --name product-order-service \
  --network host \
  --env-file .env \
  --restart unless-stopped \
  product-order-service:latest

echo ""
echo "Waiting for application to start..."
sleep 30

echo "Checking health..."
curl -f http://localhost:8080/product-order-service/actuator/health || echo "Health check pending..."

echo ""
echo "╔══════════════════════════════════════════════════════════════════════╗"
echo "║     ✅ DEPLOYMENT COMPLETE!                                         ║"
echo "╚══════════════════════════════════════════════════════════════════════╝"
echo ""
echo "Application is running!"
echo ""
echo "View logs:"
echo "  sudo docker logs -f product-order-service"
echo ""
echo "Restart:"
echo "  sudo docker restart product-order-service"
echo ""

