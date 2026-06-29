# Deploy on EC2 with Dockerfile

This guide uses the existing `Dockerfile` to run the Spring Boot application on one EC2 instance.

## Goal

- run the Spring Boot app on EC2 with Docker
- expose port `8080`
- call the downstream aggregation service
- use the default downstream message `My name is Steven`
- let the downstream service append its own name in the response

## Recommended demo setup

For a low-cost homework demo, run the app with the `h2` profile so the container does not need a separate PostgreSQL instance.

## 1. Launch EC2

Suggested settings:

- Amazon Linux 2023
- instance type: `t3.micro` or the lowest allowed in your account
- security group inbound:
  - `22` from your IP only
  - `8080` from your IP or demo audience

## 2. Install Docker on EC2

```bash
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
newgrp docker
```

## 3. Copy or clone the project

```bash
git clone https://github.com/lopsun7/student-management-system.git
cd student-management-system
```

If your latest code is only local, push it first and then clone it on EC2.

## 4. Build the Docker image

```bash
docker build -t student-management-system .
```

## 5. Run the container

```bash
docker run -d \
  --name student-management-system \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=h2 \
  -e DOWNSTREAM_BASE_URL=http://18.216.74.156:8080 \
  -e DOWNSTREAM_AGGREGATION_PATH=/name/aggregation \
  -e DOWNSTREAM_DEFAULT_NAME=Steven \
  student-management-system
```

## 6. Verify the app

Health check:

```bash
curl http://<EC2_PUBLIC_IP>:8080/actuator/health
```

Downstream aggregation:

```bash
curl -X POST http://<EC2_PUBLIC_IP>:8080/api/v1/integrations/name/aggregation
```

Or pass the same name explicitly:

```bash
curl -X POST http://<EC2_PUBLIC_IP>:8080/api/v1/integrations/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":"Steven"}'
```

Expected response pattern:

```json
{
  "name": "Steven, Jocelyn"
}
```

## 7. Check logs

```bash
docker logs -f student-management-system
```

## Optional: use PostgreSQL instead of H2

If you want the full student CRUD data stored outside the container, run with PostgreSQL settings instead of the `h2` profile:

```bash
docker run -d \
  --name student-management-system \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<DB_HOST>:5432/student_management_system \
  -e SPRING_DATASOURCE_USERNAME=<DB_USER> \
  -e SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD> \
  -e DOWNSTREAM_BASE_URL=http://18.216.74.156:8080 \
  -e DOWNSTREAM_AGGREGATION_PATH=/name/aggregation \
  -e DOWNSTREAM_DEFAULT_NAME=Steven \
  student-management-system
```
