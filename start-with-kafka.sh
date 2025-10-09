#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   Starting Application with Docker Kafka + Kafka UI          ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed!"
    echo ""
    echo "Please install Docker Desktop:"
    echo "  Mac: https://docs.docker.com/desktop/install/mac-install/"
    echo ""
    echo "After installation:"
    echo "  1. Open Docker Desktop"
    echo "  2. Wait for it to start (whale icon in menu bar)"
    echo "  3. Run this script again"
    exit 1
fi

# Check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running!"
    echo ""
    echo "Please start Docker Desktop and try again."
    exit 1
fi

echo "✅ Docker is installed and running"
echo ""

# Set AWS credentials
if [ -z "$AWS_ACCESS_KEY_ID" ]; then
    echo "⚠️  AWS credentials not set"
    echo "Set them with:"
    echo "  export AWS_ACCESS_KEY_ID=your-key"
    echo "  export AWS_SECRET_ACCESS_KEY=your-secret"
    echo ""
fi

# Start Kafka infrastructure
echo "1. Starting Kafka, Zookeeper, Redis, and Kafka UI..."
docker-compose -f docker-compose.local.yml up -d zookeeper kafka redis kafka-ui

echo ""
echo "2. Waiting for Kafka to be ready (30 seconds)..."
sleep 30

# Check Kafka health
if docker exec kafka kafka-topics --list --bootstrap-server localhost:9092 &> /dev/null; then
    echo "   ✅ Kafka is ready!"
else
    echo "   ⚠️  Kafka might still be starting... wait a bit longer"
fi

echo ""
echo "3. Starting application..."
docker-compose -f docker-compose.local.yml up -d app

echo ""
echo "4. Waiting for application to start (40 seconds)..."
sleep 40

# Check app health
if curl -s http://localhost:8080/product-order-service/actuator/health | grep -q "UP"; then
    echo "   ✅ Application is ready!"
else
    echo "   ⚠️  Application might still be starting..."
fi

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                    ALL SERVICES STARTED! ✅                  ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "🌐 Access Points:"
echo "  • Application:  http://localhost:8080/product-order-service"
echo "  • Swagger UI:   http://localhost:8080/product-order-service/swagger-ui.html"
echo "  • Kafka UI:     http://localhost:8090  ⭐ View Kafka messages here!"
echo "  • H2 Console:   http://localhost:8080/product-order-service/h2-console"
echo ""
echo "📊 View Kafka Events:"
echo "  1. Open http://localhost:8090"
echo "  2. Click 'Topics'"
echo "  3. Click on any topic (e.g., 'order.created')"
echo "  4. See messages in real-time!"
echo ""
echo "🧪 Test:"
echo "  1. Create an order in Swagger UI"
echo "  2. Go to Kafka UI and see the event appear"
echo "  3. Check 'order.created' topic for the message"
echo ""
echo "🛑 Stop all services:"
echo "  docker-compose -f docker-compose.local.yml down"
echo ""

