package com.example.smart_expense.repository;

import com.example.smart_expense.model.DebtLoan;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DebtLoanRepository {

    private final JdbcTemplate jdbcTemplate;

    public DebtLoanRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DebtLoan> debtLoanRowMapper = (rs, rowNum) -> DebtLoan.builder()
            .debtId(rs.getInt("debt_id"))
            .userId(rs.getInt("user_id"))
            .personName(rs.getString("person_name"))
            .type(rs.getString("type"))
            .amount(rs.getBigDecimal("amount"))
            .interestRate(rs.getBigDecimal("interest_rate"))
            .dueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null)
            .status(rs.getString("status"))
            .note(rs.getString("note"))
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
            .build();

    public DebtLoan save(DebtLoan dl) {
        String sql = "INSERT INTO debt_loans (user_id, person_name, type, amount, interest_rate, due_date, status, note, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();
        dl.setCreatedAt(now);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, dl.getUserId());
            ps.setString(2, dl.getPersonName());
            ps.setString(3, dl.getType());
            ps.setBigDecimal(4, dl.getAmount());
            ps.setBigDecimal(5, dl.getInterestRate());
            ps.setDate(6, dl.getDueDate() != null ? java.sql.Date.valueOf(dl.getDueDate()) : null);
            ps.setString(7, dl.getStatus() != null ? dl.getStatus() : "UNPAID");
            ps.setString(8, dl.getNote());
            ps.setTimestamp(9, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            dl.setDebtId(keyHolder.getKey().intValue());
        }
        return dl;
    }

    public List<DebtLoan> findByUserId(Integer userId) {
        String sql = "SELECT * FROM debt_loans WHERE user_id = ? ORDER BY due_date ASC";
        return jdbcTemplate.query(sql, debtLoanRowMapper, userId);
    }

    public Optional<DebtLoan> findByIdAndUserId(Integer debtId, Integer userId) {
        String sql = "SELECT * FROM debt_loans WHERE debt_id = ? AND user_id = ?";
        try {
            DebtLoan dl = jdbcTemplate.queryForObject(sql, debtLoanRowMapper, debtId, userId);
            return Optional.ofNullable(dl);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public DebtLoan update(DebtLoan dl) {
        String sql = "UPDATE debt_loans SET person_name = ?, type = ?, amount = ?, interest_rate = ?, due_date = ?, status = ?, note = ? " +
                "WHERE debt_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                dl.getPersonName(),
                dl.getType(),
                dl.getAmount(),
                dl.getInterestRate(),
                dl.getDueDate() != null ? java.sql.Date.valueOf(dl.getDueDate()) : null,
                dl.getStatus(),
                dl.getNote(),
                dl.getDebtId(),
                dl.getUserId());
        return dl;
    }

    public int deleteByIdAndUserId(Integer debtId, Integer userId) {
        String sql = "DELETE FROM debt_loans WHERE debt_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, debtId, userId);
    }
}
