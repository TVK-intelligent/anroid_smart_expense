package com.example.smart_expense.controller;

import com.example.smart_expense.model.SavingsGoal;
import com.example.smart_expense.repository.SavingsGoalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalController(SavingsGoalRepository savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    /**
     * Lấy danh sách mục tiêu tích lũy của người dùng.
     * GET /api/savings-goals?userId=1
     */
    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getSavingsGoals(@RequestParam Integer userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        return ResponseEntity.ok(goals);
    }

    /**
     * Tạo mới một mục tiêu tích lũy.
     * POST /api/savings-goals
     */
    @PostMapping
    public ResponseEntity<SavingsGoal> createSavingsGoal(@RequestBody SavingsGoal goal) {
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        if (goal.getDeadline() == null) {
            goal.setDeadline(LocalDate.now().plusYears(1));
        }
        goal.setStatus("IN_PROGRESS");
        goal.setCreatedAt(LocalDateTime.now());
        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        return ResponseEntity.ok(savedGoal);
    }

    /**
     * Nạp thêm quỹ tiền vào mục tiêu tích lũy.
     * POST /api/savings-goals/{goalId}/add-funds?amount=500000
     */
    @PostMapping("/{goalId}/add-funds")
    public ResponseEntity<Map<String, Object>> addFunds(
            @PathVariable Integer goalId,
            @RequestParam BigDecimal amount) {
        
        savingsGoalRepository.updateCurrentAmount(goalId, amount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("goalId", goalId);
        response.put("amountAdded", amount);
        response.put("message", "Nạp quỹ tích lũy thành công!");
        
        return ResponseEntity.ok(response);
    }
}
