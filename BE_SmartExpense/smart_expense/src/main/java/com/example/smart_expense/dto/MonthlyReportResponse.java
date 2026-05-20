package com.example.smart_expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    private Integer userId;
    private Integer month;
    private Integer year;

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    private List<IncomeExpenseByDay> incomeExpenseByDay;
    private List<ExpenseByCategory> expenseByCategory;
    private List<TopExpenseCategory> topExpenseCategories;
}

