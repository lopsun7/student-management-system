# Student Management System

A Spring Boot CRUD REST API for managing student records. This project was built by following the structure of the linked Spring Boot employee-management style tutorial and adapting the domain from employees to students.

## Tech Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- H2 (test scope)

## Features

- Create a student
- Get all students
- Get a student by ID
- Update a student
- Delete a student
- Validation for required fields and email format
- JSON error responses for missing records and validation failures
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
| `POST` | `/api/v1/students` | Create a new student |
| `GET` | `/api/v1/students/{id}` | Get a student by ID |
| `PUT` | `/api/v1/students/{id}` | Update a student |
| `DELETE` | `/api/v1/students/{id}` | Delete a student |

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

## Run the Project

```bash
./mvnw spring-boot:run
```

## Run Tests

```bash
./mvnw test
```

## Project Structure

- [src/main/java/com/studentmanagement/controller/StudentController.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/controller/StudentController.java:1)
- [src/main/java/com/studentmanagement/model/Student.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/model/Student.java:1)
- [src/main/java/com/studentmanagement/repository/StudentRepository.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/repository/StudentRepository.java:1)
- [src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/serviceimpl/StudentServiceImpl.java:1)
- [src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/aspect/ServiceLoggingAspect.java:1)
- [src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java](/Users/lopsun/Documents/New project 4/src/main/java/com/studentmanagement/exception/GlobalExceptionHandler.java:1)

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

## Tutorial Reference

Assignment video provided by the instructor:

- [YouTube Video](https://www.youtube.com/watch?v=v1IFQWzuSrw)
