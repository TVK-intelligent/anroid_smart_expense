package com.example.smart_expense.repository;

import com.example.smart_expense.model.SavingsGoal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class SavingsGoalRepository {

    private final JdbcTemplate jdbcTemplate;

    public SavingsGoalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SavingsGoal> goalRowMapper = (rs, rowNum) -> SavingsGoal.builder()
            .goalId(rs.getInt("goal_id"))
            .userId(rs.getInt("user_id"))
            .goalName(rs.getString("goal_name"))
            .targetAmount(rs.getBigDecimal("target_amount"))
            .currentAmount(rs.getBigDecimal("current_amount"))
            .walletId(rs.getObject("wallet_id") != null ? rs.getInt("wallet_id") : null)
            .deadline(rs.getDate("deadline").toLocalDate())
            .status(rs.getString("status"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public SavingsGoal save(SavingsGoal goal) {
        String sql = "INSERT INTO savings_goals (user_id, goal_name, target_amount, current_amount, wallet_id, deadline, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, goal.getUserId());
            ps.setString(2, goal.getGoalName());
            ps.setBigDecimal(3, goal.getTargetAmount());
            ps.setBigDecimal(4, goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO);
            if (goal.getWalletId() != null) {
                ps.setInt(5, goal.getWalletId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setDate(6, Date.valueOf(goal.getDeadline()));
            ps.setString(7, goal.getStatus() != null ? goal.getStatus() : "IN_PROGRESS");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            goal.setGoalId(keyHolder.getKey().intValue());
        }
        return goal;
    }

    public Optional<SavingsGoal> findById(Integer goalId) {
        String sql = "SELECT * FROM savings_goals WHERE goal_id = ?";
        try {
            SavingsGoal goal = jdbcTemplate.queryForObject(sql, goalRowMapper, goalId);
            return Optional.ofNullable(goal);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<SavingsGoal> findByUserId(Integer userId) {
        String sql = "SELECT * FROM savings_goals WHERE user_id = ? ORDER BY deadline ASC";
        return jdbcTemplate.query(sql, goalRowMapper, userId);
    }

    /**
     * Lấy các mục tiêu tích lũy chưa hoàn thành, ưu tiên deadline gần nhất.
     * Sử dụng cho thuật toán Phân bổ tiết kiệm thông minh (Savings Allocator).
     */
    public List<SavingsGoal> findActiveGoalsOrderByDeadline(Integer userId) {
        String sql = "SELECT * FROM savings_goals WHERE user_id = ? AND status = 'IN_PROGRESS' ORDER BY deadline ASC";
        return jdbcTemplate.query(sql, goalRowMapper, userId);
    }

    public void updateCurrentAmount(Integer goalId, BigDecimal amountChange) {
        String sql = "UPDATE savings_goals SET current_amount = current_amount + ? WHERE goal_id = ?";
        jdbcTemplate.update(sql, amountChange, goalId);
        
        // Kiểm tra xem mục tiêu đã hoàn thành chưa để tự động cập nhật status
        String updateStatusSql = "UPDATE savings_goals SET status = 'COMPLETED' WHERE goal_id = ? AND current_amount >= target_amount";
        jdbcTemplate.update(updateStatusSql, goalId);
    }

    public SavingsGoal update(SavingsGoal goal) {
        String sql = "UPDATE savings_goals SET goal_name = ?, target_amount = ?, current_amount = ?, wallet_id = ?, deadline = ?, status = ? WHERE goal_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getWalletId(),
                Date.valueOf(goal.getDeadline()),
                goal.getStatus(),
                goal.getGoalId(),
                goal.getUserId());
        return goal;
    }
}
