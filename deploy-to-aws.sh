#!/bin/bash
set -e

echo "=========================================="
echo "Product Order Service - AWS Deployment"
echo "=========================================="
echo ""

if [ ! -f "terraform/terraform.tfvars" ]; then
    echo "❌ Error: terraform/terraform.tfvars not found"
    echo "Copy terraform.tfvars.example and configure it first"
    exit 1
fi

echo "Building application..."
export JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "/usr/lib/jvm/java-21")
mvn clean package -DskipTests

if [ ! -f "target/product-order-service-1.0.0.jar" ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"
echo ""

echo "Deploying infrastructure with Terraform..."
cd terraform
terraform init
terraform plan
echo ""
read -p "Apply Terraform changes? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Deployment cancelled"
    exit 0
fi

terraform apply -auto-approve

echo ""
echo "Getting outputs..."
EC2_IP=$(terraform output -raw ec2_public_ip)
echo "EC2 IP: $EC2_IP"

cd ..

echo ""
echo "Waiting for EC2 to be ready (60 seconds)..."
sleep 60

echo ""
echo "Copying files to EC2..."
scp -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa target/product-order-service-1.0.0.jar ubuntu@$EC2_IP:/opt/product-order-service/
scp -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa Dockerfile ubuntu@$EC2_IP:/opt/product-order-service/

echo ""
echo "Deploying application..."
ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa ubuntu@$EC2_IP "cd /opt/product-order-service && sudo ./deploy.sh"

echo ""
echo "=========================================="
echo "✅ Deployment Complete!"
echo "=========================================="
echo ""
echo "Application URL: http://$EC2_IP:8080/product-order-service"
echo "Health Check: http://$EC2_IP:8080/product-order-service/actuator/health"
echo "Swagger UI: http://$EC2_IP:8080/product-order-service/swagger-ui.html"
echo ""
echo "SSH Access: ssh -i ~/.ssh/id_rsa ubuntu@$EC2_IP"
echo ""

