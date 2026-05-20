package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    private Integer walletId;
    private Integer userId;
    private String name;
    private BigDecimal balance;
    private String type; // e.g. "CASH", "BANK", "E-WALLET"
    private LocalDateTime createdAt;
}
