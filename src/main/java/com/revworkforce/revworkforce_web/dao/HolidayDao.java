package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.Holiday;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class HolidayDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<Holiday> rowMapper = (rs, rowNum) -> Holiday.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .holidayDate(rs.getDate("holiday_date").toLocalDate())
            .build();

    public List<Holiday> findAll() {
        return jdbcTemplate.query("SELECT * FROM holidays ORDER BY holiday_date", rowMapper);
    }

    public Holiday save(Holiday holiday) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_holiday");
        Map<String, Object> params = new HashMap<>();
        params.put("p_name", holiday.getName());
        params.put("p_date", java.sql.Date.valueOf(holiday.getHolidayDate()));
        Map<String, Object> result = jdbcCall.execute(params);
        holiday.setId(((Number) result.get("P_ID")).longValue());
        return holiday;
    }

    public void delete(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_delete_holiday")
                .execute(Map.of("p_id", id));
    }
}
