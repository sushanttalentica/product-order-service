# Terraform configuration for AWS EC2 deployment
# 
# Design Principles Applied:
# - Infrastructure as Code: Defines infrastructure using code
# - Modularity: Uses modules for reusable components
# - Security: Implements security best practices
# - Scalability: Configures for horizontal scaling
# - Monitoring: Includes monitoring and logging
# - Backup: Implements backup strategies
# - Cost Optimization: Uses cost-effective resources

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Configure AWS Provider
provider "aws" {
  region = var.aws_region
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }
}

# VPC Module
module "vpc" {
  source = "./modules/vpc"
  
  name                 = "${var.project_name}-vpc"
  cidr                 = var.vpc_cidr
  azs                  = data.aws_availability_zones.available.names
  private_subnets      = var.private_subnets
  public_subnets       = var.public_subnets
  enable_nat_gateway   = true
  enable_vpn_gateway   = false
  enable_dns_hostnames = true
  enable_dns_support   = true
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Security Groups
resource "aws_security_group" "product_order_service" {
  name_prefix = "${var.project_name}-product-order-service"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-product-order-service-sg"
    Environment = var.environment
  }
}

# RDS MySQL Database
module "rds" {
  source = "./modules/rds"
  
  identifier = "${var.project_name}-mysql"
  
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  
  vpc_security_group_ids = [aws_security_group.rds.id]
  subnet_ids             = module.vpc.private_subnets
  
  backup_retention_period = 7
  backup_window          = "03:00-04:00"
  maintenance_window     = "sun:04:00-sun:05:00"
  
  tags = {
    Environment = var.environment
    Project    = var.project_name
  }
}

# ElastiCache Redis
module "redis" {
  source = "./modules/redis"
  
  cluster_id           = "${var.project_name}-redis"
  node_type            = var.redis_node_type
  num_cache_nodes      = var.redis_num_cache_nodes
  parameter_group_name  = "default.redis7"
  
  subnet_ids = module.vpc.private_subnets
  security_group_ids = [aws_security_group.redis.id]
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# MSK Kafka Cluster
module "kafka" {
  source = "./modules/kafka"
  
  cluster_name = "${var.project_name}-kafka"
  kafka_version = "2.8.1"
  
  number_of_broker_nodes = var.kafka_broker_count
  broker_instance_type   = var.kafka_instance_type
  
  subnet_ids = module.vpc.private_subnets
  security_group_ids = [aws_security_group.kafka.id]
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Application Load Balancer
module "alb" {
  source = "./modules/alb"
  
  name               = "${var.project_name}-alb"
  load_balancer_type = "application"
  
  vpc_id          = module.vpc.vpc_id
  subnets         = module.vpc.public_subnets
  security_groups = [aws_security_group.alb.id]
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# Auto Scaling Group
module "asg" {
  source = "./modules/asg"
  
  name = "${var.project_name}-asg"
  
  vpc_id           = module.vpc.vpc_id
  subnet_ids       = module.vpc.private_subnets
  security_groups  = [aws_security_group.product_order_service.id]
  
  min_size         = var.asg_min_size
  max_size         = var.asg_max_size
  desired_capacity = var.asg_desired_capacity
  
  image_id      = data.aws_ami.amazon_linux.id
  instance_type = var.instance_type
  
  user_data = base64encode(templatefile("${path.module}/user_data.sh", {
    db_host     = module.rds.endpoint
    db_name     = var.db_name
    db_username = var.db_username
    db_password = var.db_password
    redis_host  = module.redis.endpoint
    kafka_hosts = module.kafka.bootstrap_brokers
  }))
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# S3 Bucket for Invoices
resource "aws_s3_bucket" "invoices" {
  bucket = "${var.project_name}-invoices-${random_id.bucket_suffix.hex}"
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_s3_bucket_versioning" "invoices" {
  bucket = aws_s3_bucket.invoices.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "invoices" {
  bucket = aws_s3_bucket.invoices.id
  
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets  = true
}

# CloudWatch Log Groups
resource "aws_cloudwatch_log_group" "product_order_service" {
  name              = "/aws/ec2/${var.project_name}-product-order-service"
  retention_in_days = 30
  
  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# CloudWatch Alarms
resource "aws_cloudwatch_metric_alarm" "high_cpu" {
  alarm_name          = "${var.project_name}-high-cpu"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "80"
  alarm_description   = "This metric monitors ec2 cpu utilization"
  
  dimensions = {
    AutoScalingGroupName = module.asg.name
  }
}

# Random ID for S3 bucket suffix
resource "random_id" "bucket_suffix" {
  byte_length = 4
}
