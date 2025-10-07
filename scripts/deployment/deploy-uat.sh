#!/bin/bash

# Product Order Service - UAT Deployment Script
# This script deploys the application to the UAT environment

set -e

# Configuration
APP_NAME="product-order-service"
NAMESPACE="uat"
IMAGE_TAG="${BUILD_NUMBER:-latest}"
REPLICAS=2

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

# Run tests
run_tests() {
    log "Running tests..."
    
    # Unit tests
    mvn test
    
    if [ $? -ne 0 ]; then
        error "Unit tests failed"
    fi
    
    log "Tests completed successfully"
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

# Run integration tests
run_integration_tests() {
    log "Running integration tests..."
    
    # Get service URL
    SERVICE_URL=$(kubectl get service ${APP_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [ -z "$SERVICE_URL" ]; then
        SERVICE_URL="localhost:8080"
    fi
    
    # Wait for service to be ready
    sleep 30
    
    # Check health endpoint
    curl -f http://${SERVICE_URL}/actuator/health || error "Health check failed"
    
    # Run API tests
    mvn verify -Pintegration-tests -Dtest.server.url=http://${SERVICE_URL}
    
    if [ $? -ne 0 ]; then
        error "Integration tests failed"
    fi
    
    log "Integration tests completed successfully"
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

# Main deployment function
main() {
    log "Starting UAT deployment..."
    
    check_prerequisites
    run_tests
    build_application
    build_docker_image
    deploy_to_kubernetes
    wait_for_deployment
    run_integration_tests
    check_deployment_status
    
    log "UAT deployment completed successfully!"
    log "Application is available at: http://uat-api.yourdomain.com"
    log "Swagger UI: http://uat-api.yourdomain.com/swagger-ui.html"
}

# Run main function
main "$@"
