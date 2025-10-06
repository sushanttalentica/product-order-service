#!/bin/bash

# Production Environment Deployment Script
# This script deploys the Product Order Service to the production environment

set -e

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}[INFO]${NC} Starting deployment to Production environment..."

# Configuration
NAMESPACE="prod"
SERVICE_NAME="product-order-service"
DOCKER_IMAGE="${DOCKER_REGISTRY:-your-docker-registry.com}/${SERVICE_NAME}:${BUILD_TAG:-latest-prod}"

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
        kubectl apply -f k8s/prod/namespace.yaml
    fi
}

# Function to deploy configuration
deploy_config() {
    echo -e "${BLUE}[INFO]${NC} Deploying configuration..."
    kubectl apply -f k8s/prod/configmap.yaml
    kubectl apply -f k8s/prod/secret.yaml
    echo -e "${GREEN}[SUCCESS]${NC} Configuration deployed"
}

# Function to deploy application with blue-green strategy
deploy_application() {
    echo -e "${BLUE}[INFO]${NC} Deploying application with blue-green strategy..."
    
    # Get current deployment status
    CURRENT_REPLICAS=$(kubectl get deployment $SERVICE_NAME -n $NAMESPACE -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "0")
    
    if [ "$CURRENT_REPLICAS" -eq 0 ]; then
        echo -e "${BLUE}[INFO]${NC} First deployment, creating deployment..."
        kubectl apply -f k8s/prod/deployment.yaml
    else
        echo -e "${BLUE}[INFO]${NC} Updating existing deployment..."
        kubectl set image deployment/$SERVICE_NAME $SERVICE_NAME=$DOCKER_IMAGE -n $NAMESPACE
    fi
    
    # Wait for rollout to complete
    kubectl rollout status deployment/$SERVICE_NAME -n $NAMESPACE --timeout=900s
    
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
    sleep 120
    
    # Health check
    for i in {1..20}; do
        if kubectl exec -n $NAMESPACE deployment/$SERVICE_NAME -- curl -f http://localhost:8080/actuator/health &> /dev/null; then
            echo -e "${GREEN}[SUCCESS]${NC} Health check passed"
            return 0
        else
            echo -e "${YELLOW}[WARNING]${NC} Health check failed, retrying in 20 seconds... (attempt $i/20)"
            sleep 20
        fi
    done
    
    echo -e "${RED}[ERROR]${NC} Health check failed after 20 attempts"
    return 1
}

# Function to run comprehensive tests
run_tests() {
    echo -e "${BLUE}[INFO]${NC} Running comprehensive tests..."
    
    # Get a pod name
    POD_NAME=$(kubectl get pods -n $NAMESPACE -l app=$SERVICE_NAME -o jsonpath='{.items[0].metadata.name}')
    
    if [ -z "$POD_NAME" ]; then
        echo -e "${RED}[ERROR]${NC} Could not find pod for tests"
        return 1
    fi
    
    # Run health tests
    echo -e "${BLUE}[INFO]${NC} Testing health endpoint..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/actuator/health
    
    echo -e "${BLUE}[INFO]${NC} Testing info endpoint..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/actuator/info
    
    echo -e "${BLUE}[INFO]${NC} Testing metrics endpoint..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/actuator/metrics
    
    echo -e "${BLUE}[INFO]${NC} Testing prometheus endpoint..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/actuator/prometheus
    
    # Test application endpoints
    echo -e "${BLUE}[INFO]${NC} Testing application endpoints..."
    kubectl exec -n $NAMESPACE $POD_NAME -- curl -f http://localhost:8080/api/v1/products
    
    echo -e "${GREEN}[SUCCESS]${NC} All tests passed"
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
    
    echo ""
    
    # Show HPA
    echo -e "${YELLOW}HorizontalPodAutoscaler:${NC}"
    kubectl get hpa -n $NAMESPACE -l app=$SERVICE_NAME
    
    echo ""
    
    # Show PDB
    echo -e "${YELLOW}PodDisruptionBudget:${NC}"
    kubectl get pdb -n $NAMESPACE -l app=$SERVICE_NAME
}

# Function to show monitoring information
show_monitoring() {
    echo -e "${BLUE}[INFO]${NC} Monitoring Information:"
    echo "========================"
    
    # Show resource usage
    echo -e "${YELLOW}Resource Usage:${NC}"
    kubectl top pods -n $NAMESPACE -l app=$SERVICE_NAME
    
    echo ""
    
    # Show events
    echo -e "${YELLOW}Recent Events:${NC}"
    kubectl get events -n $NAMESPACE --sort-by='.lastTimestamp' | tail -10
}

# Main deployment process
main() {
    echo -e "${BLUE}[INFO]${NC} Deploying to Production Environment"
    echo "=============================================="
    echo "Namespace: $NAMESPACE"
    echo "Service: $SERVICE_NAME"
    echo "Image: $DOCKER_IMAGE"
    echo ""
    
    # Production deployment confirmation
    echo -e "${RED}[WARNING]${NC} This will deploy to PRODUCTION environment!"
    echo -e "${YELLOW}[INFO]${NC} Please confirm you want to proceed (y/N):"
    read -r confirmation
    
    if [[ ! $confirmation =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}[INFO]${NC} Deployment cancelled by user"
        exit 0
    fi
    
    # Check prerequisites
    check_kubectl
    check_namespace
    
    # Deploy configuration
    deploy_config
    
    # Deploy application
    deploy_application
    
    # Check health
    if check_health; then
        # Run tests
        run_tests
        
        echo -e "${GREEN}[SUCCESS]${NC} Production deployment completed successfully!"
        show_status
        show_monitoring
    else
        echo -e "${RED}[ERROR]${NC} Production deployment completed but health check failed"
        show_status
        show_monitoring
        exit 1
    fi
}

# Run main function
main "$@"
