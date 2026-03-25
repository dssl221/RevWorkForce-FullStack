package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.Department;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DepartmentDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<Department> rowMapper = (rs, rowNum) -> Department.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .build();

    public List<Department> findAll() {
        return jdbcTemplate.query("SELECT * FROM departments ORDER BY name", rowMapper);
    }

    public Optional<Department> findById(Long id) {
        List<Department> list = jdbcTemplate.query(
                "SELECT * FROM departments WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Department save(Department department) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_department");
        Map<String, Object> params = new HashMap<>();
        params.put("p_name", department.getName());
        Map<String, Object> result = jdbcCall.execute(params);
        department.setId(((Number) result.get("P_ID")).longValue());
        return department;
    }

    public void update(Long id, String name) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_update_department")
                .execute(Map.of("p_id", id, "p_name", name));
    }

    public void delete(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_delete_department")
                .execute(Map.of("p_id", id));
    }
}
