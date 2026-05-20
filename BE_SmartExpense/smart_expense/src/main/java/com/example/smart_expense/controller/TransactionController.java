package com.example.smart_expense.controller;

import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Tạo giao dịch mới (tự động cập nhật ví và chạy phân tích cảnh báo thông minh).
     * POST /api/transactions
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        Transaction saved = transactionService.createTransaction(transaction);
        return ResponseEntity.ok(saved);
    }
}
