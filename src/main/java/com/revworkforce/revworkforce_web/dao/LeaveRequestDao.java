package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.LeaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class LeaveRequestDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<LeaveRequest> rowMapper = (rs, rowNum) -> LeaveRequest.builder()
            .id(rs.getLong("id"))
            .employeeId(rs.getLong("employee_id"))
            .leaveType(rs.getString("leave_type"))
            .startDate(rs.getDate("start_date").toLocalDate())
            .endDate(rs.getDate("end_date").toLocalDate())
            .reason(rs.getString("reason"))
            .status(LeaveRequest.LeaveStatus.valueOf(rs.getString("status")))
            .managerComment(rs.getString("manager_comment"))
            .appliedDate(rs.getDate("applied_date") != null ? rs.getDate("applied_date").toLocalDate() : null)
            .build();

    public LeaveRequest save(LeaveRequest request) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_apply_leave");
        Map<String, Object> params = new HashMap<>();
        params.put("p_employee_id", request.getEmployeeId());
        params.put("p_leave_type", request.getLeaveType());
        params.put("p_start_date", java.sql.Date.valueOf(request.getStartDate()));
        params.put("p_end_date", java.sql.Date.valueOf(request.getEndDate()));
        params.put("p_reason", request.getReason());

        Map<String, Object> result = jdbcCall.execute(params);
        Long leaveId = ((Number) result.get("P_LEAVE_ID")).longValue();
        request.setId(leaveId);
        request.setStatus(LeaveRequest.LeaveStatus.PENDING);
        return request;
    }

    public Optional<LeaveRequest> findById(Long id) {
        List<LeaveRequest> list = jdbcTemplate.query(
                "SELECT * FROM leave_requests WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<LeaveRequest> findByEmployeeId(Long employeeId) {
        return jdbcTemplate.query(
                "SELECT * FROM leave_requests WHERE employee_id = ? ORDER BY applied_date DESC",
                rowMapper, employeeId);
    }

    public List<LeaveRequest> findByEmployeeIdIn(List<Long> employeeIds) {
        if (employeeIds.isEmpty())
            return Collections.emptyList();
        String placeholders = String.join(",", Collections.nCopies(employeeIds.size(), "?"));
        return jdbcTemplate.query(
                "SELECT * FROM leave_requests WHERE employee_id IN (" + placeholders + ") ORDER BY applied_date DESC",
                rowMapper, employeeIds.toArray());
    }

    public List<LeaveRequest> findAll() {
        return jdbcTemplate.query("SELECT * FROM leave_requests ORDER BY applied_date DESC", rowMapper);
    }

    public void cancelLeave(Long leaveId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_cancel_leave")
                .execute(Map.of("p_leave_id", leaveId));
    }

    public void approveLeave(Long leaveId, String comment, long days) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_approve_leave");
        Map<String, Object> params = new HashMap<>();
        params.put("p_leave_id", leaveId);
        params.put("p_comment", comment);
        params.put("p_days", days);
        jdbcCall.execute(params);
    }

    public void rejectLeave(Long leaveId, String comment) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_reject_leave");
        Map<String, Object> params = new HashMap<>();
        params.put("p_leave_id", leaveId);
        params.put("p_comment", comment);
        jdbcCall.execute(params);
    }

    public long countByStatus(String status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM leave_requests WHERE status = ?", Long.class, status);
        return count != null ? count : 0;
    }
}
