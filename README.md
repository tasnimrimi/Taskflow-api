<div align="center">

# TaskFlow API

### A tested task-management REST API built with Java and Spring Boot

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white)
![Tests](https://img.shields.io/badge/Automated_Tests-65-22C55E?style=for-the-badge)

</div>

## Overview

TaskFlow is a layered backend application for creating and managing user-owned tasks. It exposes a JSON REST API, applies request validation and business logic, persists data with Spring Data JPA, and returns meaningful HTTP status codes and validation errors.

The application supports a local H2 setup and a PostgreSQL profile. PostgreSQL schema changes are managed through versioned Flyway migrations.

## Features

- Create, list, find, partially update, and delete tasks
- Filter tasks by completed or unfinished status
- Search task titles using case-insensitive partial matching
- Return tasks in page-sized groups with pagination metadata
- Sort paginated tasks by ID, title, or completion status in ascending or descending order
- Reject invalid page sizes, page numbers, sort fields, and sort directions
- Validate task titles before processing requests
- Return structured JSON validation errors
- Use the appropriate `200`, `201`, `204`, `400`, and `404` status codes
- Separate HTTP, application, and persistence responsibilities
- Persist local development data with file-based H2
- Run with PostgreSQL through a dedicated Spring profile
- Manage the PostgreSQL schema with Flyway
- Publish machine-readable OpenAPI documentation and an interactive Swagger UI
- Authenticate through a public login endpoint that returns a signed JWT access token
- Protect task endpoints with Spring Security OAuth2 Resource Server and bearer-token authentication
- Use stateless authentication so every protected request is verified from its JWT
- Reject missing, malformed, incorrectly signed, and expired access tokens
- Keep Swagger, OpenAPI, and health-check endpoints publicly accessible
- Register database-backed users with validated email addresses and BCrypt password hashing
- Reject duplicate email registration without exposing passwords or password hashes
- Authenticate registered users by loading their email and password hash from the database
- Associate every newly created task with its authenticated owner
- Restrict listing, filtering, searching, pagination, reading, updating, and deleting to the task owner
- Return `404 Not Found` when a task is missing or belongs to another user
- Verify service, controller, repository, and complete application workflows with automated tests

## Application Flow

```text
Client
  ↓ HTTP request / JSON
Spring Security
  ↓ authenticated request
TaskController
  ↓ application call
TaskService
  ↓ data operation
TaskRepository
  ↓ JPA / Hibernate / SQL
Database
```

- **Controller:** handles routes, JSON, validation, and HTTP responses.
- **Spring Security:** loads registered users through `UserDetailsService`, verifies BCrypt passwords, and rejects unauthenticated access before requests reach the controller.
- **Service:** contains task-related application logic and enforces task ownership.
- **Repository:** provides database operations through Spring Data JPA.
- **Hibernate:** converts Java entity operations into SQL.
- **Flyway:** creates and versions the PostgreSQL database structure.

## API Reference

| Method | Endpoint | Success | Description |
|---|---|---:|---|
| `GET` | `/api/tasks` | `200` | List the authenticated user's tasks |
| `GET` | `/api/tasks?completed={boolean}` | `200` | List the authenticated user's completed or unfinished tasks |
| `GET` | `/api/tasks/search?title={text}` | `200` | Search the authenticated user's task titles using case-insensitive partial matching |
| `GET` | `/api/tasks/page?page={number}&size={number}&sortBy={field}&direction={order}` | `200` | Return a validated, sorted page of the authenticated user's tasks |
| `GET` | `/api/tasks/{id}` | `200` | Find an owned task; returns `404` when missing or owned by another user |
| `POST` | `/api/tasks` | `201` | Create a task owned by the authenticated user |
| `PATCH` | `/api/tasks/{id}` | `200` | Update supplied fields on an owned task; returns `404` when unavailable |
| `DELETE` | `/api/tasks/{id}` | `204` | Delete an owned task; returns `404` when unavailable |
| `POST` | `/api/auth/register` | `201` | Register a database-backed user; returns `409` for a duplicate email |
| `POST` | `/api/auth/login` | `200` | Verify an email and password and return a signed JWT access token |
| `GET` | `/actuator/health` | `200` | Check application health |

All `/api/tasks` endpoints require authentication. Anonymous requests receive `401 Unauthorized`.

### Register a user

Registration is public so a new visitor can create an account:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "email": "learner@example.com",
  "password": "Learning123!"
}
```

The email is normalized, the password is stored only as a BCrypt hash, and the response exposes only safe fields:

```json
{
  "id": 1,
  "email": "learner@example.com"
}
```

Invalid registration data returns `400 Bad Request`, while an existing email returns `409 Conflict`.

### Log in

Submit the registered email and password:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "learner@example.com",
  "password": "Learning123!"
}
```

