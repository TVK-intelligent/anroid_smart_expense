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
public class Budget {
    private Integer budgetId;
    private Integer userId;
    private Integer categoryId;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal alertThreshold; // e.g. 80.00 (%)
    private LocalDateTime createdAt;
}
