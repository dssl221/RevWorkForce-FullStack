# RevWorkForce Database Walkthrough

## Overview

The RevWorkForce HRM system uses an **Oracle Database** hosted on **FreeSQLDatabase.com**. The database contains **10 tables**, **13 indexes**, and **28 PL/SQL stored procedures**.

- Setup script: `database/freesql_setup.sql`
- Original (local Oracle) script: `database/oracle_setup.sql`

---

## Tables (10 Total)

| # | Table Name | Purpose | Key Columns |
|---|-----------|---------|-------------|
| 1 | `departments` | Stores department names | `id`, `name` |
| 2 | `users` | All users (Admin, Manager, Employee) | `id`, `name`, `email`, `password`, `role`, `department`, `manager_id`, `salary` |
| 3 | `leave_types` | Types of leave available | `id`, `name`, `default_days` |
| 4 | `leave_requests` | Employee leave applications | `id`, `employee_id`, `leave_type`, `start_date`, `end_date`, `status` |
| 5 | `leave_balances` | Leave balance per employee per type | `id`, `employee_id`, `leave_type`, `total_days`, `used_days` |
| 6 | `goals` | Employee goals with progress tracking | `id`, `employee_id`, `description`, `deadline`, `priority`, `progress` |
| 7 | `performance_reviews` | Annual performance reviews | `id`, `employee_id`, `self_rating`, `manager_rating`, `status`, `review_year` |
| 8 | `announcements` | Company-wide announcements | `id`, `title`, `description`, `created_date` |
| 9 | `notifications` | Per-user notifications | `id`, `user_id`, `message`, `is_read`, `created_at` |
| 10 | `holidays` | Company holidays list | `id`, `name`, `holiday_date` |

---

## Table Details

### 1. `departments`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| name | VARCHAR2(255) | NOT NULL, UNIQUE |

### 2. `users`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| name | VARCHAR2(255) | NOT NULL |
| email | VARCHAR2(255) | NOT NULL, UNIQUE |
| password | VARCHAR2(255) | NOT NULL |
| employee_id | VARCHAR2(255) | UNIQUE |
| role | VARCHAR2(20) | NOT NULL (ADMIN / MANAGER / EMPLOYEE) |
| department | VARCHAR2(255) | |
| designation | VARCHAR2(255) | |
| manager_id | NUMBER | FK → users(id) |
| phone | VARCHAR2(20) | |
| address | VARCHAR2(500) | |
| emergency_contact | VARCHAR2(255) | |
| joining_date | DATE | |
| active | NUMBER(1) | NOT NULL, DEFAULT 1 |
| salary | NUMBER(15,2) | |

### 3. `leave_types`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| name | VARCHAR2(255) | NOT NULL, UNIQUE |
| default_days | NUMBER | NOT NULL |

### 4. `leave_requests`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| employee_id | NUMBER | NOT NULL, FK → users(id) |
| leave_type | VARCHAR2(255) | NOT NULL |
| start_date | DATE | NOT NULL |
| end_date | DATE | NOT NULL |
| reason | VARCHAR2(500) | |
| status | VARCHAR2(20) | NOT NULL, DEFAULT 'PENDING' |
| manager_comment | VARCHAR2(500) | |
| applied_date | DATE | |

### 5. `leave_balances`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| employee_id | NUMBER | NOT NULL, FK → users(id) |
| leave_type | VARCHAR2(255) | NOT NULL |
| total_days | NUMBER | NOT NULL |
| used_days | NUMBER | NOT NULL, DEFAULT 0 |

### 6. `goals`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| employee_id | NUMBER | NOT NULL, FK → users(id) |
| description | VARCHAR2(1000) | NOT NULL |
| deadline | DATE | |
| priority | VARCHAR2(20) | DEFAULT 'MEDIUM' |
| progress | NUMBER | NOT NULL, DEFAULT 0 |
| manager_comment | VARCHAR2(1000) | |

### 7. `performance_reviews`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| employee_id | NUMBER | NOT NULL, FK → users(id) |
| deliverables | VARCHAR2(2000) | |
| accomplishments | VARCHAR2(2000) | |
| improvements | VARCHAR2(2000) | |
| self_rating | NUMBER | |
| manager_rating | NUMBER | |
| manager_feedback | VARCHAR2(2000) | |
| status | VARCHAR2(20) | NOT NULL, DEFAULT 'DRAFT' |
| review_year | NUMBER | |

### 8. `announcements`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| title | VARCHAR2(255) | NOT NULL |
| description | VARCHAR2(2000) | |
| created_date | DATE | DEFAULT SYSDATE |

### 9. `notifications`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| user_id | NUMBER | NOT NULL, FK → users(id) |
| message | VARCHAR2(500) | NOT NULL |
| is_read | NUMBER(1) | NOT NULL, DEFAULT 0 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT SYSTIMESTAMP |

### 10. `holidays`
| Column | Type | Constraints |
|--------|------|-------------|
| id | NUMBER (IDENTITY) | PRIMARY KEY |
| name | VARCHAR2(255) | NOT NULL |
| holiday_date | DATE | NOT NULL |

---

## Foreign Key Relationships

