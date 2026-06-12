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
import java.sql.Timestamp;
import java.util.List;

@Repository
public class RecurringTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public RecurringTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RecurringTransaction> recurringRowMapper = (rs, rowNum) -> {
        Date nextDueDate = rs.getDate("next_due_date");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return RecurringTransaction.builder()
                .recurringId(rs.getInt("recurring_id"))
                .userId(rs.getInt("user_id"))
                .walletId(rs.getInt("wallet_id"))
                .categoryId(rs.getInt("category_id"))
                .amount(rs.getBigDecimal("amount"))
                .frequency(rs.getString("frequency"))
                .nextDueDate(nextDueDate != null ? nextDueDate.toLocalDate() : null)
                .note(rs.getString("note"))
                .isActive(rs.getBoolean("is_active"))
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .build();
    };

    public RecurringTransaction save(RecurringTransaction rt) {
        String sql = "INSERT INTO recurring_transactions (user_id, wallet_id, category_id, amount, frequency, next_due_date, note, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
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

    /**
     * Tìm tất cả các giao dịch định kỳ đang hoạt động và đến hạn.
     */
    public List<RecurringTransaction> findActiveAndDue() {
        String sql = "SELECT * FROM recurring_transactions WHERE is_active = TRUE AND next_due_date <= ?";
        return jdbcTemplate.query(sql, recurringRowMapper, Date.valueOf(java.time.LocalDate.now()));
    }

    /**
     * Cập nhật ngày đến hạn tiếp theo của giao dịch định kỳ.
     */
    public void updateNextDueDate(Integer recurringId, java.time.LocalDate nextDueDate) {
        String sql = "UPDATE recurring_transactions SET next_due_date = ? WHERE recurring_id = ?";
        jdbcTemplate.update(sql, Date.valueOf(nextDueDate), recurringId);
    }

    /**
     * Xóa giao dịch định kỳ của người dùng.
     */
    public int deleteByIdAndUserId(Integer recurringId, Integer userId) {
        String sql = "DELETE FROM recurring_transactions WHERE recurring_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, recurringId, userId);
    }

    /**
     * Cập nhật trạng thái kích hoạt của giao dịch định kỳ.
     */
    public int updateStatus(Integer recurringId, Integer userId, boolean isActive) {
        String sql = "UPDATE recurring_transactions SET is_active = ? WHERE recurring_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, isActive, recurringId, userId);
    }

    public RecurringTransaction update(RecurringTransaction rt) {
        String sql = "UPDATE recurring_transactions SET wallet_id = ?, category_id = ?, amount = ?, frequency = ?, next_due_date = ?, note = ?, is_active = ? WHERE recurring_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                rt.getWalletId(),
                rt.getCategoryId(),
                rt.getAmount(),
                rt.getFrequency(),
                Date.valueOf(rt.getNextDueDate()),
                rt.getNote(),
                rt.getIsActive(),
                rt.getRecurringId(),
                rt.getUserId());
        return rt;
    }
}
