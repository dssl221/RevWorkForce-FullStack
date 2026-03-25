# RevWorkforce - Human Resource Management System

RevWorkforce is a comprehensive full-stack monolithic Human Resource Management (HRM) application. 
The system streamlines employee directory management, leave tracking, goal setting, and performance reviews. It features role-based access control (Admin, Manager, Employee) with a responsive, premium dark-themed web interface.

## 🚀 Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2.3** (Web, Validation)
- **Spring Security** (Session-based Auth, BCrypt password encoding)
- **Spring JDBC & Oracle Data Access** (`JdbcTemplate`, `SimpleJdbcCall`)
- **Lombok** (Boilerplate reduction)

### Database
- **Oracle Database** (tested with `ORCLPDB` at `localhost:1521`)
- Data Logic entirely handled via **PL/SQL Stored Procedures and Functions**

### Frontend
- **Thymeleaf** (Server-side rendering and routing)
- **HTML5 & Vanilla CSS3** (Premium dark theme, CSS Variables, Glassmorphism, animations)
- **JavaScript & jQuery 3.7** (AJAX API integration, DOM manipulation)
- **Bootstrap 5.3** (Grid system functionality)

## 🏗️ Architecture

RevWorkforce uses a clean, layered monolithic architecture heavily reliant on Oracle database procedures for data integrity and speed.

1. **Frontend Layer**: Thymeleaf templates serve the base HTML. jQuery calls REST APIs for dynamic updates.
2. **Controller Layer**: REST APIs map incoming HTTP requests (`@RestController`) to the Service layer logic.
3. **Service Layer**: Handles business logic, orchestrates data validation, and manages transactions.
4. **Data Access Layer (DAO)**: Uses standard Spring JDBC components (`SimpleJdbcCall` and `JdbcTemplate`) to invoke predefined Oracle PL/SQL stored procedures. **No JPA/Hibernate is used.**
5. **Database Layer (Oracle)**: 10 tables manage data. 30+ PL/SQL procedures and functions execute all Create, Read, Update, Delete (CRUD) operations.

## ✨ Key Features

### Role-Based Access Control
- **ADMIN**: Full system control (manage employees, add/delete leave types, announcements, holidays, view all reports).
- **MANAGER**: Team oversight (approve/reject team leaves, set team goals, write performance reviews, view team data).
- **EMPLOYEE**: Self-service profile management, apply for leaves, track personal goals, complete self-assessments.

### Modules 
1. **Authentication & Profile**: secure login/logout and view/edit profile info.
2. **Dashboard**: Role-customized statistics, leave balances, pending tasks, and announcements.
3. **Leave Management**: Apply for casual/sick/maternity leaves, manager approval workflow, automatic balance deductions.
4. **Goal Management**: Add, update progress, prioritize, and receive manager comments on specific goals.
5. **Performance Reviews**: Self-assessment capabilities coupled with manager ratings and feedback.
6. **Company Directory & Announcements**: Search for employees, system-wide notifications, and holiday viewing.

## 🛠️ Setup & Installation Instructions

### 1. Database Setup
Ensure Oracle Database is running. You must execute the setup script:
```bash
sqlplus sys/yourpassword as sysdba
@database/oracle_setup.sql
```
*(This script creates the `REVWORKFORCE_WEB` user with password `rev123`, all tables, sequences, indexes, the 30+ PL/SQL procedures, and the necessary seed data).*

### 2. Configure Application Properties
Edit `src/main/resources/application.properties` to ensure the Oracle JDBC fields match your environment:
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/ORCLPDB
spring.datasource.username=REVWORKFORCE_WEB
spring.datasource.password=rev123
```

### 3. Build & Run
From the root directory of the project, build and start the Spring Boot application using Maven:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Access the Application
Open a browser and navigate to:
**http://localhost:8080**

### Demo User Accounts (Pre-seeded by `oracle_setup.sql`)
| Role | Email | Password |
|---|---|---|
| Admin | `admin@revworkforce.com` | `admin123` |
| Manager | `manager@revworkforce.com` | `manager123` |
| Employee | `employee@revworkforce.com` | `employee123` |

## 📁 Project Structure

```text
RevWorkforce/
├── database/
│   └── oracle_setup.sql              # Database schema, procedures, and seed data
├── src/
│   └── main/
│       ├── java/com.revworkforce.revworkforce_web/
│       │   ├── config/               # SecurityConfig, DataInitializer
│       │   ├── controller/           # REST endpoints (Admin, Auth, Employee, Leave...)
│       │   ├── dao/                  # JDBC Data Access mapping to PL/SQL
│       │   ├── model/                # POJOs (User, LeaveRequest, Goal...)
│       │   ├── service/              # Business logic implementation
│       │   └── RevworkforceWebApplication.java 
│       └── resources/
│           ├── static/
│           │   ├── css/style.css     # Premium dark theme stylesheet
│           │   └── js/app.js         # Core jQuery frontend application logic
│           ├── templates/            # Thymeleaf HTML views (dashboard.html, leaves.html...)
│           └── application.properties # Spring Boot configuration
└── pom.xml                           # Maven dependencies
```

## 🔒 Security Summary
* Paths `/css/**`, `/js/**`, `/api/auth/**`, `/login`, `/register` are publicly accessible.
* All other API endpoints (`/api/**`) require an active, authenticated session.
* `/api/admin/**` endpoints are restricted to users with the `ADMIN` role.
* Non-API UI pages (`/dashboard`, `/leaves`, etc.) bounce unauthenticated users back to `/login` via UI JavaScript validation as well as Spring Security mapping.

---
*Developed with Java 17 and Spring Boot 3.*
