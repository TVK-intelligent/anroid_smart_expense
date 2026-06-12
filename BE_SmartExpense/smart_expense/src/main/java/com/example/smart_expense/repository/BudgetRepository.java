package com.example.smart_expense.repository;

import com.example.smart_expense.model.Budget;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class BudgetRepository {

    private final JdbcTemplate jdbcTemplate;

    public BudgetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Budget> budgetRowMapper = (rs, rowNum) -> {
        Date startDate = rs.getDate("start_date");
        Date endDate = rs.getDate("end_date");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return Budget.builder()
                .budgetId(rs.getInt("budget_id"))
                .userId(rs.getInt("user_id"))
                .categoryId(rs.getInt("category_id"))
                .amount(rs.getBigDecimal("amount"))
                .startDate(startDate != null ? startDate.toLocalDate() : null)
                .endDate(endDate != null ? endDate.toLocalDate() : null)
                .alertThreshold(rs.getBigDecimal("alert_threshold"))
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .build();
    };

    public Budget save(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, category_id, amount, start_date, end_date, alert_threshold, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, budget.getUserId());
            ps.setInt(2, budget.getCategoryId());
            ps.setBigDecimal(3, budget.getAmount());
            ps.setDate(4, Date.valueOf(budget.getStartDate()));
            ps.setDate(5, Date.valueOf(budget.getEndDate()));
            ps.setBigDecimal(6, budget.getAlertThreshold());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            budget.setBudgetId(keyHolder.getKey().intValue());
        }
        return budget;
    }

    public Optional<Budget> findById(Integer budgetId) {
        String sql = "SELECT * FROM budgets WHERE budget_id = ?";
        try {
            Budget budget = jdbcTemplate.queryForObject(sql, budgetRowMapper, budgetId);
            return Optional.ofNullable(budget);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Budget> findByUserId(Integer userId) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? ORDER BY start_date DESC";
        return jdbcTemplate.query(sql, budgetRowMapper, userId);
    }

    /**
     * Tìm ngân sách hoạt động hiện tại của người dùng đối với danh mục cụ thể.
     */
    public Optional<Budget> findActiveBudgetByCategory(Integer userId, Integer categoryId) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category_id = ? AND CURDATE() BETWEEN start_date AND end_date LIMIT 1";
        try {
            Budget budget = jdbcTemplate.queryForObject(sql, budgetRowMapper, userId, categoryId);
            return Optional.ofNullable(budget);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả ngân sách đang hoạt động trong ngày hôm nay của người dùng.
     */
    public List<Budget> findActiveBudgets(Integer userId) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND CURDATE() BETWEEN start_date AND end_date";
        return jdbcTemplate.query(sql, budgetRowMapper, userId);
    }
}
