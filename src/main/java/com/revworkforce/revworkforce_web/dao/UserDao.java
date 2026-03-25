package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .employeeId(rs.getString("employee_id"))
            .role(User.Role.valueOf(rs.getString("role")))
            .department(rs.getString("department"))
            .designation(rs.getString("designation"))
            .managerId(rs.getObject("manager_id") != null ? rs.getLong("manager_id") : null)
            .phone(rs.getString("phone"))
            .address(rs.getString("address"))
            .emergencyContact(rs.getString("emergency_contact"))
            .joiningDate(rs.getDate("joining_date") != null ? rs.getDate("joining_date").toLocalDate() : null)
            .active(rs.getInt("active") == 1)
            .salary(rs.getObject("salary") != null ? rs.getDouble("salary") : null)
            .build();

    public User save(User user) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_register_user");
        Map<String, Object> params = new HashMap<>();
        params.put("p_name", user.getName());
        params.put("p_email", user.getEmail());
        params.put("p_password", user.getPassword());
        params.put("p_employee_id", user.getEmployeeId());
        params.put("p_role", user.getRole().name());
        params.put("p_department", user.getDepartment());
        params.put("p_designation", user.getDesignation());
        params.put("p_manager_id", user.getManagerId());
        params.put("p_phone", user.getPhone());
        params.put("p_address", user.getAddress());
        params.put("p_emergency_contact", user.getEmergencyContact());
        params.put("p_joining_date",
                user.getJoiningDate() != null ? java.sql.Date.valueOf(user.getJoiningDate()) : null);
        params.put("p_salary", user.getSalary());

        Map<String, Object> result = jdbcCall.execute(params);
        Long userId = ((Number) result.get("P_USER_ID")).longValue();
        user.setId(userId);
        return user;
    }

    public void update(User user) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_user");
        Map<String, Object> params = new HashMap<>();
        params.put("p_id", user.getId());
        params.put("p_name", user.getName());
        params.put("p_department", user.getDepartment());
        params.put("p_designation", user.getDesignation());
        params.put("p_phone", user.getPhone());
        params.put("p_address", user.getAddress());
        params.put("p_emergency_contact", user.getEmergencyContact());
        params.put("p_manager_id", user.getManagerId());
        params.put("p_role", user.getRole() != null ? user.getRole().name() : null);
        params.put("p_salary", user.getSalary());
        jdbcCall.execute(params);
    }

    public void updateProfile(Long id, String phone, String address, String emergencyContact) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_profile");
        Map<String, Object> params = new HashMap<>();
        params.put("p_id", id);
        params.put("p_phone", phone);
        params.put("p_address", address);
        params.put("p_emergency_contact", emergencyContact);
        jdbcCall.execute(params);
    }

    public void deactivate(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_deactivate_user")
                .execute(Map.of("p_id", id));
    }

    public void reactivate(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_reactivate_user")
                .execute(Map.of("p_id", id));
    }

    public void assignManager(Long employeeId, Long managerId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_assign_manager")
                .execute(Map.of("p_employee_id", employeeId, "p_manager_id", managerId));
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE email = ?", userRowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", userRowMapper);
    }

    public List<User> findByManagerId(Long managerId) {
        return jdbcTemplate.query("SELECT * FROM users WHERE manager_id = ?", userRowMapper, managerId);
    }

    public List<User> searchUsers(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? " +
                        "OR LOWER(department) LIKE ? OR LOWER(designation) LIKE ? OR LOWER(employee_id) LIKE ?",
                userRowMapper, searchPattern, searchPattern, searchPattern, searchPattern, searchPattern);
    }

    public long countByActive(boolean active) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE active = ?", Long.class, active ? 1 : 0);
        return count != null ? count : 0;
    }

    public long countByRole(User.Role role) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = ?", Long.class, role.name());
        return count != null ? count : 0;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return count != null ? count : 0;
    }
}
