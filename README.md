# Student Management System

A Spring Boot CRUD REST API for managing student records. This project was built by following the structure of the linked Spring Boot employee-management style tutorial and adapting the domain from employees to students.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Spring Security
- Spring OAuth2 Resource Server
- Spring Kafka
- PostgreSQL
- Maven
- H2 (demo runtime and tests)

## Features

- Create a student
- Get all students
- Search students by course with `@RequestParam`
- Get a student by ID
- Update a student
- Delete a student
- Return `Steven + incoming names` for upstream integration
- Issue local demo OAuth2-style JWT access tokens
- Protect student management APIs with Bearer token authentication
- Publish student-created events to Kafka
- Consume Kafka events with a consumer group using 3-way parallel processing
- Validation for required fields and email format
- JSON error responses with `@RestControllerAdvice`
- Transaction management with `@Transactional`
- Actuator health and info endpoints
- Simple async logging example with `@Async`
- Service-layer AOP logging with `@Before`, `@After`, and `@Around` advice

## Student Model

Each student record contains:

- `id`
- `firstName`
- `lastName`
- `email`
- `course`

## API Endpoints

Base URL: `http://localhost:8080/api/v1/students`

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/students` | Get all students |
| `GET` | `/api/v1/students/search?course=Java` | Search students by course |
| `POST` | `/api/v1/students` | Create a new student |
| `GET` | `/api/v1/students/{id}` | Get a student by ID |
| `PUT` | `/api/v1/students/{id}` | Update a student |
| `DELETE` | `/api/v1/students/{id}` | Delete a student |
| `POST` | `/api/v1/auth/token` | Exchange demo username/password for a Bearer JWT |
| `POST` | `/api/v1/integrations/name/aggregation` | Return the downstream result after forwarding `Steven + incoming names` |
| `POST` | `/api/v1/kafka/student-events` | Publish a Kafka validation event when Kafka is enabled |
| `GET` | `/api/v1/kafka/consumed-events` | Read consumed Kafka validation events when Kafka is enabled |
| `DELETE` | `/api/v1/kafka/consumed-events` | Clear consumed Kafka validation events when Kafka is enabled |

## Sample Request Body

```json
{
  "firstName": "Ava",
  "lastName": "Johnson",
  "email": "ava.johnson@example.com",
  "course": "Computer Science"
}
```

## PostgreSQL Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE student_management_system;
```

The application reads database settings from environment variables when available:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Default local values are already defined in [src/main/resources/application.properties](/Users/lopsun/Documents/New project 4/src/main/resources/application.properties:1).

For a container-only demo without PostgreSQL, use:

- `SPRING_PROFILES_ACTIVE=h2`

## Run the Project

```bash
./mvnw spring-boot:run
```

To run with the in-memory demo database:

```bash
SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run
```

## OAuth2 and Spring Security

The student management APIs are protected by Spring Security and OAuth2 Resource Server JWT validation.

Public endpoints:

- `POST /api/v1/auth/token`
- `POST /api/v1/integrations/name/aggregation`
- `GET /actuator/health`
- `GET /actuator/info`

Protected endpoints:

- `GET /api/v1/students`
- `GET /api/v1/students/search?course=Java`
- `POST /api/v1/students`
- `GET /api/v1/students/{id}`
- `PUT /api/v1/students/{id}`
- `DELETE /api/v1/students/{id}`

Demo users:

```text
username: steven
password: password123

username: admin
password: admin123
```

