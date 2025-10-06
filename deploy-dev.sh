#!/bin/bash

# Development Environment Deployment Script
# This script deploys the Product Order Service to the development environment

set -e

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}[INFO]${NC} Starting deployment to Development environment..."

# Configuration
NAMESPACE="dev"
SERVICE_NAME="product-order-service"
DOCKER_IMAGE="${DOCKER_REGISTRY:-your-docker-registry.com}/${SERVICE_NAME}:${BUILD_TAG:-latest-dev}"

# Function to check if kubectl is available
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}[ERROR]${NC} kubectl is not installed or not in PATH"
        exit 1
    fi
    echo -e "${GREEN}[SUCCESS]${NC} kubectl is available"
}

# Function to check if namespace exists
check_namespace() {
    if kubectl get namespace $NAMESPACE &> /dev/null; then
        echo -e "${GREEN}[SUCCESS]${NC} Namespace $NAMESPACE exists"
    else
        echo -e "${YELLOW}[WARNING]${NC} Namespace $NAMESPACE does not exist, creating..."
        kubectl apply -f k8s/dev/namespace.yaml
    fi
}

# Function to deploy configuration
deploy_config() {
    echo -e "${BLUE}[INFO]${NC} Deploying configuration..."
    kubectl apply -f k8s/dev/configmap.yaml
    kubectl apply -f k8s/dev/secret.yaml
    echo -e "${GREEN}[SUCCESS]${NC} Configuration deployed"
}

# Function to deploy application
deploy_application() {
    echo -e "${BLUE}[INFO]${NC} Deploying application..."
    
    # Update the image in the deployment
    kubectl set image deployment/$SERVICE_NAME $SERVICE_NAME=$DOCKER_IMAGE -n $NAMESPACE
    
    # Wait for rollout to complete
    kubectl rollout status deployment/$SERVICE_NAME -n $NAMESPACE --timeout=300s
    
    echo -e "${GREEN}[SUCCESS]${NC} Application deployed"
}

# Function to check deployment health
check_health() {
    echo -e "${BLUE}[INFO]${NC} Checking deployment health..."
    
    # Get the service URL
    SERVICE_URL=$(kubectl get service $SERVICE_NAME -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
    
    if [ -z "$SERVICE_URL" ]; then
        echo -e "${RED}[ERROR]${NC} Could not get service URL"
        return 1
    fi
    
    # Wait for service to be ready
    echo -e "${BLUE}[INFO]${NC} Waiting for service to be ready..."
    sleep 30
    
    # Health check
    for i in {1..10}; do
        if kubectl exec -n $NAMESPACE deployment/$SERVICE_NAME -- curl -f http://localhost:8080/actuator/health &> /dev/null; then
            echo -e "${GREEN}[SUCCESS]${NC} Health check passed"
            return 0
        else
            echo -e "${YELLOW}[WARNING]${NC} Health check failed, retrying in 10 seconds... (attempt $i/10)"
            sleep 10
        fi
    done
    
    echo -e "${RED}[ERROR]${NC} Health check failed after 10 attempts"
    return 1
}

# Function to show deployment status
show_status() {
    echo -e "${BLUE}[INFO]${NC} Deployment Status:"
    echo "=================="
    
    # Show pods
    echo -e "${YELLOW}Pods:${NC}"
    kubectl get pods -n $NAMESPACE -l app=$SERVICE_NAME
    
    echo ""
    
    # Show services
    echo -e "${YELLOW}Services:${NC}"
    kubectl get services -n $NAMESPACE -l app=$SERVICE_NAME
    
    echo ""
    
    # Show ingress
    echo -e "${YELLOW}Ingress:${NC}"
    kubectl get ingress -n $NAMESPACE -l app=$SERVICE_NAME
}

# Main deployment process
main() {
    echo -e "${BLUE}[INFO]${NC} Deploying to Development Environment"
    echo "=============================================="
    echo "Namespace: $NAMESPACE"
    echo "Service: $SERVICE_NAME"
    echo "Image: $DOCKER_IMAGE"
    echo ""
    
    # Check prerequisites
    check_kubectl
    check_namespace
    
    # Deploy configuration
    deploy_config
    
    # Deploy application
    deploy_application
    
    # Check health
    if check_health; then
        echo -e "${GREEN}[SUCCESS]${NC} Deployment completed successfully!"
        show_status
    else
        echo -e "${RED}[ERROR]${NC} Deployment completed but health check failed"
        show_status
        exit 1
    fi
}

# Run main function
main "$@"
