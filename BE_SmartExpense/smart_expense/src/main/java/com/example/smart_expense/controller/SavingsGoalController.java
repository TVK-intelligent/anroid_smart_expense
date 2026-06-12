package com.example.smart_expense.controller;

import com.example.smart_expense.model.SavingsGoal;
import com.example.smart_expense.model.Wallet;
import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.SavingsGoalRepository;
import com.example.smart_expense.repository.WalletRepository;
import com.example.smart_expense.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalRepository savingsGoalRepository;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    public SavingsGoalController(SavingsGoalRepository savingsGoalRepository,
                                 WalletRepository walletRepository,
                                 TransactionService transactionService) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
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
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Integer walletId) {
        
        Optional<SavingsGoal> goalOpt = savingsGoalRepository.findById(goalId);
        if (goalOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Mục tiêu tích lũy không tồn tại!");
            return ResponseEntity.badRequest().body(error);
        }

        SavingsGoal goal = goalOpt.get();
        Integer resolvedWalletId = walletId != null ? walletId : goal.getWalletId();

        if (resolvedWalletId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Vui long chon vi nguon de nap quy!");
            return ResponseEntity.badRequest().body(error);
        }

        Optional<Wallet> walletOpt = walletRepository.findByIdAndUserId(resolvedWalletId, goal.getUserId());
        if (walletOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Vi nguon khong ton tai hoac khong thuoc nguoi dung nay!");
            return ResponseEntity.badRequest().body(error);
        }

        Wallet wallet = walletOpt.get();
        if (wallet.getBalance() == null || wallet.getBalance().compareTo(amount) < 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", String.format("Vi '%s' khong du so du de nap quy (Can: %s, Hien co: %s)!",
                    wallet.getName(), amount.setScale(0), wallet.getBalance() != null ? wallet.getBalance().setScale(0) : BigDecimal.ZERO));
            return ResponseEntity.badRequest().body(error);
        }

        Transaction transaction = Transaction.builder()
                .userId(goal.getUserId())
                .walletId(resolvedWalletId)
                .categoryId(8)
                .amount(amount)
                .type("EXPENSE")
                .transactionDate(LocalDate.now())
                .note("Nap quy tiet kiem: " + goal.getGoalName())
                .build();

        transactionService.createTransaction(transaction);
        savingsGoalRepository.updateCurrentAmount(goalId, amount);

        BigDecimal walletBalanceAfter = walletRepository
                .findByIdAndUserId(resolvedWalletId, goal.getUserId())
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("goalId", goalId);
        response.put("walletId", resolvedWalletId);
        response.put("walletName", wallet.getName());
        response.put("walletBalanceAfter", walletBalanceAfter);
        response.put("amountAdded", amount);
        response.put("message", "Nạp quỹ tích lũy thành công!");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật mục tiêu tích lũy.
     * PUT /api/savings-goals/{goalId}?userId=1
     */
    @PutMapping("/{goalId}")
    public ResponseEntity<?> updateGoal(@PathVariable Integer goalId,
                                        @RequestParam Integer userId,
                                        @RequestBody SavingsGoal request) {
        Optional<SavingsGoal> existingOpt = savingsGoalRepository.findById(goalId);
        if (existingOpt.isEmpty() || !existingOpt.get().getUserId().equals(userId)) {
            return ResponseEntity.notFound().build();
        }

        SavingsGoal existing = existingOpt.get();
        if (request.getGoalName() != null) {
            existing.setGoalName(request.getGoalName());
        }
        if (request.getTargetAmount() != null) {
            existing.setTargetAmount(request.getTargetAmount());
        }
        if (request.getCurrentAmount() != null) {
            existing.setCurrentAmount(request.getCurrentAmount());
        }
        if (request.getWalletId() != null) {
            existing.setWalletId(request.getWalletId());
        }
        if (request.getDeadline() != null) {
            existing.setDeadline(request.getDeadline());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        SavingsGoal updated = savingsGoalRepository.update(existing);
        return ResponseEntity.ok(updated);
    }
}

