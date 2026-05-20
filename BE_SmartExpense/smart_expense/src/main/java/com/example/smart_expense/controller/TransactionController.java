package com.example.smart_expense.controller;

import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public TransactionController(TransactionRepository transactionRepository,
                                 TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    /**
     * Lấy danh sách toàn bộ giao dịch của người dùng.
     * GET /api/transactions?userId=1
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(@RequestParam Integer userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Lấy danh sách giao dịch gần nhất.
     * GET /api/transactions/recent?userId=1&limit=20
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions(@RequestParam Integer userId,
                                                                   @RequestParam(required = false, defaultValue = "20") Integer limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<Transaction> transactions = transactionRepository.findRecentByUserId(userId, safeLimit);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Lọc giao dịch theo ngày, ví, danh mục, loại (INCOME/EXPENSE).
     * GET /api/transactions/filter?userId=1&startDate=2026-01-01&endDate=2026-01-31&walletId=2&categoryId=3&type=EXPENSE
     */
    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> filterTransactions(@RequestParam Integer userId,
                                                                @RequestParam(required = false) LocalDate startDate,
                                                                @RequestParam(required = false) LocalDate endDate,
                                                                @RequestParam(required = false) Integer walletId,
                                                                @RequestParam(required = false) Integer categoryId,
                                                                @RequestParam(required = false) String type) {
        List<Transaction> transactions = transactionRepository.findFiltered(userId, startDate, endDate, walletId, categoryId, type);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Tạo giao dịch mới (tự động cập nhật ví và chạy phân tích cảnh báo thông minh).
     * POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        try {
            Transaction saved = transactionService.createTransaction(transaction);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Sửa giao dịch và cập nhật lại số dư ví chính xác.
     * PUT /api/transactions/{transactionId}?userId=1
     */
    @PutMapping("/{transactionId}")
    public ResponseEntity<?> updateTransaction(@PathVariable Integer transactionId,
                                               @RequestParam Integer userId,
                                               @RequestBody Transaction transaction) {
        try {
            Transaction updated = transactionService.updateTransaction(transactionId, userId, transaction);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }

    /**
     * Xóa giao dịch và rollback lại số dư ví chính xác.
     * DELETE /api/transactions/{transactionId}?userId=1
     */
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Integer transactionId,
                                               @RequestParam Integer userId) {
        try {
            transactionService.deleteTransaction(transactionId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Bad request";
            if (msg.toLowerCase().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }
}
