#!/bin/bash

# Product Order Service - Production Deployment Script
# This script deploys the application to the production environment

set -e

# Configuration
APP_NAME="product-order-service"
NAMESPACE="prod"
IMAGE_TAG="${BUILD_NUMBER:-latest}"
REPLICAS=3

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
    
    # Check if helm is installed
    if ! command -v helm &> /dev/null; then
        warn "helm is not installed, skipping helm operations"
    fi
    
    log "Prerequisites check passed"
}

# Run security scans
run_security_scans() {
    log "Running security scans..."
    
    # Dependency check
    mvn dependency-check:check
    
    if [ $? -ne 0 ]; then
        error "Security scan failed"
    fi
    
    log "Security scans completed"
}

# Run tests
run_tests() {
    log "Running tests..."
    
    # Unit tests
    mvn test
    
    if [ $? -ne 0 ]; then
        error "Unit tests failed"
    fi
    
    # Integration tests
    mvn verify -Pintegration-tests
    
    if [ $? -ne 0 ]; then
        error "Integration tests failed"
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
    
    # Tag for registry
    docker tag ${APP_NAME}:${IMAGE_TAG} ${REGISTRY_URL}/${APP_NAME}:${IMAGE_TAG}
    
    # Push to registry
    docker push ${REGISTRY_URL}/${APP_NAME}:${IMAGE_TAG}
    
    log "Docker image built and pushed successfully"
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
    
    kubectl wait --for=condition=available --timeout=600s deployment/${APP_NAME} -n ${NAMESPACE}
    
    if [ $? -ne 0 ]; then
        error "Deployment failed to become ready"
    fi
    
    log "Deployment is ready"
}

# Run smoke tests
run_smoke_tests() {
    log "Running smoke tests..."
    
    # Get service URL
    SERVICE_URL=$(kubectl get service ${APP_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    
    if [ -z "$SERVICE_URL" ]; then
        SERVICE_URL="localhost:8080"
    fi
    
    # Wait for service to be ready
    sleep 60
    
    # Check health endpoint
    curl -f http://${SERVICE_URL}/actuator/health || error "Health check failed"
    
    # Check API endpoints
    curl -f http://${SERVICE_URL}/api/v1/products || error "Products API check failed"
    
    log "Smoke tests completed successfully"
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
    
    # Get ingress status
    kubectl get ingress -n ${NAMESPACE} -l app=${APP_NAME}
    
    log "Deployment status check completed"
}

# Setup monitoring
setup_monitoring() {
    log "Setting up monitoring..."
    
    # Apply monitoring configurations
    kubectl apply -f scripts/monitoring/
    
    log "Monitoring setup completed"
}

# Main deployment function
main() {
    log "Starting production deployment..."
    
    check_prerequisites
    run_security_scans
    run_tests
    build_application
    build_docker_image
    deploy_to_kubernetes
    wait_for_deployment
    run_smoke_tests
    check_deployment_status
    setup_monitoring
    
    log "Production deployment completed successfully!"
    log "Application is available at: https://api.yourdomain.com"
    log "Swagger UI: https://api.yourdomain.com/swagger-ui.html"
    log "Monitoring: https://monitoring.yourdomain.com"
}

# Run main function
main "$@"
