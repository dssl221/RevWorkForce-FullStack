package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class AnnouncementDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<Announcement> rowMapper = (rs, rowNum) -> Announcement.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .description(rs.getString("description"))
            .createdDate(rs.getDate("created_date") != null ? rs.getDate("created_date").toLocalDate() : null)
            .build();

    public List<Announcement> findAll() {
        return jdbcTemplate.query("SELECT * FROM announcements ORDER BY created_date DESC", rowMapper);
    }

    public Optional<Announcement> findById(Long id) {
        List<Announcement> list = jdbcTemplate.query(
                "SELECT * FROM announcements WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Announcement save(Announcement announcement) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_add_announcement");
        Map<String, Object> params = new HashMap<>();
        params.put("p_title", announcement.getTitle());
        params.put("p_description", announcement.getDescription());
        Map<String, Object> result = jdbcCall.execute(params);
        announcement.setId(((Number) result.get("P_ID")).longValue());
        return announcement;
    }

    public void update(Long id, String title, String description) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_announcement");
        Map<String, Object> params = new HashMap<>();
        params.put("p_id", id);
        params.put("p_title", title);
        params.put("p_description", description);
        jdbcCall.execute(params);
    }

    public void delete(Long id) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_delete_announcement")
                .execute(Map.of("p_id", id));
    }
}
