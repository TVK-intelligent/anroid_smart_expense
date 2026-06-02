package com.example.smart_expense.service;

import com.example.smart_expense.dto.BudgetWarning;
import com.example.smart_expense.dto.DashboardResponse;
import com.example.smart_expense.dto.TopExpenseCategory;
import com.example.smart_expense.model.Budget;
import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.CategoryRepository;
import com.example.smart_expense.repository.BudgetRepository;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(WalletRepository walletRepository,
                            TransactionRepository transactionRepository,
                            BudgetRepository budgetRepository,
                            CategoryRepository categoryRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    public DashboardResponse getDashboard(Integer userId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");

        BigDecimal totalBalance = walletRepository.getTotalBalanceByUserId(userId);

        YearMonth ym = YearMonth.now();
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        BigDecimal monthlyIncome = transactionRepository.getMonthlyTotalByType(userId, "INCOME", startOfMonth, endOfMonth);
        BigDecimal monthlyExpense = transactionRepository.getMonthlyTotalByType(userId, "EXPENSE", startOfMonth, endOfMonth);
        BigDecimal monthlyNet = monthlyIncome.subtract(monthlyExpense);

        List<Transaction> recentTransactions = transactionRepository.findRecentByUserId(userId, 5);
        // Populate type for convenience (derived from category)
        for (Transaction t : recentTransactions) {
            String resolvedType = resolveTypeFromCategory(t.getCategoryId());
            if (resolvedType != null) t.setType(resolvedType);
        }

        List<TopExpenseCategory> topExpenseCategories = new ArrayList<>();
        List<Map<String, Object>> topRows = transactionRepository.getTopExpenseCategories(userId, startOfMonth, endOfMonth, 3);
        for (Map<String, Object> row : topRows) {
            TopExpenseCategory item = TopExpenseCategory.builder()
                    .categoryId(((Number) row.get("category_id")).intValue())
                    .categoryName((String) row.get("name"))
                    .totalSpent((BigDecimal) row.get("total_spent"))
                    .build();
            topExpenseCategories.add(item);
        }

        List<BudgetWarning> budgetWarnings = computeBudgetWarnings(userId);

        return DashboardResponse.builder()
                .totalBalance(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .monthlyNet(monthlyNet)
                .recentTransactions(recentTransactions)
                .topExpenseCategories(topExpenseCategories)
                .budgetWarnings(budgetWarnings)
                .build();
    }

    private List<BudgetWarning> computeBudgetWarnings(Integer userId) {
        List<Budget> activeBudgets = budgetRepository.findActiveBudgets(userId);
        if (activeBudgets.isEmpty()) return List.of();

        List<BudgetWarning> warnings = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Budget b : activeBudgets) {
            if (b.getAmount() == null || b.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

            LocalDate start = b.getStartDate();
            LocalDate end = b.getEndDate();
            if (start == null || end == null) continue;
            if (today.isBefore(start) || today.isAfter(end)) continue;

            BigDecimal spent = transactionRepository.getTotalExpenseByCategoryAndPeriod(userId, b.getCategoryId(), start, end);
            BigDecimal percent = spent
                    .multiply(new BigDecimal("100"))
                    .divide(b.getAmount(), 2, RoundingMode.HALF_UP);

            BigDecimal threshold = b.getAlertThreshold() != null ? b.getAlertThreshold() : new BigDecimal("80.0");
            boolean over = spent.compareTo(b.getAmount()) > 0;
            boolean near = percent.compareTo(threshold) >= 0;

            if (!over && !near) continue;

            // Category name is optional; try pull from topExpenseCategories row style query
            String categoryName = resolveCategoryName(b.getCategoryId());

            warnings.add(BudgetWarning.builder()
                    .budgetId(b.getBudgetId())
                    .categoryId(b.getCategoryId())
                    .categoryName(categoryName)
                    .budgetAmount(b.getAmount())
                    .spentAmount(spent)
                    .spentPercent(percent)
                    .startDate(start)
                    .endDate(end)
                    .status(over ? "OVER_LIMIT" : "NEAR_LIMIT")
                    .build());
        }

        return warnings;
    }

    private String resolveTypeFromCategory(Integer categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .map(c -> c.getType() != null ? c.getType().trim().toUpperCase() : null)
                .orElse(null);
    }

    private String resolveCategoryName(Integer categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .map(c -> c.getName())
                .orElse(null);
    }
}
