package com.example.smart_expense.controller;

import com.example.smart_expense.model.DebtLoan;
import com.example.smart_expense.repository.DebtLoanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/debts")
public class DebtLoanController {

    private final DebtLoanRepository debtLoanRepository;

    public DebtLoanController(DebtLoanRepository debtLoanRepository) {
        this.debtLoanRepository = debtLoanRepository;
    }

    /**
     * Lấy danh sách các khoản nợ / cho vay của người dùng.
     * GET /api/debts?userId=1
     */
    @GetMapping
    public ResponseEntity<List<DebtLoan>> getDebts(@RequestParam Integer userId) {
        List<DebtLoan> list = debtLoanRepository.findByUserId(userId);
        return ResponseEntity.ok(list);
    }

    /**
     * Thêm khoản nợ / cho vay mới.
     * POST /api/debts
     */
    @PostMapping
    public ResponseEntity<?> createDebt(@RequestBody DebtLoan dl) {
        if (dl.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        if (dl.getPersonName() == null || dl.getPersonName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("personName is required");
        }
        if (dl.getType() == null || (!dl.getType().equalsIgnoreCase("DEBT") && !dl.getType().equalsIgnoreCase("LOAN"))) {
            return ResponseEntity.badRequest().body("type must be DEBT or LOAN");
        }
        if (dl.getAmount() == null || dl.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("amount must be greater than 0");
        }

        if (dl.getStatus() == null) {
            dl.setStatus("UNPAID");
        }

        DebtLoan saved = debtLoanRepository.save(dl);
        return ResponseEntity.ok(saved);
    }

    /**
     * Cập nhật khoản nợ / cho vay.
     * PUT /api/debts/{debtId}?userId=1
     */
    @PutMapping("/{debtId}")
    public ResponseEntity<?> updateDebt(@PathVariable Integer debtId,
                                        @RequestParam Integer userId,
                                        @RequestBody DebtLoan request) {
        Optional<DebtLoan> existingOpt = debtLoanRepository.findByIdAndUserId(debtId, userId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DebtLoan existing = existingOpt.get();
        if (request.getPersonName() != null) {
            existing.setPersonName(request.getPersonName());
        }
        if (request.getType() != null) {
            existing.setType(request.getType());
        }
        if (request.getAmount() != null) {
            existing.setAmount(request.getAmount());
        }
        if (request.getInterestRate() != null) {
            existing.setInterestRate(request.getInterestRate());
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(request.getDueDate());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            existing.setNote(request.getNote());
        }

        DebtLoan updated = debtLoanRepository.update(existing);
        return ResponseEntity.ok(updated);
    }

    /**
     * Xóa khoản nợ / cho vay.
     * DELETE /api/debts/{debtId}?userId=1
     */
    @DeleteMapping("/{debtId}")
    public ResponseEntity<?> deleteDebt(@PathVariable Integer debtId,
                                        @RequestParam Integer userId) {
        Optional<DebtLoan> existing = debtLoanRepository.findByIdAndUserId(debtId, userId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        debtLoanRepository.deleteByIdAndUserId(debtId, userId);
        return ResponseEntity.noContent().build();
    }
}
