# Student Management System

A Spring Boot CRUD REST API for managing student records. This project was built by following the structure of the linked Spring Boot employee-management style tutorial and adapting the domain from employees to students.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
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
- Forward a name to a downstream service so it can append its own name
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
| `POST` | `/api/v1/integrations/name/aggregation` | Send a name to the downstream aggregation service |

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

## Run Tests

```bash
./mvnw test
```

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

This project now includes a downstream integration endpoint for the assignment.

The flow is:

1. this app sends `Steven` to the downstream service
2. the downstream service adds its own name
3. this app returns the downstream response to the caller

Local endpoint:

```text
POST /api/v1/integrations/name/aggregation
```

Default downstream target:

```text
http://18.216.74.156:8080/name/aggregation
```

Default request payload sent by this app:

```json
{
  "name": "Steven"
}
```

Example local request:

```bash
curl -X POST http://localhost:8080/api/v1/integrations/name/aggregation
```

Or explicitly pass the same name:

```bash
curl -X POST http://localhost:8080/api/v1/integrations/name/aggregation \
  -H "Content-Type: application/json" \
  -d '{"name":"Steven"}'
```

Expected response pattern:

```json
{
  "name": "Steven, Jocelyn"
}
```

Environment variables for downstream configuration:

- `DOWNSTREAM_BASE_URL`
- `DOWNSTREAM_AGGREGATION_PATH`
- `DOWNSTREAM_DEFAULT_NAME`

## Project Structure

- [src/main/java/com/studentmanagement/controller/StudentController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/StudentController.java:1)
- [src/main/java/com/studentmanagement/controller/DownstreamAggregationController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/DownstreamAggregationController.java:1)
- [src/main/java/com/studentmanagement/model/Student.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/model/Student.java:1)
- [src/main/java/com/studentmanagement/repository/StudentRepository.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/repository/StudentRepository.java:1)
- [src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java:1)
- [src/main/java/com/studentmanagement/service/DownstreamAggregationService.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/service/DownstreamAggregationService.java:1)
- [src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java:1)
- [src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java:1)

## Deploy with Docker on EC2

See [DEPLOY_EC2.md](/Users/lopsun/Documents/New project 4/DEPLOY_EC2.md) for the full EC2 deployment steps using the existing `Dockerfile`.

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
