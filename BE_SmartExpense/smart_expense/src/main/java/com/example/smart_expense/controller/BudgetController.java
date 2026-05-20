package com.example.smart_expense.controller;

import com.example.smart_expense.model.Budget;
import com.example.smart_expense.repository.BudgetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;

    public BudgetController(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
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
