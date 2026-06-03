package com.example.smart_expense.service;

import com.example.smart_expense.model.Category;
import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.CategoryRepository;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final SmartAnalyticsService smartAnalyticsService;

    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              CategoryRepository categoryRepository,
                              SmartAnalyticsService smartAnalyticsService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
        this.smartAnalyticsService = smartAnalyticsService;
    }

    /**
     * Ghi nhận giao dịch mới: lưu vào database, tự động cộng/trừ số dư ví
     * và kích hoạt các thuật toán rule-based phân tích chi tiêu tức thì.
     */
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        validateCreate(transaction);
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }

        // Resolve type BEFORE saving to ensure it is stored correctly in the database
        String resolvedType = resolveType(transaction.getType(), transaction.getCategoryId());
        transaction.setType(resolvedType);

        // 1. Lưu giao dịch
        Transaction savedTransaction = transactionRepository.save(transaction);

        BigDecimal amountChange = computeWalletDelta(resolvedType, transaction.getAmount());
        int updatedWallets = walletRepository.updateBalance(
                transaction.getWalletId(),
                transaction.getUserId(),
                amountChange);
        if (updatedWallets == 0) {
            throw new IllegalArgumentException("Wallet not found for this user");
        }

        if ("EXPENSE".equalsIgnoreCase(resolvedType)) {
            smartAnalyticsService.checkForAnomaly(
                    transaction.getUserId(),
                    transaction.getCategoryId(),
                    transaction.getAmount()
            );
            smartAnalyticsService.checkBurnRateAndSuggest(
                    transaction.getUserId(),
                    transaction.getCategoryId()
            );
        }

        return savedTransaction;
    }

    @Transactional
    public Transaction updateTransaction(Integer transactionId, Integer userId, Transaction updateRequest) {
        if (updateRequest == null) throw new IllegalArgumentException("Transaction body is required");

        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found for this user"));

        if (updateRequest.getWalletId() == null) throw new IllegalArgumentException("walletId is required");
        if (updateRequest.getCategoryId() == null) throw new IllegalArgumentException("categoryId is required");
        if (updateRequest.getAmount() == null) throw new IllegalArgumentException("amount is required");
        if (updateRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount must be > 0");
        if (updateRequest.getTransactionDate() == null) throw new IllegalArgumentException("transactionDate is required");

        String oldType = resolveType(existing.getType(), existing.getCategoryId());
        BigDecimal rollbackDelta = computeWalletDelta(oldType, existing.getAmount()).negate();

        // Rollback old impact
        int rolledBack = walletRepository.updateBalance(existing.getWalletId(), userId, rollbackDelta);
        if (rolledBack == 0) throw new IllegalArgumentException("Wallet not found for this user");

        // Apply new impact
        String newType = resolveType(updateRequest.getType(), updateRequest.getCategoryId());
        BigDecimal newDelta = computeWalletDelta(newType, updateRequest.getAmount());
        int applied = walletRepository.updateBalance(updateRequest.getWalletId(), userId, newDelta);
        if (applied == 0) throw new IllegalArgumentException("Wallet not found for this user");

        Transaction updated = transactionRepository.update(transactionId, userId, updateRequest);
        updated.setType(newType);
        return updated;
    }

    @Transactional
    public void deleteTransaction(Integer transactionId, Integer userId) {
        Transaction existing = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found for this user"));

        String existingType = resolveType(existing.getType(), existing.getCategoryId());
        BigDecimal rollbackDelta = computeWalletDelta(existingType, existing.getAmount()).negate();

        int rolledBack = walletRepository.updateBalance(existing.getWalletId(), userId, rollbackDelta);
        if (rolledBack == 0) throw new IllegalArgumentException("Wallet not found for this user");

        int deleted = transactionRepository.deleteByIdAndUserId(transactionId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("Transaction not found for this user");
        }
    }

    private void validateCreate(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("Transaction body is required");
        if (transaction.getUserId() == null) throw new IllegalArgumentException("userId is required");
        if (transaction.getWalletId() == null) throw new IllegalArgumentException("walletId is required");
        if (transaction.getCategoryId() == null) throw new IllegalArgumentException("categoryId is required");
        if (transaction.getAmount() == null) throw new IllegalArgumentException("amount is required");
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("amount must be > 0");
    }

    private String resolveType(String requestType, Integer categoryId) {
        if (requestType != null && !requestType.isBlank()) {
            String t = requestType.trim().toUpperCase();
            if (!"INCOME".equals(t) && !"EXPENSE".equals(t)) {
                throw new IllegalArgumentException("type must be INCOME or EXPENSE");
            }
            return t;
        }

        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty() || categoryOpt.get().getType() == null) {
            throw new IllegalArgumentException("Category not found");
        }
        return categoryOpt.get().getType().trim().toUpperCase();
    }

    private BigDecimal computeWalletDelta(String type, BigDecimal amount) {
        if ("EXPENSE".equalsIgnoreCase(type)) return amount.negate();
        return amount;
    }
}
