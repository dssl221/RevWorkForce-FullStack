package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.Goal;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class GoalDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<Goal> rowMapper = (rs, rowNum) -> Goal.builder()
            .id(rs.getLong("id"))
            .employeeId(rs.getLong("employee_id"))
            .description(rs.getString("description"))
            .deadline(rs.getDate("deadline") != null ? rs.getDate("deadline").toLocalDate() : null)
            .priority(rs.getString("priority"))
            .progress(rs.getInt("progress"))
            .managerComment(rs.getString("manager_comment"))
            .build();

    public Goal save(Goal goal) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_create_goal");
        Map<String, Object> params = new HashMap<>();
        params.put("p_employee_id", goal.getEmployeeId());
        params.put("p_description", goal.getDescription());
        params.put("p_deadline", goal.getDeadline() != null ? java.sql.Date.valueOf(goal.getDeadline()) : null);
        params.put("p_priority", goal.getPriority());

        Map<String, Object> result = jdbcCall.execute(params);
        goal.setId(((Number) result.get("P_GOAL_ID")).longValue());
        return goal;
    }

    public Optional<Goal> findById(Long id) {
        List<Goal> list = jdbcTemplate.query("SELECT * FROM goals WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Goal> findByEmployeeId(Long employeeId) {
        return jdbcTemplate.query(
                "SELECT * FROM goals WHERE employee_id = ? ORDER BY id DESC", rowMapper, employeeId);
    }

    public List<Goal> findByEmployeeIdIn(List<Long> employeeIds) {
        if (employeeIds.isEmpty())
            return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(employeeIds.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM goals WHERE employee_id IN (" + placeholders + ") ORDER BY id DESC",
                rowMapper, employeeIds.toArray());
    }

    public void updateProgress(Long goalId, int progress) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_update_goal_progress")
                .execute(Map.of("p_goal_id", goalId, "p_progress", progress));
    }

    public void addComment(Long goalId, String comment) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_add_goal_comment")
                .execute(Map.of("p_goal_id", goalId, "p_comment", comment));
    }

    public void delete(Long goalId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_delete_goal")
                .execute(Map.of("p_goal_id", goalId));
    }
}
