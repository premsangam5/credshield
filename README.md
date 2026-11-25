# CredShield – Secure Loan Management System

CredShield is a backend-based loan management system developed using Java and Spring Boot. It provides REST APIs for user authentication, loan applications, loan approvals, repayments, and installment tracking.

The project focuses on secure authentication, role-based access control, and structured loan management.

## Features

- User registration and login
- JWT-based authentication
- Password encryption using BCrypt
- Role-based access control (USER and ADMIN)
- Loan application and application history
- Admin loan approval and rejection
- Loan search, filtering, and pagination
- EMI calculation
- Loan repayment management
- Monthly installment schedule generation
- Repayment history and summary
- Automatic loan closure after repayment completion
- Admin dashboard with loan statistics
- Audit logging
- Global exception handling
- API documentation using Swagger
- Unit testing using JUnit and Mockito
- Docker support

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming |
| Spring Boot | Backend framework |
| Spring Security | Application security |
| JWT | Authentication |
| Spring Data JPA | Database operations |
| Hibernate | ORM |
| MySQL | Relational database |
| Maven | Build management |
| JUnit 5 | Unit testing |
| Mockito | Mock testing |
| Swagger / OpenAPI | API documentation |
| Docker | Containerization |

## Project Structure

```text
credshield/
├── src/
│   ├── main/
│   │   ├── java/com/credshield/credshield/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
├── Dockerfile
├── .dockerignore
├── .gitignore
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites

- Java 21
- MySQL 8.0+
- Maven (or use the included Maven Wrapper)
- Docker (optional)

### 1. Clone the repository

```bash
git clone https://github.com/premsangam5/credshield.git
cd credshield
```

### 2. Create the database

```sql
CREATE DATABASE credshield;
```

### 3. Configure environment variables

Set the following environment variables before running the application:

```text
DB_URL=jdbc:mysql://localhost:3306/credshield
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```

Never commit real passwords or secret keys to GitHub.

### 4. Run the application

On Windows PowerShell:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/credshield"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"

.\mvnw.cmd spring-boot:run
```

### 5. Open Swagger UI

After the application starts, visit:

http://localhost:8080/swagger-ui/index.html

Use Swagger UI to explore and test the REST APIs.

## Running Tests

Run the test suite using:

```powershell
.\mvnw.cmd test
```

## Running with Docker

Build the Docker image:

```bash
docker build -t credshield:1.0 .
```

Run the container:

```bash
docker run -d --name credshield-container -p 8080:8080 -e DB_URL="jdbc:mysql://host.docker.internal:3306/credshield" -e DB_USERNAME="root" -e DB_PASSWORD="your_mysql_password" credshield:1.0
```

Check application logs:

```bash
docker logs credshield-container
```

The database must be accessible from the container. The `host.docker.internal` hostname is suitable for Docker Desktop on Windows.

## API Documentation

CredShield provides REST APIs for:

- User registration and authentication
- Loan applications and management
- Administrative loan operations
- Loan repayments
- Installment schedules
- Repayment summaries
- Admin dashboard statistics
- Audit logs

Explore the available endpoints through Swagger UI.

## Security

- Passwords are hashed using BCrypt.
- JWT is used for authentication.
- Role-based authorization protects restricted operations.
- Database credentials are supplied through environment variables.

## Author

**Prem Sangam**

GitHub: https://github.com/premsangam5

---

*CredShield is an educational backend development project built to demonstrate Java, Spring Boot, REST API development, database management, authentication, testing, and Docker fundamentals.*