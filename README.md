<div align="center">

# ✅ TaskFlow API

### A beginner-friendly task management REST API built with Java and Spring Boot

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Learning_Project-7C5CFC?style=for-the-badge)

</div>

## About

TaskFlow is a small backend project created to learn how a REST API works from request to response. A client sends an HTTP request, Spring routes it to a Java controller, and the result is returned as JSON.

## Current Features

- List all tasks with `GET /api/tasks`
- Create a task with `POST /api/tasks`
- Generate task IDs automatically
- Validate JSON request bodies
- Store tasks in memory while the server is running
- Check application health through Spring Boot Actuator

## Request Workflow

```text
Client → embedded Tomcat server → Spring controller → Java objects → JSON response
```

## API Reference

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/tasks` | Return every task |
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/actuator/health` | Check whether the application is healthy |

Example request:

```json
{
  "title": "Practise Spring Boot"
}
```

Example response:

```json
{
  "id": 4,
  "title": "Practise Spring Boot",
  "completed": false
}
```

## Run Locally

Requirements: Java 17 and Git. Maven does not need to be installed because the repository includes the Maven Wrapper.

```powershell
git clone https://github.com/tasnimrimi/Taskflow-api.git
cd Taskflow-api
.\mvnw.cmd spring-boot:run
```

Then open [http://localhost:8080/api/tasks](http://localhost:8080/api/tasks).

## Project Structure

```text
src/main/java/com/tasnim/taskflow_api/
├── TaskflowApiApplication.java   # application entry point
├── Task.java                     # task data model
└── TaskController.java           # API routes and in-memory storage
```

## Author

**Tasnim Akhter** · [GitHub](https://github.com/tasnimrimi) · [LinkedIn](https://www.linkedin.com/in/tasnim-akhter%F0%9F%92%A0-a99082315/)
