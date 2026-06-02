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
public class BudgetDetailResponse {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal spentPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal alertThreshold;
    private String status; // "NORMAL" | "NEAR_LIMIT" | "OVER_LIMIT"
}