Get an access token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"steven","password":"password123"}' \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
```

Call a protected API with the Bearer token:

```bash
curl http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer ${TOKEN}"
```

Without a token, protected APIs return `401 Unauthorized`.

Security-related environment variables:

- `JWT_SECRET`: HMAC secret used to sign local demo JWTs. Change this outside local demos.
- `JWT_EXPIRATION_MINUTES`: token lifetime, default `60`.
- `DEMO_USER_USERNAME`
- `DEMO_USER_PASSWORD`
- `DEMO_ADMIN_USERNAME`
- `DEMO_ADMIN_PASSWORD`

## Run Tests

```bash
./mvnw clean verify
```

This runs all JUnit 5 tests, generates the JaCoCo report, and fails the build if instruction coverage drops below 90%.

Current verified result:

- Tests: 50 passed
- JaCoCo instruction coverage: 93.33%
- HTML report: `target/site/jacoco/index.html`
- XML report for SonarQube: `target/site/jacoco/jacoco.xml`

## Actuator Endpoints

Base URL:

```text
http://localhost:8080/actuator
```

Examples:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

## Downstream Aggregation Endpoint

This project now includes a name aggregation endpoint for the assignment.

The flow is:

1. the upstream caller sends an array of names
2. this app first converts it to `Steven, ...`
3. this app POSTs that result to the downstream service
4. this app returns the downstream response to the caller

Local endpoint:

```text
POST /api/v1/integrations/name/aggregation
```

Copy-ready request body:

```json
{
  "name": ["...", "..."]
}
```

Example local request:

```bash
curl -X POST http://localhost:8080/api/v1/integrations/name/aggregation
```

Or explicitly pass names in the request body:

```bash
curl -X POST http://localhost:8080/api/v1/integrations/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":["...","..."]}'
```

Copy-ready response body:

```json
{
  "name": ["Steven", "...", "...", "Celine"]
}
```

Environment variables for downstream configuration:

- `DOWNSTREAM_BASE_URL`
- `DOWNSTREAM_AGGREGATION_PATH`
- `DOWNSTREAM_DEFAULT_NAME`

## Kafka Integration

Homework 22 adds Kafka to the project with:

- a 3-broker Docker Kafka cluster in [docker-compose.kafka.yml](/Users/lopsun/Documents/New project 4/docker-compose.kafka.yml:1)
- topic auto-creation with 3 partitions and replication factor 3
- a Spring Kafka producer for `STUDENT_CREATED` events
- a Spring Kafka consumer group using concurrency `3` for parallel processing
- validation endpoints to publish and read consumed events
- an embedded Kafka integration test that validates real produce and consume behavior

Kafka is disabled by default so the normal CRUD app can still run without a local broker. Enable it for the Kafka assignment demo:

```bash
docker compose -f docker-compose.kafka.yml up -d
```

Start the app with Kafka enabled:

```bash
KAFKA_ENABLED=true \
SPRING_PROFILES_ACTIVE=h2 \
KAFKA_BOOTSTRAP_SERVERS=localhost:9092,localhost:9093,localhost:9094 \
./mvnw spring-boot:run
```

Get a JWT token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"steven","password":"password123"}' \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
```

Creating a student publishes a Kafka event:

```bash
curl -X POST http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Steven",
    "lastName": "Zhao",
    "email": "steven.kafka@example.com",
    "course": "Kafka"
  }'
```

You can also publish a validation event directly:

```bash
curl -X POST http://localhost:8080/api/v1/kafka/student-events \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "STUDENT_CREATED",
    "studentId": 100,
    "email": "steven.kafka@example.com",
    "course": "Kafka"
  }'
```

Check consumed events:

```bash
curl http://localhost:8080/api/v1/kafka/consumed-events \
  -H "Authorization: Bearer ${TOKEN}"
```

Validate topic partition and replica configuration:

```bash
docker exec kafka-1 kafka-topics \
  --bootstrap-server kafka-1:29092 \
  --describe \
  --topic student-events
```

Expected result includes:

```text
PartitionCount: 3
ReplicationFactor: 3
```

## Project Structure

