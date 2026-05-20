package com.example.smartexpense.models.dashboard;

import com.example.smartexpense.models.Transaction;
import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {
    @SerializedName("totalBalance")
    private BigDecimal totalBalance;

    @SerializedName("monthlyIncome")
    private BigDecimal monthlyIncome;

    @SerializedName("monthlyExpense")
    private BigDecimal monthlyExpense;

    @SerializedName("monthlyNet")
    private BigDecimal monthlyNet;

    @SerializedName("recentTransactions")
    private List<Transaction> recentTransactions;

    @SerializedName("topExpenseCategories")
    private List<TopExpenseCategory> topExpenseCategories;

    @SerializedName("budgetWarnings")
    private List<BudgetWarning> budgetWarnings;

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public BigDecimal getMonthlyExpense() { return monthlyExpense; }
    public void setMonthlyExpense(BigDecimal monthlyExpense) { this.monthlyExpense = monthlyExpense; }

    public BigDecimal getMonthlyNet() { return monthlyNet; }
    public void setMonthlyNet(BigDecimal monthlyNet) { this.monthlyNet = monthlyNet; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<Transaction> recentTransactions) { this.recentTransactions = recentTransactions; }

    public List<TopExpenseCategory> getTopExpenseCategories() { return topExpenseCategories; }
    public void setTopExpenseCategories(List<TopExpenseCategory> topExpenseCategories) { this.topExpenseCategories = topExpenseCategories; }

    public List<BudgetWarning> getBudgetWarnings() { return budgetWarnings; }
    public void setBudgetWarnings(List<BudgetWarning> budgetWarnings) { this.budgetWarnings = budgetWarnings; }
}

