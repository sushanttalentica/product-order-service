#!/bin/bash
set -e

echo "Starting EC2 setup (Free Tier)..."

apt-get update
apt-get install -y apt-transport-https ca-certificates curl software-properties-common

echo "Installing Java 21..."
wget -O- https://apt.corretto.aws/corretto.key | apt-key add -
add-apt-repository 'deb https://apt.corretto.aws stable main'
apt-get update
apt-get install -y java-21-amazon-corretto-jdk

echo "Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
usermod -aG docker ubuntu
systemctl enable docker
systemctl start docker

echo "Installing Docker Compose..."
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

echo "Installing Redis locally (Free Tier)..."
apt-get install -y redis-server
systemctl enable redis-server
systemctl start redis-server

echo "Creating application directory..."
mkdir -p /opt/product-order-service
cd /opt/product-order-service

echo "Creating docker-compose.yml..."
cat > /opt/product-order-service/docker-compose.yml << 'DOCKEREOF'
version: '3.8'

services:
  app:
    image: product-order-service:latest
    container_name: product-order-service
    network_mode: host
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATA_REDIS_HOST=localhost
      - SPRING_DATA_REDIS_PORT=6379
      - AWS_S3_BUCKET_NAME=${s3_bucket_name}
      - AWS_REGION=${aws_region}
      - JWT_SECRET=${jwt_secret}
    restart: unless-stopped
DOCKEREOF

echo "Creating deployment script..."
cat > /opt/product-order-service/deploy.sh << 'DEPLOYEOF'
#!/bin/bash
set -e

echo "Deploying application..."

cd /opt/product-order-service

if docker ps | grep -q product-order-service; then
    echo "Stopping existing service..."
    docker-compose down || true
fi

if [ -f "product-order-service-1.0.0.jar" ]; then
    echo "Building Docker image..."
    docker build -t product-order-service:latest .
    
    echo "Starting service..."
    docker-compose up -d
    
    echo "Waiting for startup..."
    sleep 30
    
    echo "Health check..."
    curl -f http://localhost:8080/product-order-service/actuator/health || echo "Health check failed"
    
    echo "Deployment complete!"
else
    echo "JAR file not found. Please upload product-order-service-1.0.0.jar first."
fi
DEPLOYEOF

chmod +x /opt/product-order-service/deploy.sh

echo "Creating systemd service..."
cat > /etc/systemd/system/product-order-service.service << 'SERVICEEOF'
[Unit]
Description=Product Order Service
After=docker.service redis-server.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/product-order-service
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
User=root

[Install]
WantedBy=multi-user.target
SERVICEEOF

systemctl daemon-reload
systemctl enable product-order-service

echo "Setup complete!"

