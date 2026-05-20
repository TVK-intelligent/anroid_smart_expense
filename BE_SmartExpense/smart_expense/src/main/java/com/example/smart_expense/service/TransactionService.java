package com.example.smart_expense.service;

import com.example.smart_expense.model.Category;
import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.CategoryRepository;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        // 1. Lưu giao dịch
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 2. Tìm thông tin danh mục chi tiêu
        Optional<Category> categoryOpt = categoryRepository.findById(transaction.getCategoryId());
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            BigDecimal amountChange = transaction.getAmount();

            // Nếu là khoản chi (EXPENSE) -> trừ số dư ví; nếu là thu (INCOME) -> cộng số dư ví
            if ("EXPENSE".equalsIgnoreCase(category.getType())) {
                amountChange = amountChange.negate();
            }

            // Cập nhật số dư ví tương ứng
            walletRepository.updateBalance(transaction.getWalletId(), amountChange);

            // 3. Áp dụng phân tích thông minh cho các khoản chi tiêu
            if ("EXPENSE".equalsIgnoreCase(category.getType())) {
                // Kiểm tra tính bất thường (tự sinh thông báo nếu phát hiện giao dịch đột biến)
                smartAnalyticsService.checkForAnomaly(
                        transaction.getUserId(),
                        transaction.getCategoryId(),
                        transaction.getAmount()
                );

                // Kiểm tra burn rate (tự sinh thông báo nếu tiêu vượt kế hoạch tiến độ)
                smartAnalyticsService.checkBurnRateAndSuggest(
                        transaction.getUserId(),
                        transaction.getCategoryId()
                );
            }
        }

        return savedTransaction;
    }
}
