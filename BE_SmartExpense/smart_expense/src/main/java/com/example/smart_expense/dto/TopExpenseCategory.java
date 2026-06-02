package com.example.smart_expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopExpenseCategory {
    private Integer categoryId;
    private String categoryName;
    private BigDecimal totalSpent;
}

