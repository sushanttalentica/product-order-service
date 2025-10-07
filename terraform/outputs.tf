# Terraform outputs for AWS EC2 deployment
# 
# Design Principles Applied:
# - Output Management: Exposes important resource information
# - Documentation: Documents each output
# - Security: Excludes sensitive information
# - Usability: Provides useful information for users

output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

output "database_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}

output "database_port" {
  description = "RDS instance port"
  value       = module.rds.port
}

output "redis_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = module.redis.endpoint
  sensitive   = true
}

output "kafka_bootstrap_brokers" {
  description = "Kafka bootstrap brokers"
  value       = module.kafka.bootstrap_brokers
  sensitive   = true
}

output "alb_dns_name" {
  description = "DNS name of the load balancer"
  value       = module.alb.dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the load balancer"
  value       = module.alb.zone_id
}

output "asg_name" {
  description = "Name of the Auto Scaling Group"
  value       = module.asg.name
}

output "asg_arn" {
  description = "ARN of the Auto Scaling Group"
  value       = module.asg.arn
}

output "s3_bucket_name" {
  description = "Name of the S3 bucket for invoices"
  value       = aws_s3_bucket.invoices.bucket
}

output "s3_bucket_arn" {
  description = "ARN of the S3 bucket for invoices"
  value       = aws_s3_bucket.invoices.arn
}

output "cloudwatch_log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = aws_cloudwatch_log_group.product_order_service.name
}

output "application_url" {
  description = "URL of the application"
  value       = "http://${module.alb.dns_name}"
}

output "monitoring_dashboard_url" {
  description = "URL of the CloudWatch dashboard"
  value       = "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards:name=${var.project_name}-dashboard"
}

output "deployment_summary" {
  description = "Summary of the deployment"
  value = {
    project_name     = var.project_name
    environment      = var.environment
    aws_region       = var.aws_region
    vpc_id          = module.vpc.vpc_id
    application_url  = "http://${module.alb.dns_name}"
    database_endpoint = module.rds.endpoint
    redis_endpoint   = module.redis.endpoint
    kafka_brokers    = module.kafka.bootstrap_brokers
    s3_bucket        = aws_s3_bucket.invoices.bucket
  }
  sensitive = true
}
