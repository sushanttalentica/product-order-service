# AWS EC2 Deployment Guide

## Quick Start

### 1. Prerequisites

```bash
# Install Terraform
brew install terraform

# Verify AWS CLI
aws configure list

# Generate SSH key if needed
ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa
```

### 2. Configure Terraform

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:
- SSH public key (from `~/.ssh/id_rsa.pub`)
- S3 bucket name
- JWT secret

### 3. Deploy

```bash
# Automated deployment
./deploy-to-aws.sh
```

Or manual steps:

```bash
# Build application
mvn clean package -DskipTests

# Deploy infrastructure
cd terraform
terraform init
terraform apply

# Get EC2 IP
export EC2_IP=$(terraform output -raw ec2_public_ip)

# Copy files
scp -i ~/.ssh/id_rsa target/*.jar ubuntu@$EC2_IP:/opt/product-order-service/
scp -i ~/.ssh/id_rsa Dockerfile ubuntu@$EC2_IP:/opt/product-order-service/

# Deploy
ssh -i ~/.ssh/id_rsa ubuntu@$EC2_IP "cd /opt/product-order-service && sudo ./deploy.sh"
```

## Infrastructure

### Components

- **VPC**: 10.0.0.0/16
- **Subnets**: 2 public subnets (ap-south-1a, ap-south-1b)
- **EC2**: t3.medium with Java 21
- **Redis**: ElastiCache cache.t3.micro
- **Storage**: 30GB gp3 volume
- **IP**: Elastic IP (static)

### Security

- App port 8080: Open to internet
- gRPC port 9090: Open to internet
- SSH port 22: Open (restrict to your IP in production)
- Redis port 6379: Private (app only)

## Application Access

After deployment:

```bash
# Get outputs
cd terraform
terraform output
```

### URLs

- **Application**: `http://<EC2_IP>:8080/product-order-service`
- **Health**: `http://<EC2_IP>:8080/product-order-service/actuator/health`
- **Swagger**: `http://<EC2_IP>:8080/product-order-service/swagger-ui.html`
- **H2 Console**: `http://<EC2_IP>:8080/product-order-service/h2-console`

### SSH Access

```bash
ssh -i ~/.ssh/id_rsa ubuntu@<EC2_IP>
```

## Application Management

### View Logs

```bash
ssh ubuntu@<EC2_IP>
docker logs -f product-order-service
```

### Restart Application

```bash
ssh ubuntu@<EC2_IP>
cd /opt/product-order-service
sudo docker-compose restart
```

### Update Application

```bash
# Build new version
mvn clean package -DskipTests

# Deploy
scp -i ~/.ssh/id_rsa target/*.jar ubuntu@<EC2_IP>:/opt/product-order-service/
ssh ubuntu@<EC2_IP> "cd /opt/product-order-service && sudo ./deploy.sh"
```

## Monitoring

### Check Status

```bash
curl http://<EC2_IP>:8080/product-order-service/actuator/health
```

### View Metrics

```bash
curl http://<EC2_IP>:8080/product-order-service/actuator/metrics
```

## Cost

Monthly estimate:
- EC2 t3.medium: ~$30
- ElastiCache t3.micro: ~$12
- EIP: ~$3
- Data transfer: Variable
- **Total: ~$45-50/month**

## Cleanup

```bash
cd terraform
terraform destroy
```

## Troubleshooting

### Application won't start

```bash
ssh ubuntu@<EC2_IP>
docker logs product-order-service
```

### Can't connect

Check security group allows port 8080 from your IP.

### Redis connection fails

Verify Redis endpoint in environment variables.

