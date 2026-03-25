package com.revworkforce.revworkforce_web.config;

import com.revworkforce.revworkforce_web.dao.*;
import com.revworkforce.revworkforce_web.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    private final LeaveTypeDao leaveTypeDao;
    private final LeaveBalanceDao leaveBalanceDao;
    private final HolidayDao holidayDao;
    private final AnnouncementDao announcementDao;


    @Override
    public void run(String... args) {
        try {
            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
            if (userCount != null && userCount > 0) {
                log.info("Data already seeded. Cleaning up duplicates and migrating passwords...");
                // Clean up duplicate leave balances
                try {
                    jdbcTemplate.update("DELETE FROM leave_balances WHERE ROWID NOT IN (SELECT MIN(ROWID) FROM leave_balances GROUP BY employee_id, leave_type)");
                    log.info("Duplicate leave balances cleaned up.");
                } catch (Exception e) {
                    log.warn("Failed to clean up leave balance duplicates: " + e.getMessage());
                }
                // Clean up duplicate announcements
                try {
                    jdbcTemplate.update("DELETE FROM announcements WHERE ROWID NOT IN (SELECT MIN(ROWID) FROM announcements GROUP BY title, description)");
                    log.info("Duplicate announcements cleaned up.");
                } catch (Exception e) {
                    log.warn("Failed to clean up announcement duplicates: " + e.getMessage());
                }
                // Clean up duplicate holidays
                try {
                    jdbcTemplate.update("DELETE FROM holidays WHERE ROWID NOT IN (SELECT MIN(ROWID) FROM holidays GROUP BY name, holiday_date)");
                    log.info("Duplicate holidays cleaned up.");
                } catch (Exception e) {
                    log.warn("Failed to clean up holiday duplicates: " + e.getMessage());
                }
                // Migrate BCrypt hashed passwords to plain text
                try {
                    jdbcTemplate.update("UPDATE users SET password = 'admin123' WHERE email = 'admin@revworkforce.com' AND password LIKE '$2a$%'");
                    jdbcTemplate.update("UPDATE users SET password = 'manager123' WHERE email = 'manager@revworkforce.com' AND password LIKE '$2a$%'");
                    jdbcTemplate.update("UPDATE users SET password = 'employee123' WHERE email = 'employee@revworkforce.com' AND password LIKE '$2a$%'");
                    jdbcTemplate.update("UPDATE users SET password = 'jane123' WHERE email = 'jane@revworkforce.com' AND password LIKE '$2a$%'");
                    jdbcTemplate.update("UPDATE users SET password = 'bob123' WHERE email = 'bob@revworkforce.com' AND password LIKE '$2a$%'");
                    log.info("Password migration completed.");
                } catch (Exception ex) {
                    log.warn("Password migration skipped: " + ex.getMessage());
                }
                return;
            }
        } catch (Exception e) {
            log.warn("Tables may not exist yet. Attempting to seed data...");
        }

        log.info("=== Seeding initial data ===");

        try {
            // Departments
            if (departmentDao.findAll().isEmpty()) {
                departmentDao.save(Department.builder().name("Engineering").build());
                departmentDao.save(Department.builder().name("Human Resources").build());
                departmentDao.save(Department.builder().name("Marketing").build());
                departmentDao.save(Department.builder().name("Finance").build());
                departmentDao.save(Department.builder().name("Operations").build());
                log.info("Departments seeded.");
            }

            // Leave Types
            if (leaveTypeDao.findAll().isEmpty()) {
                leaveTypeDao.save(LeaveType.builder().name("Casual Leave").defaultDays(12).build());
                leaveTypeDao.save(LeaveType.builder().name("Sick Leave").defaultDays(10).build());
                leaveTypeDao.save(LeaveType.builder().name("Paid Leave").defaultDays(15).build());
                leaveTypeDao.save(LeaveType.builder().name("Maternity Leave").defaultDays(180).build());
                log.info("Leave types seeded.");
            }

            // Users
            if (userDao.findAll().isEmpty()) {
                User admin = User.builder()
                        .name("Admin User").email("admin@revworkforce.com")
                        .password("admin123")
                        .employeeId("EMP00001").role(User.Role.ADMIN)
                        .department("Human Resources").designation("HR Director")
                        .phone("9876543210").joiningDate(LocalDate.of(2023, 1, 1))
                        .active(true).salary(120000.0).build();
                admin = userDao.save(admin);

                User manager = User.builder()
                        .name("Manager User").email("manager@revworkforce.com")
                        .password("manager123")
                        .employeeId("EMP00002").role(User.Role.MANAGER)
                        .department("Engineering").designation("Engineering Manager")
                        .phone("9876543211").joiningDate(LocalDate.of(2023, 3, 15))
                        .managerId(admin.getId()).active(true).salary(100000.0).build();
                manager = userDao.save(manager);

                User employee1 = User.builder()
                        .name("John Employee").email("employee@revworkforce.com")
                        .password("employee123")
                        .employeeId("EMP00003").role(User.Role.EMPLOYEE)
                        .department("Engineering").designation("Software Developer")
                        .phone("9876543212").joiningDate(LocalDate.of(2023, 6, 1))
                        .managerId(manager.getId()).active(true).salary(60000.0).build();
                employee1 = userDao.save(employee1);

                User employee2 = User.builder()
                        .name("Jane Developer").email("jane@revworkforce.com")
                        .password("jane123")
                        .employeeId("EMP00004").role(User.Role.EMPLOYEE)
                        .department("Engineering").designation("Senior Developer")
                        .phone("9876543213").joiningDate(LocalDate.of(2023, 8, 10))
                        .managerId(manager.getId()).active(true).salary(80000.0).build();
                employee2 = userDao.save(employee2);

                User employee3 = User.builder()
                        .name("Bob Analyst").email("bob@revworkforce.com")
                        .password("bob123")
                        .employeeId("EMP00005").role(User.Role.EMPLOYEE)
                        .department("Marketing").designation("Marketing Analyst")
                        .phone("9876543214").joiningDate(LocalDate.of(2024, 1, 15))
                        .managerId(admin.getId()).active(true).salary(55000.0).build();
                employee3 = userDao.save(employee3);

                // Initialize leave balances
                List<LeaveType> types = leaveTypeDao.findAll();
                for (User u : List.of(admin, manager, employee1, employee2, employee3)) {
                    for (LeaveType lt : types) {
                        leaveBalanceDao.save(LeaveBalance.builder()
                                .employeeId(u.getId())
                                .leaveType(lt.getName())
                                .totalDays(lt.getDefaultDays())
                                .usedDays(0).build());
                    }
                }
                log.info("Users and leave balances seeded.");
            }

            // Holidays
            if (holidayDao.findAll().isEmpty()) {
                holidayDao.save(Holiday.builder().name("Republic Day").holidayDate(LocalDate.of(2026, 1, 26)).build());
                holidayDao.save(Holiday.builder().name("Holi").holidayDate(LocalDate.of(2026, 3, 17)).build());
                holidayDao.save(
                        Holiday.builder().name("Independence Day").holidayDate(LocalDate.of(2026, 8, 15)).build());
                holidayDao
                        .save(Holiday.builder().name("Gandhi Jayanti").holidayDate(LocalDate.of(2026, 10, 2)).build());
                holidayDao.save(Holiday.builder().name("Diwali").holidayDate(LocalDate.of(2026, 10, 20)).build());
                holidayDao.save(Holiday.builder().name("Christmas").holidayDate(LocalDate.of(2026, 12, 25)).build());
                log.info("Holidays seeded.");
            }

            // Announcements
            if (announcementDao.findAll().isEmpty()) {
                announcementDao.save(Announcement.builder()
                        .title("Welcome to RevWorkForce!")
                        .description(
                                "Our new HRM system is now live. Explore all features including leave management, goal tracking, and performance reviews.")
                        .build());
                announcementDao.save(Announcement.builder()
                        .title("Q1 Performance Review Cycle")
                        .description(
                                "The Q1 2026 performance review cycle begins on April 1st. Please ensure all goals are updated.")
                        .build());
                log.info("Announcements seeded.");
            }

            log.info("=== Data seeding complete ===");
        } catch (Exception e) {
            log.error("Error during data initialization: " + e.getMessage(), e);
        }
    }
}
