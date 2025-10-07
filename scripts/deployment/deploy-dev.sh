#!/bin/bash

# Product Order Service - Development Deployment Script
# This script deploys the application to the development environment

set -e

# Configuration
APP_NAME="product-order-service"
NAMESPACE="dev"
IMAGE_TAG="latest"
REPLICAS=1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        error "kubectl is not installed or not in PATH"
    fi
    
    # Check if docker is installed
    if ! command -v docker &> /dev/null; then
        error "docker is not installed or not in PATH"
    fi
    
    # Check if mvn is installed
    if ! command -v mvn &> /dev/null; then
        error "mvn is not installed or not in PATH"
    fi
    
    log "Prerequisites check passed"
}

# Build application
build_application() {
    log "Building application..."
    
    # Clean and package
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        error "Maven build failed"
    fi
    
    log "Application built successfully"
}

# Build Docker image
build_docker_image() {
    log "Building Docker image..."
    
    # Build image
    docker build -t ${APP_NAME}:${IMAGE_TAG} .
    
    if [ $? -ne 0 ]; then
        error "Docker build failed"
    fi
    
    log "Docker image built successfully"
}

# Deploy to Kubernetes
deploy_to_kubernetes() {
    log "Deploying to Kubernetes..."
    
    # Create namespace if it doesn't exist
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
    
    # Apply configurations
    kubectl apply -f k8s/${NAMESPACE}/namespace.yaml
    kubectl apply -f k8s/${NAMESPACE}/configmap.yaml
    kubectl apply -f k8s/${NAMESPACE}/secret.yaml
    kubectl apply -f k8s/${NAMESPACE}/deployment.yaml
    
    log "Kubernetes deployment completed"
}

# Wait for deployment
wait_for_deployment() {
    log "Waiting for deployment to be ready..."
    
    kubectl wait --for=condition=available --timeout=300s deployment/${APP_NAME} -n ${NAMESPACE}
    
    if [ $? -ne 0 ]; then
        error "Deployment failed to become ready"
    fi
    
    log "Deployment is ready"
}

# Check deployment status
check_deployment_status() {
    log "Checking deployment status..."
    
    # Get pod status
    kubectl get pods -n ${NAMESPACE} -l app=${APP_NAME}
    
    # Get service status
    kubectl get services -n ${NAMESPACE} -l app=${APP_NAME}
    
    # Get deployment status
    kubectl get deployments -n ${NAMESPACE} -l app=${APP_NAME}
    
    log "Deployment status check completed"
}

# Run health checks
run_health_checks() {
    log "Running health checks..."
    
    # Get service URL
    SERVICE_URL=$(kubectl get service ${APP_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [ -z "$SERVICE_URL" ]; then
        SERVICE_URL="localhost:8080"
    fi
    
    # Wait for service to be ready
    sleep 30
    
    # Check health endpoint
    curl -f http://${SERVICE_URL}/actuator/health || warn "Health check failed"
    
    log "Health checks completed"
}

# Main deployment function
main() {
    log "Starting development deployment..."
    
    check_prerequisites
    build_application
    build_docker_image
    deploy_to_kubernetes
    wait_for_deployment
    check_deployment_status
    run_health_checks
    
    log "Development deployment completed successfully!"
    log "Application is available at: http://localhost:8080"
    log "Swagger UI: http://localhost:8080/swagger-ui.html"
    log "H2 Console: http://localhost:8080/h2-console"
}

# Run main function
main "$@"