A successful login returns a signed access token that is valid for 900 seconds:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

Send that token with every protected request:

```http
Authorization: Bearer eyJ...
```

An incorrect email or password returns `401 Unauthorized` without creating a token.

### Create a task

```http
POST /api/tasks
Content-Type: application/json
Authorization: Bearer eyJ...
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

### Filter tasks by status

Return only completed tasks:

```http
GET /api/tasks?completed=true
```

Return only unfinished tasks:

```http
GET /api/tasks?completed=false
```

Omit the `completed` query parameter to return every task.

### Search tasks by title

Search is case-insensitive and matches text contained anywhere in a title:

```http
GET /api/tasks/search?title=spring
```

For example, the search text `spring` matches both `Learn Spring Boot` and `SPRING testing`.

### Paginate tasks

Request the first page with two tasks per page:

```http
GET /api/tasks/page?page=0&size=2
```

Page numbers start at `0`. The response contains the tasks under `content` together with metadata such as `totalElements`, `totalPages`, `number`, `size`, `first`, and `last`.

Sort the page by title from A to Z:

```http
GET /api/tasks/page?page=0&size=2&sortBy=title&direction=asc
```

Supported sort fields are `id`, `title`, and `completed`. Supported directions are `asc` and `desc`. The page number must be zero or greater, and the page size must be between 1 and 100. Invalid values return `400 Bad Request`.

Example response:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Learn Spring Boot",
      "completed": false
    }
  ],
  "totalElements": 4,
  "totalPages": 2,
  "number": 0,
  "size": 2,
  "first": true,
  "last": false
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

## Interactive API Documentation

While the application is running, open Swagger UI to explore and execute the documented endpoints from a browser:

```text
http://localhost:8080/swagger-ui.html
```

The machine-readable OpenAPI document is available as JSON at:

```text
http://localhost:8080/v3/api-docs
```

The documentation is generated from the Spring MVC controllers and enriched with OpenAPI metadata from `OpenApiConfig` and `TaskController`.

## Security

Spring Security protects the task API with stateless JWT bearer authentication. Swagger UI, OpenAPI JSON, the health endpoint, registration, login, and error responses remain public.

Registered users are persisted in `app_users`, and only BCrypt password hashes are stored. During login, `UserDetailsService` finds the submitted email through `AppUserRepository`, and `AuthenticationManager` uses BCrypt to verify the submitted password. A successful login asks `TokenService` to create a signed RSA access token containing the user's email as its subject, together with its issue and expiration times.

Spring Security's OAuth2 Resource Server reads bearer tokens on protected requests. `JwtDecoder` verifies the signature and expiration, then exposes the token subject through `Principal.getName()`. The server does not save a login session; every protected request must provide its token. The current development RSA key pair is generated when the application starts, so tokens become invalid after a restart.

Every task is associated with an owner. Task queries include the authenticated owner, preventing one registered user from listing, reading, updating, or deleting another user's tasks. A task that is missing or belongs to another user produces the same `404 Not Found` response so the API does not reveal another user's data.

```text
Register → POST /api/auth/register → 201 Created
Correct email and password → POST /api/auth/login → signed access token
Wrong password or unknown email → POST /api/auth/login → 401 Unauthorized
Valid bearer token → /api/tasks → 200 OK
Missing, malformed, or expired token → /api/tasks → 401 Unauthorized
Anonymous request → /v3/api-docs → 200 OK
```

## Run Locally with H2

Requirements: Java 17 and Git. The Maven Wrapper is included, so a global Maven installation is not required.

```powershell
git clone https://github.com/tasnimrimi/Taskflow-api.git
cd Taskflow-api
.\mvnw.cmd spring-boot:run
```

The default profile stores H2 data in `./data/taskflow`. Register an account through `/api/auth/register`, log in through `/api/auth/login`, and send the returned access token as a bearer token when calling [http://localhost:8080/api/tasks](http://localhost:8080/api/tasks).

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

The schema is managed by these versioned migrations:

```text
V1__create_tasks_table.sql         # creates the tasks table
V2__limit_task_title_length.sql    # limits titles to 100 characters
V3__create_app_users_table.sql     # creates users with unique emails and password hashes
V4__add_task_owner.sql             # associates tasks with their owning users
```

Flyway records completed migrations in `flyway_schema_history` and applies each version only once.

## Automated Tests

The project currently contains 65 focused automated tests:

- **13 service tests:** owner-aware task behavior plus email normalization, password hashing, persistence, and duplicate-registration prevention
- **22 controller tests:** task, registration, and login routing plus safe JSON, validation, pagination, sorting, and HTTP responses
- **10 repository tests:** real task and user persistence, ownership, lookup, filtering, pagination, and sorting with temporary H2
- **20 integration and application-context tests:** complete task ownership, cross-user rejection, documentation, registration, password verification, real JWT login, bearer-token access, and rejection of invalid or expired tokens

Run the focused test suite:

```powershell
.\mvnw.cmd "-Dtest=TaskServiceTest,UserServiceTest,TaskControllerTest,AuthControllerTest,TaskRepositoryTest,AppUserRepositoryTest,TaskFlowIntegrationTest,AuthenticationIntegrationTest" test
```

Expected result:

```text
Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/tasnim/taskflow_api/
│   │   ├── ApiExceptionHandler.java
│   │   ├── AppUser.java
│   │   ├── AppUserRepository.java
│   │   ├── AuthController.java
│   │   ├── CreateTaskRequest.java
│   │   ├── JwtConfig.java
│   │   ├── LoginRequest.java
│   │   ├── OpenApiConfig.java
│   │   ├── RegisterUserRequest.java
│   │   ├── SecurityConfig.java
│   │   ├── Task.java
│   │   ├── TaskController.java
│   │   ├── TaskRepository.java
│   │   ├── TaskService.java
│   │   ├── TaskflowApiApplication.java
│   │   ├── TokenResponse.java
│   │   ├── TokenService.java
│   │   ├── UpdateTaskRequest.java
│   │   ├── UserResponse.java
│   │   └── UserService.java
│   └── resources/
│       ├── db/migration/
│       │   ├── V1__create_tasks_table.sql
│       │   ├── V2__limit_task_title_length.sql
│       │   ├── V3__create_app_users_table.sql
│       │   └── V4__add_task_owner.sql
│       ├── application-postgres.properties
│       └── application.properties
└── test/java/com/tasnim/taskflow_api/
    ├── AppUserRepositoryTest.java
    ├── AuthControllerTest.java
    ├── AuthenticationIntegrationTest.java
    ├── TaskControllerTest.java
    ├── TaskFlowIntegrationTest.java
    ├── TaskRepositoryTest.java
    ├── TaskServiceTest.java
    └── UserServiceTest.java
```

## Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- OpenAPI 3 and Swagger UI
- Spring Security
- Spring Security OAuth2 Resource Server
- RSA-signed JWT access tokens
- BCrypt password hashing
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
