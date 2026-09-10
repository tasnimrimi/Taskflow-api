<div align="center">

# TaskFlow API

### A tested task-management REST API built with Java and Spring Boot

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Tests](https://img.shields.io/badge/Automated_Tests-18-22C55E?style=for-the-badge)

</div>

## Overview

TaskFlow is a layered backend application for creating and managing tasks. It exposes a JSON REST API, applies request validation and business logic, persists data with Spring Data JPA, and returns meaningful HTTP status codes and validation errors.

The application supports a local H2 setup and a PostgreSQL profile. PostgreSQL schema changes are managed through versioned Flyway migrations.

## Features

- Create, list, find, partially update, and delete tasks
- Validate task titles before processing requests
- Return structured JSON validation errors
- Use the appropriate `200`, `201`, `204`, `400`, and `404` status codes
- Separate HTTP, application, and persistence responsibilities
- Persist local development data with file-based H2
- Run with PostgreSQL through a dedicated Spring profile
- Manage the PostgreSQL schema with Flyway
- Verify service, controller, repository, and complete application workflows with automated tests

## Application Flow

```text
Client
  ↓ HTTP request / JSON
TaskController
  ↓ application call
TaskService
  ↓ data operation
TaskRepository
  ↓ JPA / Hibernate / SQL
Database
```

- **Controller:** handles routes, JSON, validation, and HTTP responses.
- **Service:** contains task-related application logic.
- **Repository:** provides database operations through Spring Data JPA.
- **Hibernate:** converts Java entity operations into SQL.
- **Flyway:** creates and versions the PostgreSQL database structure.

## API Reference

| Method | Endpoint | Success | Description |
|---|---|---:|---|
| `GET` | `/api/tasks` | `200` | List every task |
| `GET` | `/api/tasks/{id}` | `200` | Find one task; returns `404` when missing |
| `POST` | `/api/tasks` | `201` | Create a task |
| `PATCH` | `/api/tasks/{id}` | `200` | Update only the supplied fields; returns `404` when missing |
| `DELETE` | `/api/tasks/{id}` | `204` | Delete a task; returns `404` when missing |
| `GET` | `/actuator/health` | `200` | Check application health |

### Create a task

```http
POST /api/tasks
Content-Type: application/json
```

```json
{
  "title": "Learn Spring Boot testing"
}
```

Example response:

```json
{
  "id": 1,
  "title": "Learn Spring Boot testing",
  "completed": false
}
```

### Partially update a task

Only the provided fields are changed:

```http
PATCH /api/tasks/1
Content-Type: application/json
```

```json
{
  "completed": true
}
```

### Validation error

A blank title produces `400 Bad Request`:

```json
{
  "error": "Validation failed",
  "field": "title",
  "message": "Title is required"
}
```

## Run Locally with H2

Requirements: Java 17 and Git. The Maven Wrapper is included, so a global Maven installation is not required.

```powershell
git clone https://github.com/tasnimrimi/Taskflow-api.git
cd Taskflow-api
.\mvnw.cmd spring-boot:run
```

The default profile stores H2 data in `./data/taskflow`. Open the API at [http://localhost:8080/api/tasks](http://localhost:8080/api/tasks).

## Run Locally with PostgreSQL

Requirements: PostgreSQL 17 with an empty database named `taskflow` and a local PostgreSQL user named `postgres`.

Set the password only in your current PowerShell session:

```powershell
$env:DB_PASSWORD = "<your-local-postgresql-password>"
$env:SPRING_PROFILES_ACTIVE = "postgres"
.\mvnw.cmd spring-boot:run
```

The password is read through `${DB_PASSWORD}` and is never stored in the repository. With the `postgres` profile active, Flyway applies the migrations under `src/main/resources/db/migration`, and Hibernate validates the resulting schema.

## Database Migration

The current schema is created by:

```text
src/main/resources/db/migration/V1__create_tasks_table.sql
```

Flyway records the completed migration in `flyway_schema_history`, ensuring version 1 is applied only once.

## Automated Tests

The project currently contains 18 focused automated tests:

- **5 service tests:** task lookup, partial updates, and deletion logic using a mocked repository
- **7 controller tests:** routing, JSON, validation, and HTTP responses using a mocked service
- **2 repository tests:** real JPA save, find, and delete behavior with temporary H2
- **4 integration tests:** complete GET, POST, PATCH, and DELETE workflows through all application layers

Run the focused test suite:

```powershell
.\mvnw.cmd "-Dtest=TaskServiceTest,TaskControllerTest,TaskRepositoryTest,TaskFlowIntegrationTest" test
```

Expected result:

```text
Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/tasnim/taskflow_api/
│   │   ├── ApiExceptionHandler.java
│   │   ├── CreateTaskRequest.java
│   │   ├── Task.java
│   │   ├── TaskController.java
│   │   ├── TaskRepository.java
│   │   ├── TaskService.java
│   │   ├── TaskflowApiApplication.java
│   │   └── UpdateTaskRequest.java
│   └── resources/
│       ├── db/migration/V1__create_tasks_table.sql
│       ├── application-postgres.properties
│       └── application.properties
└── test/java/com/tasnim/taskflow_api/
    ├── TaskControllerTest.java
    ├── TaskFlowIntegrationTest.java
    ├── TaskRepositoryTest.java
    └── TaskServiceTest.java
```

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- PostgreSQL 17
- H2 Database
- Flyway
- Jakarta Validation
- JUnit 5
- Mockito
- MockMvc
- Maven Wrapper

## Author

**Tasnim Akhter** · [GitHub](https://github.com/tasnimrimi) · [LinkedIn](https://www.linkedin.com/in/tasnim-akhter%F0%9F%92%A0-a99082315/)
