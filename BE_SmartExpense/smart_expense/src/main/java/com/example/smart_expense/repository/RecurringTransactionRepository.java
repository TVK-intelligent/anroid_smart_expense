package com.example.smart_expense.repository;

import com.example.smart_expense.model.RecurringTransaction;
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

@Repository
public class RecurringTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecurringTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RecurringTransaction> recurringRowMapper = (rs, rowNum) -> RecurringTransaction.builder()
            .recurringId(rs.getInt("recurring_id"))
            .userId(rs.getInt("user_id"))
            .walletId(rs.getInt("wallet_id"))
            .categoryId(rs.getInt("category_id"))
            .amount(rs.getBigDecimal("amount"))
            .frequency(rs.getString("frequency"))
            .nextDueDate(rs.getDate("next_due_date").toLocalDate())
            .note(rs.getString("note"))
            .isActive(rs.getBoolean("is_active"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public RecurringTransaction save(RecurringTransaction rt) {
        String sql = "INSERT INTO recurring_transactions (user_id, wallet_id, category_id, amount, frequency, next_due_date, note, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, rt.getUserId());
            ps.setInt(2, rt.getWalletId());
            ps.setInt(3, rt.getCategoryId());
            ps.setBigDecimal(4, rt.getAmount());
            ps.setString(5, rt.getFrequency());
            ps.setDate(6, Date.valueOf(rt.getNextDueDate()));
            ps.setString(7, rt.getNote());
            ps.setBoolean(8, rt.getIsActive() == null || rt.getIsActive());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            rt.setRecurringId(keyHolder.getKey().intValue());
        }
        return rt;
    }

    public List<RecurringTransaction> findByUserId(Integer userId) {
        String sql = "SELECT * FROM recurring_transactions WHERE user_id = ? ORDER BY next_due_date ASC";
        return jdbcTemplate.query(sql, recurringRowMapper, userId);
    }

    /**
     * Tính tổng chi phí cố định định kỳ hàng tháng của người dùng.
     * Sử dụng cho thuật toán gợi ý trích tích lũy (Savings Allocator).
     */
    public BigDecimal getTotalExpectedFixedExpensesForMonth(Integer userId) {
        String sql = "SELECT COALESCE(SUM(rt.amount), 0) FROM recurring_transactions rt " +
                     "JOIN categories c ON rt.category_id = c.category_id " +
                     "WHERE rt.user_id = ? AND rt.is_active = TRUE AND c.type = 'EXPENSE'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
    }

    /**
     * Tính tổng thu nhập cố định định kỳ hàng tháng của người dùng.
     */
    public BigDecimal getTotalExpectedFixedIncomesForMonth(Integer userId) {
        String sql = "SELECT COALESCE(SUM(rt.amount), 0) FROM recurring_transactions rt " +
                     "JOIN categories c ON rt.category_id = c.category_id " +
                     "WHERE rt.user_id = ? AND rt.is_active = TRUE AND c.type = 'INCOME'";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
    }
}
