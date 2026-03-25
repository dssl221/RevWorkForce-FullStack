package com.revworkforce.revworkforce_web.dao;

import com.revworkforce.revworkforce_web.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class NotificationDao {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    private final RowMapper<Notification> rowMapper = (rs, rowNum) -> Notification.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .message(rs.getString("message"))
            .read(rs.getInt("is_read") == 1)
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
            .build();

    public List<Notification> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC",
                rowMapper, userId);
    }

    public List<Notification> findUnreadByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC",
                rowMapper, userId);
    }

    public long countUnread(Long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0",
                Long.class, userId);
        return count != null ? count : 0;
    }

    public Notification save(Long userId, String message) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_create_notification");
        Map<String, Object> params = new HashMap<>();
        params.put("p_user_id", userId);
        params.put("p_message", message);
        Map<String, Object> result = jdbcCall.execute(params);
        Long notifId = ((Number) result.get("P_NOTIF_ID")).longValue();
        return Notification.builder()
                .id(notifId)
                .userId(userId)
                .message(message)
                .read(false)
                .build();
    }

    public void markRead(Long notifId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_mark_notification_read")
                .execute(Map.of("p_notif_id", notifId));
    }

    public void markAllRead(Long userId) {
        new SimpleJdbcCall(dataSource).withProcedureName("sp_mark_all_notifications_read")
                .execute(Map.of("p_user_id", userId));
    }
}
