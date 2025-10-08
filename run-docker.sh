#!/bin/bash

# Quick Start Script for Docker
# Makes it easy to run the application in Docker

set -e

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   Product Order Service - Docker Quick Start                ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed!"
    echo ""
    echo "Please install Docker Desktop:"
    echo "  Mac: https://docs.docker.com/desktop/install/mac-install/"
    echo "  Windows: https://docs.docker.com/desktop/install/windows-install/"
    echo "  Linux: https://docs.docker.com/engine/install/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed!"
    echo "It usually comes with Docker Desktop. Please reinstall Docker."
    exit 1
fi

echo "✅ Docker installed: $(docker --version)"
echo "✅ Docker Compose installed: $(docker-compose --version)"
echo ""

# Check for AWS credentials
if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo "⚠️  AWS credentials not set!"
    echo ""
    echo "For S3 invoice generation, please set:"
    echo "  export AWS_ACCESS_KEY_ID=your-key"
    echo "  export AWS_SECRET_ACCESS_KEY=your-secret"
    echo "  export AWS_REGION=us-east-1"
    echo "  export AWS_S3_BUCKET_NAME=my-pos-bucket-125"
    echo ""
    read -p "Continue without AWS? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Menu
echo "Select setup mode:"
echo "  1) Minimal (App only - H2 database)"
echo "  2) Full Stack (App + Redis + Kafka)"
echo "  3) Complete (All services + monitoring)"
echo ""
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        echo ""
        echo "🚀 Starting MINIMAL setup..."
        echo "   Services: Application only"
        echo ""
        docker-compose -f docker-compose.dev.yml up --build
        ;;
    2)
        echo ""
        echo "🚀 Starting FULL STACK setup..."
        echo "   Services: App + Redis + Kafka + Zookeeper"
        echo ""
        docker-compose -f docker-compose.local.yml up --build
        ;;
    3)
        echo ""
        echo "🚀 Starting COMPLETE setup..."
        echo "   Services: App + Redis + Kafka + Elasticsearch + Kibana + Prometheus + Grafana + Nginx"
        echo ""
        docker-compose up --build
        ;;
    *)
        echo "❌ Invalid choice!"
        exit 1
        ;;
esac

