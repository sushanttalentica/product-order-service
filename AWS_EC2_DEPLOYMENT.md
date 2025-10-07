# AWS EC2 Deployment Guide

## Quick Deployment to AWS EC2

This guide shows how to deploy the Product Order Service to AWS EC2 using the included Terraform configuration.

---

## Prerequisites

- AWS Account with appropriate permissions
- AWS CLI configured (`aws configure`)
- Terraform installed (`brew install terraform`)
- SSH key pair created in AWS

---

## Step 1: Configure AWS Credentials

```bash
# Set AWS credentials
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_DEFAULT_REGION="ap-south-1"

# Verify configuration
aws sts get-caller-identity
```

---

## Step 2: Deploy Infrastructure with Terraform

```bash
# Navigate to terraform directory
cd terraform

# Initialize Terraform
terraform init

# Review the deployment plan
terraform plan

# Apply the configuration
terraform apply -auto-approve
```

**Terraform will create:**
- ✅ EC2 instances with auto-scaling (2-10 instances)
- ✅ Application Load Balancer (ALB)
- ✅ RDS PostgreSQL database
- ✅ ElastiCache Redis cluster
- ✅ S3 bucket for invoices
- ✅ VPC with public/private subnets
- ✅ Security groups and IAM roles

---

## Step 3: Get Deployment Outputs

```bash
# Get application URL
terraform output application_url

# Get database endpoint
terraform output database_endpoint

# Get all outputs
terraform output
```

Example output:
```
application_url = "http://my-app-lb-123456.ap-south-1.elb.amazonaws.com"
database_endpoint = "mydb.abc123.ap-south-1.rds.amazonaws.com:5432"
redis_endpoint = "myredis.abc123.cache.amazonaws.com:6379"
```

---

## Step 4: Verify Deployment

```bash
# Get ALB URL
ALB_URL=$(terraform output -raw application_url)

# Check health
curl $ALB_URL/product-order-service/actuator/health

# Access Swagger UI
open $ALB_URL/product-order-service/swagger-ui.html
```

---

## Step 5: Configure Environment Variables

The application automatically uses these from Terraform:

```bash
# Set on EC2 via user_data.sh (already configured)
S3_BUCKET=<from-terraform-output>
DB_HOST=<rds-endpoint>
REDIS_HOST=<elasticache-endpoint>
KAFKA_BOOTSTRAP_SERVERS=<kafka-endpoint>
```

---

## Alternative: Manual EC2 Deployment (Without Terraform)

### **1. Launch EC2 Instance:**

```bash
# Launch EC2 instance (Ubuntu 22.04)
aws ec2 run-instances \
  --image-id ami-0c55b159cbfafe1f0 \
  --instance-type t3.medium \
  --key-name your-key-pair \
  --security-groups your-security-group \
  --user-data file://terraform/user_data.sh
```

### **2. SSH to Instance:**

```bash
ssh -i your-key.pem ubuntu@<ec2-public-ip>
```

### **3. Deploy Application:**

```bash
# On EC2 instance

# Clone repository
git clone https://github.com/sushanttalentica/product-order-service.git
cd product-order-service

# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk -y

# Build application
./mvnw clean package -DskipTests

# Set environment variables
export S3_BUCKET=my-pos-bucket-125
export S3_REGION=ap-south-1
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret

# Run application
nohup java -jar target/product-order-service-1.0.0.jar > app.log 2>&1 &
```

### **4. Setup as Systemd Service (Production):**

```bash
# Create service file
sudo nano /etc/systemd/system/product-order-service.service
```

```ini
[Unit]
Description=Product Order Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/product-order-service
ExecStart=/usr/bin/java -jar /home/ubuntu/product-order-service/target/product-order-service-1.0.0.jar
Restart=always
RestartSec=10

Environment="S3_BUCKET=my-pos-bucket-125"
Environment="S3_REGION=ap-south-1"
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable product-order-service
sudo systemctl start product-order-service

# Check status
sudo systemctl status product-order-service

# View logs
sudo journalctl -u product-order-service -f
```

---

## Step 6: Configure Load Balancer (Optional)

If deploying multiple EC2 instances manually:

```bash
# Install Nginx on a separate instance
sudo apt install nginx -y

# Copy nginx configuration
sudo cp config/nginx.conf /etc/nginx/sites-available/product-order-service

# Update upstream servers in nginx.conf
upstream product_order_service {
    server ec2-instance-1:8080;
    server ec2-instance-2:8080;
    server ec2-instance-3:8080;
}

# Enable site
sudo ln -s /etc/nginx/sites-available/product-order-service /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## Monitoring & Maintenance

### **Check Application Logs:**
```bash
# If using systemd
sudo journalctl -u product-order-service -f

# If running manually
tail -f app.log
```

### **Check Application Health:**
```bash
curl http://localhost:8080/product-order-service/actuator/health
```

### **View Metrics:**
```bash
curl http://localhost:8080/product-order-service/actuator/prometheus
```

### **Update Application:**
```bash
# Pull latest code
git pull origin main

# Rebuild
./mvnw clean package -DskipTests

# Restart service
sudo systemctl restart product-order-service
```

---

## Scaling Options

### **Vertical Scaling (Single Instance):**
```bash
# Change instance type
aws ec2 modify-instance-attribute \
  --instance-id i-1234567890abcdef0 \
  --instance-type t3.xlarge
```

### **Horizontal Scaling (Multiple Instances):**
```bash
# Use Auto Scaling Group (already in Terraform)
# Or manually launch more instances

# Update Terraform variables
terraform apply -var="desired_capacity=10"
```

---

## Kubernetes on EC2 (EKS)

If using Kubernetes:

```bash
# Create EKS cluster
eksctl create cluster \
  --name product-order-cluster \
  --region ap-south-1 \
  --nodegroup-name standard-workers \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 10

# Deploy application
kubectl apply -f k8s/prod/

# Check deployment
kubectl get pods
kubectl get services
```

---

## Cost Estimation (AWS)

| **Resource** | **Type** | **Monthly Cost (approx)** |
|--------------|----------|---------------------------|
| EC2 (t3.medium × 2) | Compute | $60 |
| RDS (db.t3.small) | Database | $30 |
| ElastiCache (cache.t3.micro) | Cache | $15 |
| ALB | Load Balancer | $20 |
| S3 | Storage | $5 |
| Data Transfer | Network | $10 |
| **Total** | | **~$140/month** |

---

## Troubleshooting

### **Application Won't Start:**
```bash
# Check Java version
java -version  # Should be 17+

# Check port availability
sudo netstat -tulpn | grep 8080

# Check environment variables
printenv | grep -E "S3_|DB_|REDIS"
```

### **Database Connection Issues:**
```bash
# Test database connectivity
nc -zv <rds-endpoint> 5432

# Check security groups
aws ec2 describe-security-groups --group-ids <sg-id>
```

### **S3 Upload Failures:**
```bash
# Check IAM permissions
aws s3 ls s3://my-pos-bucket-125/

# Test S3 access
aws s3 cp test.txt s3://my-pos-bucket-125/test.txt
```

---

## Quick Deployment Commands

```bash
# Full automated deployment
cd terraform
terraform apply -auto-approve

# Get URL and test
APP_URL=$(terraform output -raw application_url)
curl $APP_URL/product-order-service/actuator/health

# Done! 🚀
```

---

**With Terraform, deployment to AWS EC2 takes just 5-10 minutes!**

