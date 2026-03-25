package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.LeaveType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class LeaveTypeDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<LeaveType> rowMapper = (rs, rowNum) -> LeaveType.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .defaultDays(rs.getInt("default_days"))
            .build();

    public List<LeaveType> findAll() {
        return jdbcTemplate.query("SELECT * FROM leave_types ORDER BY id", rowMapper);
    }

    public Optional<LeaveType> findById(Long id) {
        List<LeaveType> list = jdbcTemplate.query(
                "SELECT * FROM leave_types WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM leave_types WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }

    public LeaveType save(LeaveType leaveType) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_leave_type");
        Map<String, Object> params = new HashMap<>();
        params.put("p_name", leaveType.getName());
        params.put("p_default_days", leaveType.getDefaultDays());
        Map<String, Object> result = jdbcCall.execute(params);
        leaveType.setId(((Number) result.get("P_ID")).longValue());
        return leaveType;
    }

    public void update(Long id, String name, int defaultDays) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_leave_type");
        Map<String, Object> params = new HashMap<>();
        params.put("p_id", id);
        params.put("p_name", name);
        params.put("p_default_days", defaultDays);
        jdbcCall.execute(params);
    }

    public void delete(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_delete_leave_type")
                .execute(Map.of("p_id", id));
    }
}
