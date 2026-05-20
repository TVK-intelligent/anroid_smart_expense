package com.example.smart_expense.repository;

import com.example.smart_expense.model.Transaction;
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
public class TransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public TransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> Transaction.builder()
            .transactionId(rs.getInt("transaction_id"))
            .userId(rs.getInt("user_id"))
            .walletId(rs.getInt("wallet_id"))
            .categoryId(rs.getInt("category_id"))
            .amount(rs.getBigDecimal("amount"))
            .transactionDate(rs.getDate("transaction_date").toLocalDate())
            .note(rs.getString("note"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public Transaction save(Transaction transaction) {
        String sql = "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, transaction.getUserId());
            ps.setInt(2, transaction.getWalletId());
            ps.setInt(3, transaction.getCategoryId());
            ps.setBigDecimal(4, transaction.getAmount());
            ps.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            ps.setString(6, transaction.getNote());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            transaction.setTransactionId(keyHolder.getKey().intValue());
        }
        return transaction;
    }

    public Optional<Transaction> findById(Integer transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try {
            Transaction transaction = jdbcTemplate.queryForObject(sql, transactionRowMapper, transactionId);
            return Optional.ofNullable(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Transaction> findByUserId(Integer userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC, created_at DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, userId);
    }

    /**
     * Tính toán số tiền chi tiêu trung bình cho một danh mục trong N ngày gần nhất.
     * Sử dụng cho thuật toán Phát hiện Bất thường (Anomaly Detection).
     */
    public BigDecimal getAverageExpenseForCategory(Integer userId, Integer categoryId, int days) {
        String sql = "SELECT COALESCE(AVG(amount), 0) FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.category_id " +
                     "WHERE t.user_id = ? AND t.category_id = ? AND c.type = 'EXPENSE' " +
                     "AND t.transaction_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, categoryId, days);
    }

    /**
     * Tính tổng chi tiêu của một danh mục trong khoảng thời gian nhất định (sử dụng cho Budget/Burn rate).
     */
    public BigDecimal getTotalSpentByCategoryAndPeriod(Integer userId, Integer categoryId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE user_id = ? AND category_id = ? " +
                     "AND transaction_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, categoryId, Date.valueOf(startDate), Date.valueOf(endDate));
    }
}
