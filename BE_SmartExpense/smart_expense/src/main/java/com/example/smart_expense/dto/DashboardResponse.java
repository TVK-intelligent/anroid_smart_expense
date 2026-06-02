package com.example.smart_expense.dto;

import com.example.smart_expense.model.Transaction;
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
public class DashboardResponse {
    private BigDecimal totalBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private BigDecimal monthlyNet;

    private List<Transaction> recentTransactions;
    private List<TopExpenseCategory> topExpenseCategories;
    private List<BudgetWarning> budgetWarnings;
}

