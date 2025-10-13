variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "product-order-service"
}

variable "ec2_instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t2.micro"
}

variable "ec2_ami" {
  description = "EC2 AMI ID (Ubuntu 22.04 LTS)"
  type        = string
  default     = "ami-0f58b397bc5c1f2e8"
}

variable "redis_node_type" {
  description = "ElastiCache Redis node type"
  type        = string
  default     = "cache.t2.micro"
}

variable "s3_bucket_name" {
  description = "S3 bucket name for invoices"
  type        = string
}

variable "ssh_public_key" {
  description = "SSH public key for EC2 access"
  type        = string
}

variable "jwt_secret" {
  description = "JWT secret key"
  type        = string
  sensitive   = true
}
