# Product Order Service - Deployment Guide

This document provides comprehensive instructions for deploying the Product Order Service across different environments (Development, UAT, and Production) using Jenkins and Kubernetes.

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Environment Configurations](#environment-configurations)
4. [Jenkins Pipeline](#jenkins-pipeline)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Deployment Scripts](#deployment-scripts)
7. [Monitoring and Health Checks](#monitoring-and-health-checks)
8. [Troubleshooting](#troubleshooting)

## Overview

The Product Order Service is designed to be deployed across three environments:

- **Development (dev)**: Local development with H2 database and mock services
- **UAT (uat)**: User Acceptance Testing with MySQL database and external services
- **Production (prod)**: Production environment with high availability and monitoring

## Prerequisites

### Required Tools

- **Jenkins**: For CI/CD pipeline
- **Kubernetes**: For container orchestration
- **Docker**: For containerization
- **kubectl**: For Kubernetes management
- **Maven**: For building the application
- **Java 17**: For running the application

### Infrastructure Requirements

#### Development Environment
- Kubernetes cluster (minikube, kind, or cloud provider)
- H2 database (embedded)
- Kafka cluster (optional, can use embedded)

#### UAT Environment
- Kubernetes cluster
- MySQL database
- Kafka cluster
- AWS S3 bucket
- External services (Payment, Notification, Inventory)

#### Production Environment
- High-availability Kubernetes cluster
- MySQL database cluster
- Kafka cluster
- AWS S3 bucket
- External services
- Monitoring and logging infrastructure

## Environment Configurations

### Development Environment (`application-dev.yml`)

```yaml
# Key features:
- H2 in-memory database
- Mock services enabled
- Debug logging enabled
- H2 console accessible
- Local Kafka configuration
```

### UAT Environment (`application-uat.yml`)

```yaml
# Key features:
- MySQL database
- External service integration
- Performance optimizations
- Monitoring enabled
- AWS S3 integration
```

### Production Environment (`application-prod.yml`)

```yaml
# Key features:
- High-performance MySQL configuration
- Advanced caching
- Comprehensive monitoring
- Security hardening
- Auto-scaling configuration
```

## Jenkins Pipeline

### Pipeline Configuration

The `Jenkinsfile` provides a complete CI/CD pipeline with the following stages:

1. **Checkout**: Get source code
2. **Build**: Compile the application
3. **Test**: Run unit and integration tests
4. **Package**: Create JAR file
5. **Docker Build**: Build Docker image
6. **Docker Push**: Push to registry
7. **Deploy**: Deploy to target environment
8. **Health Check**: Verify deployment

### Pipeline Parameters

- `ENVIRONMENT`: Target environment (dev, uat, prod)
- `RUN_TESTS`: Whether to run tests
- `SKIP_TESTS`: Whether to skip tests

### Usage

```bash
# Trigger pipeline with parameters
curl -X POST "http://jenkins-url/job/product-order-service/buildWithParameters" \
  -d "ENVIRONMENT=dev&RUN_TESTS=true"
```

## Kubernetes Deployment

### Directory Structure

```
k8s/
├── dev/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── deployment.yaml
├── uat/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── deployment.yaml
└── prod/
    ├── namespace.yaml
    ├── configmap.yaml
    ├── secret.yaml
    └── deployment.yaml
```

### Key Components

#### Namespace
- Isolates resources per environment
- Environment-specific labels

#### ConfigMap
- Application configuration
- Environment-specific settings
- Non-sensitive configuration

#### Secret
- Sensitive data (passwords, keys)
- Base64 encoded values
- Environment-specific secrets

#### Deployment
- Application deployment
- Resource limits and requests
- Health checks
- Environment-specific configurations

#### Service
- Internal service discovery
- Load balancing
- ClusterIP type

#### Ingress
- External access
- SSL termination
- Rate limiting
- Environment-specific domains

### Production-Specific Features

#### HorizontalPodAutoscaler (HPA)
- Automatic scaling based on CPU and memory
- Min replicas: 5, Max replicas: 20
- Target CPU: 70%, Target memory: 80%

#### PodDisruptionBudget (PDB)
- Ensures minimum availability during updates
- Min available: 3 pods

## Deployment Scripts

### Development Deployment (`deploy-dev.sh`)

```bash
# Deploy to development
./deploy-dev.sh

# With custom image
DOCKER_IMAGE=my-registry.com/product-order-service:1.0.0 ./deploy-dev.sh
```

### UAT Deployment (`deploy-uat.sh`)

```bash
# Deploy to UAT
./deploy-uat.sh

# With custom image
DOCKER_IMAGE=my-registry.com/product-order-service:1.0.0 ./deploy-uat.sh
```

### Production Deployment (`deploy-prod.sh`)

```bash
# Deploy to production (requires confirmation)
./deploy-prod.sh

# With custom image
DOCKER_IMAGE=my-registry.com/product-order-service:1.0.0 ./deploy-prod.sh
```

## Monitoring and Health Checks

### Health Endpoints

- **Health Check**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### Monitoring Features

#### Development
- Basic health checks
- Debug logging
- H2 console access

#### UAT
- Comprehensive health checks
- Performance metrics
- Smoke tests
- Log aggregation

#### Production
- Advanced health checks
- Comprehensive monitoring
- Auto-scaling
- Circuit breakers
- Performance optimization

### Logging

#### Development
- Console logging
- Debug level
- H2 console access

#### UAT
- File logging
- Info level
- Log rotation

#### Production
- Structured logging
- Error level
- Log aggregation
- Performance monitoring

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues

```bash
# Check database connectivity
kubectl exec -n <namespace> deployment/product-order-service -- \
  curl -f http://localhost:8080/actuator/health

# Check database logs
kubectl logs -n <namespace> deployment/product-order-service
```

#### 2. Kafka Connection Issues

```bash
# Check Kafka connectivity
kubectl exec -n <namespace> deployment/product-order-service -- \
  curl -f http://localhost:8080/actuator/health

# Check Kafka logs
kubectl logs -n <namespace> deployment/product-order-service | grep kafka
```

#### 3. Memory Issues

```bash
# Check memory usage
kubectl top pods -n <namespace> -l app=product-order-service

# Check memory limits
kubectl describe pod -n <namespace> <pod-name>
```

#### 4. Deployment Issues

```bash
# Check deployment status
kubectl get deployments -n <namespace>

# Check pod status
kubectl get pods -n <namespace> -l app=product-order-service

# Check events
kubectl get events -n <namespace> --sort-by='.lastTimestamp'
```

### Debug Commands

#### Check Application Logs

```bash
# Get logs from all pods
kubectl logs -n <namespace> -l app=product-order-service

# Get logs from specific pod
kubectl logs -n <namespace> <pod-name>

# Follow logs
kubectl logs -n <namespace> -f <pod-name>
```

#### Check Configuration

```bash
# Check ConfigMap
kubectl get configmap -n <namespace> product-order-service-config -o yaml

# Check Secret
kubectl get secret -n <namespace> product-order-service-secret -o yaml
```

#### Check Service Status

```bash
# Check service
kubectl get service -n <namespace> product-order-service

# Check ingress
kubectl get ingress -n <namespace> product-order-service-ingress

# Check endpoints
kubectl get endpoints -n <namespace> product-order-service
```

### Performance Optimization

#### Development
- Use H2 database for fast startup
- Enable debug logging for troubleshooting
- Use mock services for external dependencies

#### UAT
- Use MySQL for realistic testing
- Enable performance monitoring
- Use external services for integration testing

#### Production
- Use connection pooling
- Enable caching
- Use auto-scaling
- Monitor performance metrics

## Security Considerations

### Development
- Basic security configuration
- Mock services for external dependencies
- Debug logging enabled

### UAT
- Production-like security configuration
- External service integration
- Performance testing

### Production
- Comprehensive security configuration
- SSL/TLS termination
- Rate limiting
- Security scanning
- Compliance monitoring

## Backup and Recovery

### Database Backup

```bash
# MySQL backup
mysqldump -h <host> -u <user> -p <database> > backup.sql

# Restore
mysql -h <host> -u <user> -p <database> < backup.sql
```

### Configuration Backup

```bash
# Backup ConfigMaps
kubectl get configmap -n <namespace> -o yaml > configmap-backup.yaml

# Backup Secrets
kubectl get secret -n <namespace> -o yaml > secret-backup.yaml
```

## Conclusion

This deployment guide provides comprehensive instructions for deploying the Product Order Service across different environments. The configuration is designed to be scalable, maintainable, and production-ready.

For additional support or questions, please refer to the application logs and Kubernetes documentation.
