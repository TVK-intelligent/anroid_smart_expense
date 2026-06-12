package com.example.smart_expense.repository;

import com.example.smart_expense.model.Wallet;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class WalletRepository {

    private final JdbcTemplate jdbcTemplate;

    public WalletRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Wallet> walletRowMapper = (rs, rowNum) -> {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return Wallet.builder()
                .walletId(rs.getInt("wallet_id"))
                .userId(rs.getInt("user_id"))
                .name(rs.getString("name"))
                .balance(rs.getBigDecimal("balance"))
                .type(rs.getString("type"))
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .build();
    };

    public Wallet save(Wallet wallet) {
        String sql = "INSERT INTO wallets (user_id, name, balance, type, created_at) VALUES (?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, wallet.getUserId());
            ps.setString(2, wallet.getName());
            ps.setBigDecimal(3, wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO);
            ps.setString(4, wallet.getType());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            wallet.setWalletId(keyHolder.getKey().intValue());
        }
        return wallet;
    }

    public Optional<Wallet> findByIdAndUserId(Integer walletId, Integer userId) {
        String sql = "SELECT * FROM wallets WHERE wallet_id = ? AND user_id = ?";
        try {
            Wallet wallet = jdbcTemplate.queryForObject(sql, walletRowMapper, walletId, userId);
            return Optional.ofNullable(wallet);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Wallet> findByUserId(Integer userId) {
        String sql = "SELECT * FROM wallets WHERE user_id = ? ORDER BY created_at DESC, wallet_id DESC";
        return jdbcTemplate.query(sql, walletRowMapper, userId);
    }

    public Wallet update(Wallet wallet) {
        String sql = "UPDATE wallets SET name = ?, type = ?, balance = ? WHERE wallet_id = ? AND user_id = ?";
        jdbcTemplate.update(sql,
                wallet.getName(),
                wallet.getType(),
                wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO,
                wallet.getWalletId(),
                wallet.getUserId());
        return wallet;
    }

    public int deleteByIdAndUserId(Integer walletId, Integer userId) {
        String sql = "DELETE FROM wallets WHERE wallet_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, walletId, userId);
    }

    public BigDecimal getTotalBalanceByUserId(Integer userId) {
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM wallets WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, userId);
    }

    public int updateBalance(Integer walletId, Integer userId, BigDecimal amountChange) {
        String sql = "UPDATE wallets SET balance = balance + ? WHERE wallet_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, amountChange, walletId, userId);
    }
}
