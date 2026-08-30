<div align="center">

# ✅ TaskFlow API

### A beginner-friendly task management REST API built with Java and Spring Boot

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build_Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Learning_Project-7C5CFC?style=for-the-badge)

</div>

---

## 📖 About

TaskFlow API is a task-management backend created while learning Java and Spring Boot.

The project demonstrates how a client sends requests to a backend, how Spring routes those requests to Java methods, and how Java objects are returned as JSON.

## ✨ Current Features

- View all tasks
- Create a new task
- Automatic task ID generation
- JSON request and response handling
- Spring Boot health monitoring
- In-memory task storage

## 🛠️ Technology

- Java 17
- Spring Boot
- Spring Web
- Spring Boot Actuator
- Maven
- Git and GitHub

## 🔄 Request Workflow

```text
Client sends request
        ↓
Tomcat receives request
        ↓
Spring finds the correct controller
        ↓
Controller runs a Java method
        ↓
Task objects are created or retrieved
        ↓
Spring converts the result to JSON
        ↓
Client receives the response
```

## 🔗 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tasks` | Return all tasks |
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/actuator/health` | Check application health |

## 📥 Create a Task

Request:

```http
POST /api/tasks
Content-Type: application/json
```

```json
{
  "title": "Practise Spring Boot"
}
```

Response:

```json
{
  "id": 4,
  "title": "Practise Spring Boot",
  "completed": false
}
```

## ▶️ Run Locally

### Requirements

- Java 17
- Git

### Setup

Clone the repository:

```bash
git clone https://github.com/tasnimrimi/taskflow-api.git
```

Enter the project:

```bash
cd taskflow-api
```

Run on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
http://localhost:8080/api/tasks
```


## 👩‍💻 Author

**Tasnim Akhter**

- [GitHub](https://github.com/tasnimrimi)
- [LinkedIn](https://www.linkedin.com/in/tasnim-akhter%F0%9F%92%A0-a99082315/)

