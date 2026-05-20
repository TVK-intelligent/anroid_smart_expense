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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public Optional<Transaction> findByIdAndUserId(Integer transactionId, Integer userId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ? AND user_id = ?";
        try {
            Transaction transaction = jdbcTemplate.queryForObject(sql, transactionRowMapper, transactionId, userId);
            return Optional.ofNullable(transaction);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Transaction> findByUserId(Integer userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC, created_at DESC";
        return jdbcTemplate.query(sql, transactionRowMapper, userId);
    }

    public List<Transaction> findRecentByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? " +
                "ORDER BY transaction_date DESC, created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, transactionRowMapper, userId, limit);
    }

    public List<Transaction> findFiltered(Integer userId,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          Integer walletId,
                                          Integer categoryId,
                                          String type) {
        StringBuilder sql = new StringBuilder(
                "SELECT t.* FROM transactions t " +
                        "JOIN categories c ON t.category_id = c.category_id " +
                        "WHERE t.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) {
            sql.append(" AND t.transaction_date >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND t.transaction_date <= ?");
            params.add(Date.valueOf(endDate));
        }
        if (walletId != null) {
            sql.append(" AND t.wallet_id = ?");
            params.add(walletId);
        }
        if (categoryId != null) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND UPPER(c.type) = ?");
            params.add(type.trim().toUpperCase());
        }

        sql.append(" ORDER BY t.transaction_date DESC, t.created_at DESC");
        return jdbcTemplate.query(sql.toString(), transactionRowMapper, params.toArray());
    }

    public int countByWalletIdAndUserId(Integer walletId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE wallet_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, walletId, userId);
        return count != null ? count : 0;
    }

    public Transaction update(Integer transactionId, Integer userId, Transaction transaction) {
        String sql = "UPDATE transactions SET wallet_id = ?, category_id = ?, amount = ?, transaction_date = ?, note = ? " +
                "WHERE transaction_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                transaction.getWalletId(),
                transaction.getCategoryId(),
                transaction.getAmount(),
                Date.valueOf(transaction.getTransactionDate()),
                transaction.getNote(),
                transactionId,
                userId);
        transaction.setTransactionId(transactionId);
        transaction.setUserId(userId);
        return transaction;
    }

    public int deleteByIdAndUserId(Integer transactionId, Integer userId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, transactionId, userId);
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

    public BigDecimal getMonthlyTotalByType(Integer userId, String type, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND UPPER(c.type) = ? AND t.transaction_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, type.toUpperCase(), Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public BigDecimal getTotalExpenseByCategoryAndPeriod(Integer userId, Integer categoryId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(t.amount), 0) " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND t.category_id = ? AND UPPER(c.type) = 'EXPENSE' " +
                "AND t.transaction_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, categoryId, Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public List<java.util.Map<String, Object>> getTopExpenseCategories(Integer userId, LocalDate startDate, LocalDate endDate, int limit) {
        String sql = "SELECT c.category_id, c.name, COALESCE(SUM(t.amount), 0) AS total_spent " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND UPPER(c.type) = 'EXPENSE' AND t.transaction_date BETWEEN ? AND ? " +
                "GROUP BY c.category_id, c.name " +
                "ORDER BY total_spent DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, userId, Date.valueOf(startDate), Date.valueOf(endDate), limit);
    }

    public List<Map<String, Object>> getIncomeExpenseByDay(Integer userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT t.transaction_date, " +
                "COALESCE(SUM(CASE WHEN UPPER(c.type) = 'INCOME' THEN t.amount ELSE 0 END), 0) AS total_income, " +
                "COALESCE(SUM(CASE WHEN UPPER(c.type) = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS total_expense " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND t.transaction_date BETWEEN ? AND ? " +
                "GROUP BY t.transaction_date " +
                "ORDER BY t.transaction_date ASC";
        return jdbcTemplate.queryForList(sql, userId, Date.valueOf(startDate), Date.valueOf(endDate));
    }

    public List<Map<String, Object>> getExpenseByCategory(Integer userId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT c.category_id, c.name, COALESCE(SUM(t.amount), 0) AS total_spent " +
                "FROM transactions t JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? AND UPPER(c.type) = 'EXPENSE' AND t.transaction_date BETWEEN ? AND ? " +
                "GROUP BY c.category_id, c.name " +
                "ORDER BY total_spent DESC";
        return jdbcTemplate.queryForList(sql, userId, Date.valueOf(startDate), Date.valueOf(endDate));
    }
}
