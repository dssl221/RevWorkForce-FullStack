package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.LeaveBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class LeaveBalanceDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<LeaveBalance> rowMapper = (rs, rowNum) -> LeaveBalance.builder()
            .id(rs.getLong("id"))
            .employeeId(rs.getLong("employee_id"))
            .leaveType(rs.getString("leave_type"))
            .totalDays(rs.getInt("total_days"))
            .usedDays(rs.getInt("used_days"))
            .build();

    public List<LeaveBalance> findByEmployeeId(Long employeeId) {
        return jdbcTemplate.query(
                "SELECT * FROM leave_balances WHERE employee_id = ?", rowMapper, employeeId);
    }

    public Optional<LeaveBalance> findByEmployeeIdAndLeaveType(Long employeeId, String leaveType) {
        List<LeaveBalance> list = jdbcTemplate.query(
                "SELECT * FROM leave_balances WHERE employee_id = ? AND leave_type = ?",
                rowMapper, employeeId, leaveType);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void save(LeaveBalance balance) {
        Optional<LeaveBalance> existing = findByEmployeeIdAndLeaveType(balance.getEmployeeId(), balance.getLeaveType());
        if (existing.isPresent()) {
            // If balance row exists, update to avoid duplicates
            adjustBalance(balance.getEmployeeId(), balance.getLeaveType(), balance.getTotalDays());
        } else {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                    .withProcedureName("sp_init_leave_balance");
            Map<String, Object> params = new HashMap<>();
            params.put("p_employee_id", balance.getEmployeeId());
            params.put("p_leave_type", balance.getLeaveType());
            params.put("p_total_days", balance.getTotalDays());
            jdbcCall.execute(params);
        }
    }

    public void adjustBalance(Long employeeId, String leaveType, int totalDays) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_adjust_leave_balance");
        Map<String, Object> params = new HashMap<>();
        params.put("p_employee_id", employeeId);
        params.put("p_leave_type", leaveType);
        params.put("p_total_days", totalDays);
        jdbcCall.execute(params);
    }
}
