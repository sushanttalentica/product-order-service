# Terraform AWS Infrastructure

## Overview

Terraform scripts to deploy Product Order Service on AWS EC2 with ElastiCache Redis.

## Prerequisites

- Terraform >= 1.0
- AWS CLI configured
- SSH key pair generated
- S3 bucket created

## Infrastructure Components

- **VPC** with public subnets
- **EC2 instance** (t3.medium) with Java 21
- **ElastiCache Redis** (cache.t3.micro)
- **Security Groups** for app and Redis
- **Elastic IP** for static public IP
- **IAM Role** for S3 access

## Setup

### 1. Configure Variables

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:
- Add your SSH public key
- Set S3 bucket name
- Set JWT secret
- Adjust instance types if needed

### 2. Initialize Terraform

```bash
cd terraform
terraform init
```

### 3. Plan Deployment

```bash
terraform plan
```

### 4. Deploy Infrastructure

```bash
terraform apply
```

Type `yes` when prompted.

## Post-Deployment

### Get Output Values

```bash
terraform output
```

You'll get:
- EC2 Public IP
- Redis Endpoint
- Application URL
- SSH Command

### Deploy Application

1. SSH to EC2:
```bash
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_IP>
```

2. Copy application JAR:
```bash
scp -i ~/.ssh/id_rsa target/product-order-service-1.0.0.jar ubuntu@<EC2_IP>:/opt/product-order-service/
```

3. Copy Dockerfile:
```bash
scp -i ~/.ssh/id_rsa Dockerfile ubuntu@<EC2_IP>:/opt/product-order-service/
```

4. Run deployment:
```bash
ssh ubuntu@<EC2_IP>
cd /opt/product-order-service
sudo ./deploy.sh
```

### Access Application

```
http://<EC2_IP>:8080/product-order-service
```

Health check:
```
http://<EC2_IP>:8080/product-order-service/actuator/health
```

Swagger UI:
```
http://<EC2_IP>:8080/product-order-service/swagger-ui.html
```

## Cleanup

To destroy all resources:

```bash
terraform destroy
```

## Cost Estimate

- EC2 t3.medium: ~$30/month
- ElastiCache t3.micro: ~$12/month
- Data transfer: Variable
- Total: ~$45-50/month

## Security Notes

- SSH access: Restricted by security group
- S3 access: IAM role-based
- Redis: Private, only accessible from app
- Secrets: Stored in terraform.tfvars (gitignored)

