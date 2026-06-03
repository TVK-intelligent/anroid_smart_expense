package com.example.smart_expense.controller;

import com.example.smart_expense.model.RecurringTransaction;
import com.example.smart_expense.repository.RecurringTransactionRepository;
import com.example.smart_expense.service.RecurringTransactionScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionScheduler recurringTransactionScheduler;

    public RecurringTransactionController(RecurringTransactionRepository recurringTransactionRepository,
                                           RecurringTransactionScheduler recurringTransactionScheduler) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.recurringTransactionScheduler = recurringTransactionScheduler;
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
        
        // Immediately process if active and due (today or in the past)
        if (saved.getIsActive() && (saved.getNextDueDate().isBefore(LocalDate.now()) || saved.getNextDueDate().isEqual(LocalDate.now()))) {
            try {
                recurringTransactionScheduler.processSingleTransaction(saved);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        if (active) {
            // Check if this one is due, and process it immediately
            try {
                List<RecurringTransaction> list = recurringTransactionRepository.findByUserId(userId);
                for (RecurringTransaction rt : list) {
                    if (rt.getRecurringId().equals(id)) {
                        if (rt.getNextDueDate().isBefore(LocalDate.now()) || rt.getNextDueDate().isEqual(LocalDate.now())) {
                            recurringTransactionScheduler.processSingleTransaction(rt);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    /**
     * Chạy thủ công quét và xử lý giao dịch định kỳ đến hạn ngay lập tức.
     * POST /api/recurring-transactions/trigger
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerRecurringTransactions() {
        recurringTransactionScheduler.processRecurringTransactions();
        return ResponseEntity.ok("Đã thực hiện quét và thanh toán các hóa đơn định kỳ đến hạn thành công!");
    }

    /**
     * Cập nhật một giao dịch định kỳ.
     * PUT /api/recurring-transactions/{id}?userId=1
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurringTransaction(@PathVariable Integer id,
                                                                           @RequestParam Integer userId,
                                                                           @RequestBody RecurringTransaction request) {
        List<RecurringTransaction> list = recurringTransactionRepository.findByUserId(userId);
        RecurringTransaction existing = null;
        for (RecurringTransaction rt : list) {
            if (rt.getRecurringId().equals(id)) {
                existing = rt;
                break;
            }
        }
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getWalletId() != null) {
            existing.setWalletId(request.getWalletId());
        }
        if (request.getCategoryId() != null) {
            existing.setCategoryId(request.getCategoryId());
        }
        if (request.getAmount() != null) {
            existing.setAmount(request.getAmount());
        }
        if (request.getFrequency() != null) {
            existing.setFrequency(request.getFrequency());
        }
        if (request.getNextDueDate() != null) {
            existing.setNextDueDate(request.getNextDueDate());
        }
        if (request.getNote() != null) {
            existing.setNote(request.getNote());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }

        RecurringTransaction updated = recurringTransactionRepository.update(existing);

        // Immediately check if the updated one is due
        if (updated.getIsActive() && (updated.getNextDueDate().isBefore(LocalDate.now()) || updated.getNextDueDate().isEqual(LocalDate.now()))) {
            try {
                recurringTransactionScheduler.processSingleTransaction(updated);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(updated);
    }
}
