# Deploy To AWS ECS

This project can be deployed to AWS ECS Fargate with ECR for the container image and RDS PostgreSQL for the database.

## Architecture

- Spring Boot app runs as one ECS Fargate service
- Docker image is stored in ECR
- PostgreSQL runs in RDS
- Application Load Balancer exposes the API
- CloudWatch Logs stores container logs

## Prerequisites

- Docker Desktop running
- `awscli` installed
- AWS credentials configured
- An AWS region selected, for example `us-east-2`
- A VPC with at least two public subnets for ALB and private subnets for ECS/RDS if you want a production-like layout

## 1. Build The Jar

```bash
./mvnw clean package
```

## 2. Build The Docker Image

```bash
docker build -t student-management-system:latest .
```

## 3. Create ECR Repository

```bash
aws ecr create-repository --repository-name student-management-system --region us-east-2
```

## 4. Authenticate Docker To ECR

```bash
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION=us-east-2

aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin \
    "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"
```

## 5. Tag And Push The Image

```bash
docker tag student-management-system:latest \
  "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/student-management-system:latest"

docker push \
  "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/student-management-system:latest"
```

## 6. Create PostgreSQL In RDS

Suggested values:

- engine: PostgreSQL
- DB name: `student_management_system`
- security group: allow port `5432` from the ECS service security group

Keep these values for ECS environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 7. Create ECS Cluster

```bash
aws ecs create-cluster \
  --cluster-name student-management-cluster \
  --region "$AWS_REGION"
```

## 8. Create CloudWatch Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/student-management-system \
  --region "$AWS_REGION"
```

## 9. Create Task Definition

Edit [ecs/task-definition.template.json](/Users/lopsun/Documents/New project 4/ecs/task-definition.template.json) and replace:

- `<ACCOUNT_ID>`
- `<REGION>`
- `<RDS_ENDPOINT>`
- `<DB_USERNAME>`
- `<DB_PASSWORD>`

Then register it:

```bash
aws ecs register-task-definition \
  --cli-input-json file://ecs/task-definition.template.json \
  --region "$AWS_REGION"
```

## 10. Create An ALB And Target Group

You can do this in the AWS console faster for homework/demo purposes, or with CLI.

Needed resources:

- one Application Load Balancer
- one listener on port `80`
- one target group pointing to ECS on port `8080`

## 11. Create ECS Service

```bash
aws ecs create-service \
  --cluster student-management-cluster \
  --service-name student-management-service \
  --task-definition student-management-system \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-aaa,subnet-bbb],securityGroups=[sg-ecs],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=<TARGET_GROUP_ARN>,containerName=student-management-system,containerPort=8080" \
  --region "$AWS_REGION"
```

## 12. Verify

Once the service is healthy behind the ALB, test:

```bash
curl http://<ALB_DNS_NAME>/actuator/health
curl http://<ALB_DNS_NAME>/api/v1/students
```

## Recommended Homework Answer

If asked how you deployed it, a strong short answer is:

1. Containerized the Spring Boot app with Docker
2. Pushed the image to ECR
3. Used RDS PostgreSQL instead of a local database
4. Created an ECS Fargate task and service
5. Exposed it through an Application Load Balancer
6. Passed database settings through ECS environment variables
7. Verified with `/actuator/health` and CRUD endpoints