| FK Constraint | Source Table | Source Column | References |
|--------------|-------------|---------------|------------|
| fk_user_manager | users | manager_id | users(id) |
| fk_leave_employee | leave_requests | employee_id | users(id) |
| fk_balance_employee | leave_balances | employee_id | users(id) |
| fk_goal_employee | goals | employee_id | users(id) |
| fk_review_employee | performance_reviews | employee_id | users(id) |
| fk_notif_user | notifications | user_id | users(id) |

> The `users` table is the central entity — all other tables reference it via foreign keys.

---

## Indexes (13 Total)

| Index Name | Table | Column |
|-----------|-------|--------|
| idx_users_email | users | email |
| idx_users_role | users | role |
| idx_users_manager | users | manager_id |
| idx_users_department | users | department |
| idx_users_active | users | active |
| idx_leave_req_emp | leave_requests | employee_id |
| idx_leave_req_status | leave_requests | status |
| idx_leave_bal_emp | leave_balances | employee_id |
| idx_goals_emp | goals | employee_id |
| idx_reviews_emp | performance_reviews | employee_id |
| idx_reviews_status | performance_reviews | status |
| idx_notif_user | notifications | user_id |
| idx_notif_read | notifications | is_read |

---

## Stored Procedures (28 Total)

### User Management (6)
| Procedure | Description |
|-----------|-------------|
| `sp_register_user` | Creates a new user, returns new user ID |
| `sp_update_user` | Admin-level update (includes role/salary) |
| `sp_update_profile` | Self-service update (phone, address, emergency contact) |
| `sp_deactivate_user` | Soft-deletes user (active = 0) |
| `sp_reactivate_user` | Restores user (active = 1) |
| `sp_assign_manager` | Assigns a manager to an employee |

### Leave Management (6)
| Procedure | Description |
|-----------|-------------|
| `sp_apply_leave` | Creates leave request + deducts balance |
| `sp_cancel_leave` | Cancels pending request + restores balance |
| `sp_approve_leave` | Approves leave with manager comment |
| `sp_reject_leave` | Rejects leave + restores balance |
| `sp_adjust_leave_balance` | Creates or updates leave balance |
| `sp_init_leave_balance` | Initializes leave balance for new employee |

### Leave Types (3)
| Procedure | Description |
|-----------|-------------|
| `sp_add_leave_type` | Adds a new leave type |
| `sp_update_leave_type` | Updates name and default days |
| `sp_delete_leave_type` | Deletes a leave type |

### Goals (4)
| Procedure | Description |
|-----------|-------------|
| `sp_create_goal` | Creates a goal for an employee |
| `sp_update_goal_progress` | Updates progress percentage |
| `sp_add_goal_comment` | Manager adds comment |
| `sp_delete_goal` | Deletes a goal |

### Performance Reviews (3)
| Procedure | Description |
|-----------|-------------|
| `sp_create_review` | Creates draft review with self-assessment |
| `sp_submit_review` | DRAFT → SUBMITTED |
| `sp_provide_feedback` | Manager rates + feedback → REVIEWED |

### Departments (3)
| Procedure | Description |
|-----------|-------------|
| `sp_add_department` | Adds a department |
| `sp_update_department` | Updates department name |
| `sp_delete_department` | Deletes a department |

### Announcements (3)
| Procedure | Description |
|-----------|-------------|
| `sp_add_announcement` | Creates an announcement |
| `sp_update_announcement` | Updates title/description |
| `sp_delete_announcement` | Deletes an announcement |

### Notifications (3)
| Procedure | Description |
|-----------|-------------|
| `sp_create_notification` | Creates notification for a user |
| `sp_mark_notification_read` | Marks one as read |
| `sp_mark_all_notifications_read` | Marks all user notifications as read |

### Holidays (2)
| Procedure | Description |
|-----------|-------------|
| `sp_add_holiday` | Adds a holiday |
| `sp_delete_holiday` | Deletes a holiday |

---

## Seed Data

| Data | Count | Details |
|------|-------|---------|
| Departments | 5 | Engineering, Human Resources, Finance, Marketing, Operations |
| Leave Types | 3 | Casual Leave (12d), Sick Leave (10d), Paid Leave (15d) |
| Users | 5 | 1 Admin, 1 Manager, 3 Employees |
| Leave Balances | 15 | 3 per user (one per leave type) |
| Holidays | 6 | Republic Day, Holi, Independence Day, Gandhi Jayanti, Diwali, Christmas |
| Announcements | 2 | Welcome message, Performance review cycle notice |

### Default Login Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@revworkforce.com | admin123 |
| Manager | manager@revworkforce.com | manager123 |
| Employee | employee@revworkforce.com | employee123 |

---

## Connection Configuration

```properties
# File: src/main/resources/application.properties
spring.datasource.url=jdbc:oracle:thin:@//db.freesql.com:1521/26ai_un3c1
spring.datasource.username=PRAKASHTUSHRA924_SCHEMA_3S765
spring.datasource.password=ZYL4IZ736H1YHG6u5!0O1BUFTZ55Y7
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
```

> **Note:** The project uses **Spring JDBC** (not JPA/Hibernate). All database operations go through the PL/SQL stored procedures via `JdbcTemplate` / `SimpleJdbcCall`.
