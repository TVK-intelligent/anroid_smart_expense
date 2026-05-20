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
public class SavingsGoal {
    private Integer goalId;
    private Integer userId;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private String status; // "IN_PROGRESS", "COMPLETED", "FAILED"
    private LocalDateTime createdAt;
}