- [src/main/java/com/studentmanagement/controller/StudentController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/StudentController.java:1)
- [src/main/java/com/studentmanagement/controller/AuthController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/AuthController.java:1)
- [src/main/java/com/studentmanagement/controller/DownstreamAggregationController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/DownstreamAggregationController.java:1)
- [src/main/java/com/studentmanagement/controller/KafkaValidationController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/KafkaValidationController.java:1)
- [src/main/java/com/studentmanagement/config/SecurityConfig.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/config/SecurityConfig.java:1)
- [src/main/java/com/studentmanagement/config/KafkaTopicConfig.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/config/KafkaTopicConfig.java:1)
- [src/main/java/com/studentmanagement/kafka/KafkaStudentEventPublisher.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/kafka/KafkaStudentEventPublisher.java:1)
- [src/main/java/com/studentmanagement/kafka/StudentEventConsumer.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/kafka/StudentEventConsumer.java:1)
- [src/main/java/com/studentmanagement/model/Student.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/model/Student.java:1)
- [src/main/java/com/studentmanagement/repository/StudentRepository.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/repository/StudentRepository.java:1)
- [src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java:1)
- [src/main/java/com/studentmanagement/service/DownstreamAggregationService.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/service/DownstreamAggregationService.java:1)
- [src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java:1)
- [src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java:1)

## Deploy with Docker on EC2

See [DEPLOY_EC2.md](/Users/lopsun/Documents/New project 4/DEPLOY_EC2.md) for the full EC2 deployment steps using the existing `Dockerfile`.

## Jenkins Pipeline

This repo includes a [Jenkinsfile](/Users/lopsun/Documents/New project 4/Jenkinsfile:1) for CI/CD:

- GitHub webhook triggers Jenkins on branch updates
- Jenkins runs `./mvnw clean verify`
- JaCoCo enforces 90%+ unit/integration test coverage
- SonarQube scans code quality using the generated JaCoCo XML report
- the SonarQube quality gate can block deployment if quality checks fail
- merges into the `dev` branch deploy to EC2
- EC2 rebuilds and restarts the Docker container

See [JENKINS_PIPELINE.md](/Users/lopsun/Documents/New project 4/JENKINS_PIPELINE.md) for the Jenkins credential, webhook, and branch setup.

## Testing Strategy

The tests mirror the source directory structure:

- controller tests verify REST endpoints through MockMvc
- service implementation tests use Mockito for isolated business logic
- repository integration tests use H2 with the real schema
- client tests start a local HTTP server and verify the downstream POST body
- configuration, exception, DTO, and model tests cover supporting behavior

## Resilience4j Circuit Breaker

The downstream name aggregation flow uses Resilience4j for reliability:

- `@Retry` retries failed downstream calls
- `@CircuitBreaker` opens the circuit when downstream failures pass the configured threshold
- `fallbackMethod` downgrades the service by returning the locally aggregated names
- failed requests are persisted for async recovery

## AOP Notes

This project now has a simple teaching-style AOP example on the `serviceimpl` layer:

```java
@Pointcut("execution(* com.studentmanagement.serviceimpl..*(..))")
```

The aspect adds three basic logs:

- `@Before`: logs the method signature before the method runs
- `@After`: logs the method signature after the method finishes
- `@Around`: logs when the method starts and ends

Why the scope is `serviceimpl`:

- controller stays focused on HTTP requests and responses
- repository stays focused on database access
- service layer is a good place to demonstrate cross-cutting logic like logging

Key AOP characteristics in simple words:

- AOP is good for repeated logic such as logging or timing
- it helps avoid putting the same log code inside every method
- Spring AOP usually works at method level through proxies

Self-invocation note for this demo:

- external calls into `StudentServiceImpl` are intercepted by AOP
- an internal call like `createStudent() -> prepareStudentForSave()` does run, but it does not go through the proxy again
- the private helper `normalizeStudentData()` also runs, but it is not intercepted by Spring AOP

## HW11 Notes

This project also now demonstrates several Spring Boot topics from HW11 in a simple teaching-focused way:

- `@PathVariable` on the ID-based CRUD endpoints
- `@RequestBody` on create and update
- `@RequestParam` on the course search endpoint
- validation annotations in the `Student` model
- global exception handling in `GlobalExceptionHandler`
- `@Transactional` on the service layer
- Actuator monitoring endpoints
- `@Async` background logging after a student is created

## Tutorial Reference

Assignment video provided by the instructor:

- [YouTube Video](https://www.youtube.com/watch?v=v1IFQWzuSrw)
