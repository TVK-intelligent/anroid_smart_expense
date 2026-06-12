package com.example.smart_expense.repository;

import com.example.smart_expense.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Notification> notificationRowMapper = (rs, rowNum) -> {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return Notification.builder()
                .notificationId(rs.getInt("notification_id"))
                .userId(rs.getInt("user_id"))
                .title(rs.getString("title"))
                .content(rs.getString("content"))
                .isRead(rs.getBoolean("is_read"))
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .build();
    };

    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, title, content, is_read, created_at) VALUES (?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getTitle());
            ps.setString(3, notification.getContent());
            ps.setBoolean(4, notification.getIsRead() != null && notification.getIsRead());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            notification.setNotificationId(keyHolder.getKey().intValue());
        }
        return notification;
    }

    public List<Notification> findByUserId(Integer userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, notificationRowMapper, userId);
    }

    public void markAsRead(Integer notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
        jdbcTemplate.update(sql, notificationId);
    }

    public void markAllAsReadForUser(Integer userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
