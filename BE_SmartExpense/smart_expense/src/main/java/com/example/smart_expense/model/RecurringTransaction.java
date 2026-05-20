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
public class RecurringTransaction {
    private Integer recurringId;
    private Integer userId;
    private Integer walletId;
    private Integer categoryId;
    private BigDecimal amount;
    private String frequency; // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    private LocalDate nextDueDate;
    private String note;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
