package com.example.smart_expense.controller;

import com.example.smart_expense.model.Budget;
import com.example.smart_expense.dto.BudgetDetailResponse;
import com.example.smart_expense.repository.BudgetRepository;
import com.example.smart_expense.repository.CategoryRepository;
import com.example.smart_expense.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public BudgetController(BudgetRepository budgetRepository,
                            TransactionRepository transactionRepository,
                            CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Lấy toàn bộ danh sách ngân sách của người dùng.
     * GET /api/budgets?userId=1
     */
    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(@RequestParam Integer userId) {
        List<Budget> budgets = budgetRepository.findByUserId(userId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Lấy danh sách ngân sách đang hoạt động hôm nay của người dùng.
     * GET /api/budgets/active?userId=1
     */
    @GetMapping("/active")
    public ResponseEntity<List<Budget>> getActiveBudgets(@RequestParam Integer userId) {
        List<Budget> budgets = budgetRepository.findActiveBudgets(userId);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Lấy thông tin chi tiết ngân sách kèm theo tiến độ chi tiêu thực tế.
     * GET /api/budgets/details?userId=1
     */
    @GetMapping("/details")
    public ResponseEntity<List<BudgetDetailResponse>> getBudgetDetails(@RequestParam Integer userId) {
        List<Budget> activeBudgets = budgetRepository.findActiveBudgets(userId);
        List<BudgetDetailResponse> details = new ArrayList<>();

        for (Budget budget : activeBudgets) {
            String categoryName = categoryRepository.findById(budget.getCategoryId())
                    .map(c -> c.getName())
                    .orElse("Hạng mục khác");

            BigDecimal spent = transactionRepository.getTotalExpenseByCategoryAndPeriod(
                    userId,
                    budget.getCategoryId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
            if (spent == null) {
                spent = BigDecimal.ZERO;
            }

            BigDecimal limit = budget.getAmount();
            BigDecimal percent = BigDecimal.ZERO;
            if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
                percent = spent.multiply(new BigDecimal("100"))
                        .divide(limit, 2, RoundingMode.HALF_UP);
            }

            String status = "NORMAL";
            BigDecimal threshold = budget.getAlertThreshold();
            if (threshold == null) {
                threshold = new BigDecimal("80.0");
            }

            if (spent.compareTo(limit) >= 0) {
                status = "OVER_LIMIT";
            } else if (percent.compareTo(threshold) >= 0) {
                status = "NEAR_LIMIT";
            }

            details.add(BudgetDetailResponse.builder()
                    .budgetId(budget.getBudgetId())
                    .userId(userId)
                    .categoryId(budget.getCategoryId())
                    .categoryName(categoryName)
                    .limitAmount(limit)
                    .spentAmount(spent)
                    .spentPercent(percent)
                    .startDate(budget.getStartDate())
                    .endDate(budget.getEndDate())
                    .alertThreshold(threshold)
                    .status(status)
                    .build());
        }

        return ResponseEntity.ok(details);
    }

    /**
     * Tạo mới hoặc thiết lập một hạn mức ngân sách.
     * POST /api/budgets
     */
    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        if (budget.getStartDate() == null) {
            budget.setStartDate(LocalDate.now());
        }
        if (budget.getEndDate() == null) {
            budget.setEndDate(LocalDate.now().plusMonths(1));
        }
        if (budget.getAlertThreshold() == null) {
            budget.setAlertThreshold(new BigDecimal("80.0"));
        }
        budget.setCreatedAt(LocalDateTime.now());
        Budget savedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(savedBudget);
    }
}
