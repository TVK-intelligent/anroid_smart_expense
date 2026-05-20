package com.example.smart_expense.controller;

import com.example.smart_expense.model.RecurringTransaction;
import com.example.smart_expense.repository.RecurringTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionRepository recurringTransactionRepository;

    public RecurringTransactionController(RecurringTransactionRepository recurringTransactionRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    /**
     * Lấy danh sách giao dịch định kỳ của người dùng.
     * GET /api/recurring-transactions?userId=1
     */
    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getRecurringTransactions(@RequestParam Integer userId) {
        List<RecurringTransaction> list = recurringTransactionRepository.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Tạo mới một giao dịch định kỳ.
     * POST /api/recurring-transactions
     */
    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurringTransaction(@RequestBody RecurringTransaction rt) {
        if (rt.getNextDueDate() == null) {
            rt.setNextDueDate(LocalDate.now());
        }
        if (rt.getIsActive() == null) {
            rt.setIsActive(true);
        }
        rt.setCreatedAt(LocalDateTime.now());
        RecurringTransaction saved = recurringTransactionRepository.save(rt);
        return ResponseEntity.ok(saved);
    }

    /**
     * Cập nhật trạng thái (bật/tắt) của giao dịch định kỳ.
     * PUT /api/recurring-transactions/{id}/status?userId=1&active=true
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Integer id,
                                             @RequestParam Integer userId,
                                             @RequestParam boolean active) {
        int rows = recurringTransactionRepository.updateStatus(id, userId, active);
        if (rows == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Xóa một giao dịch định kỳ.
     * DELETE /api/recurring-transactions/{id}?userId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable Integer id,
                                                           @RequestParam Integer userId) {
        int rows = recurringTransactionRepository.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
