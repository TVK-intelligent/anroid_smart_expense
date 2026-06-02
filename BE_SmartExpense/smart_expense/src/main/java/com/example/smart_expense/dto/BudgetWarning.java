package com.example.smart_expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetWarning {
    private Integer budgetId;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal spentPercent; // 0..100
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // "NEAR_LIMIT" | "OVER_LIMIT"
}

