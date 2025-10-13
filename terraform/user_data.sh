#!/bin/bash

# User data script for EC2 instance
# 
set -e

# Variables
APP_NAME="product-order-service"
APP_USER="appuser"
APP_HOME="/opt/$APP_NAME"
LOG_FILE="/var/log/user-data.log"

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a $LOG_FILE
}

log "Starting user data script for $APP_NAME"

# Update system
log "Updating system packages"
yum update -y

# Install required packages
log "Installing required packages"
yum install -y \
    java-17-amazon-corretto \
    docker \
    git \
    htop \
    awscli \
    jq \
    curl \
    wget \
    unzip

# Start and enable Docker
log "Starting and enabling Docker"
systemctl start docker
systemctl enable docker

# Create application user
log "Creating application user"
useradd -r -s /bin/false -d $APP_HOME $APP_USER
mkdir -p $APP_HOME
chown $APP_USER:$APP_USER $APP_HOME

# Configure Docker to run as app user
usermod -aG docker $APP_USER

# Create application directory
log "Creating application directory"
mkdir -p $APP_HOME/{config,logs,data}
chown -R $APP_USER:$APP_USER $APP_HOME

# Download and install application
log "Downloading application"
cd $APP_HOME

# Clone repository (in production, use proper artifact management)
git clone https://github.com/your-org/product-order-service.git .

# Build application
log "Building application"
chown -R $APP_USER:$APP_USER $APP_HOME
sudo -u $APP_USER ./mvnw clean package -DskipTests

# Create application configuration
log "Creating application configuration"
cat > $APP_HOME/config/application-prod.yml << EOF
server:
  port: 8080
  servlet:
    context-path: /product-order-service

spring:
  application:
    name: product-order-service
  
  datasource:
    url: jdbc:mysql://${db_host}:3306/${db_name}
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${db_username}
    password: ${db_password}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  cache:
    type: redis
    redis:
      host: ${redis_host}
      port: 6379
  
  kafka:
    bootstrap-servers: ${kafka_hosts}
    consumer:
      group-id: product-order-service-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# Security Configuration
security:
  jwt:
    secret: \${JWT_SECRET:mySecretKey}
    expiration: 86400000

# AWS Configuration
aws:
  s3:
    bucket-name: \${S3_BUCKET_NAME:product-order-invoices}
    region: \${AWS_REGION:us-east-1}
    access-key: \${AWS_ACCESS_KEY_ID}
    secret-key: \${AWS_SECRET_ACCESS_KEY}

# Management and Monitoring
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# Logging Configuration
logging:
  level:
    com.ecommerce.productorder: INFO
    org.springframework.security: WARN
    org.springframework.kafka: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: $APP_HOME/logs/application.log
EOF

# Create systemd service
log "Creating systemd service"
cat > /etc/systemd/system/$APP_NAME.service << EOF
[Unit]
Description=Product Order Service
After=network.target

[Service]
Type=simple
User=$APP_USER
Group=$APP_USER
WorkingDirectory=$APP_HOME
ExecStart=/usr/bin/java -jar $APP_HOME/target/*.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=$APP_NAME

# Environment variables
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC

[Install]
WantedBy=multi-user.target
EOF

# Set permissions
chown -R $APP_USER:$APP_USER $APP_HOME
chmod +x $APP_HOME/target/*.jar

# Enable and start service
log "Enabling and starting service"
systemctl daemon-reload
systemctl enable $APP_NAME
systemctl start $APP_NAME

# Wait for service to start
log "Waiting for service to start"
sleep 30

# Health check
log "Performing health check"
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health; then
        log "Service is healthy"
        break
    else
        log "Health check failed, attempt $i/10"
        sleep 10
    fi
done

# Configure log rotation
log "Configuring log rotation"
cat > /etc/logrotate.d/$APP_NAME << EOF
$APP_HOME/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 $APP_USER $APP_USER
    postrotate
        systemctl reload $APP_NAME
    endscript
}
EOF

# Install CloudWatch agent
log "Installing CloudWatch agent"
wget https://s3.amazonaws.com/amazoncloudwatch-agent/amazon_linux/amd64/latest/amazon-cloudwatch-agent.rpm
rpm -U ./amazon-cloudwatch-agent.rpm

# Configure CloudWatch agent
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << EOF
{
    "logs": {
        "logs_collected": {
            "files": {
                "collect_list": [
                    {
                        "file_path": "$APP_HOME/logs/application.log",
                        "log_group_name": "/aws/ec2/$APP_NAME",
                        "log_stream_name": "{instance_id}"
                    }
                ]
            }
        }
    },
    "metrics": {
        "namespace": "CWAgent",
        "metrics_collected": {
            "cpu": {
                "measurement": [
                    "cpu_usage_idle",
                    "cpu_usage_iowait",
                    "cpu_usage_user",
                    "cpu_usage_system"
                ],
                "metrics_collection_interval": 60
            },
            "disk": {
                "measurement": [
                    "used_percent"
                ],
                "metrics_collection_interval": 60,
                "resources": [
                    "*"
                ]
            },
            "mem": {
                "measurement": [
                    "mem_used_percent"
                ],
                "metrics_collection_interval": 60
            }
        }
    }
}
EOF

# Start CloudWatch agent
systemctl start amazon-cloudwatch-agent
systemctl enable amazon-cloudwatch-agent

log "User data script completed successfully"
