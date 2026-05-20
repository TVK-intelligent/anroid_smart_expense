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
public class DebtLoan {
    private Integer debtId;
    private Integer userId;
    private String personName;
    private String type; // "DEBT" (chủ nợ) or "LOAN" (con nợ)
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LocalDate dueDate;
    private String status; // "UNPAID", "PARTIALLY_PAID", "PAID"
    private String note;
    private LocalDateTime createdAt;
}
