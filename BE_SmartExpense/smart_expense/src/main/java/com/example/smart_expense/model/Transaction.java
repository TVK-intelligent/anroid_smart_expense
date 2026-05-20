package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Integer transactionId;
    private Integer userId;
    private Integer walletId;
    private Integer categoryId;
    // Optional: if DB doesn't store this, service will derive from category.type
    // and populate it in responses where possible.
    private String type; // "INCOME" or "EXPENSE"
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String note;
    private LocalDateTime createdAt;
}
