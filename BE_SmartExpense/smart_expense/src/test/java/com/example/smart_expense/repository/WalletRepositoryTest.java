package com.example.smart_expense.repository;

import com.example.smart_expense.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WalletRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void saveThenFindByUserId_returnsExistingAndNewWallets() {
        Integer userId = 4242;
        jdbcTemplate.update(
                "INSERT INTO wallets (user_id, name, balance, type, created_at) VALUES (?, ?, ?, ?, NOW())",
                userId, "Cash", new BigDecimal("100000.00"), "CASH");

        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .name("Bank")
                .balance(new BigDecimal("200000.00"))
                .type("BANK")
                .build();

        walletRepository.save(newWallet);

        List<Wallet> wallets = walletRepository.findByUserId(userId);

        assertThat(wallets).extracting(Wallet::getName)
                .containsExactly("Bank", "Cash");
        assertThat(wallets).allSatisfy(wallet -> assertThat(wallet.getCreatedAt()).isNotNull());
    }
}
