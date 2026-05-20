package com.example.smart_expense.service;

import com.example.smart_expense.model.RecurringTransaction;
import com.example.smart_expense.model.Transaction;
import com.example.smart_expense.repository.RecurringTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;

    public RecurringTransactionScheduler(RecurringTransactionRepository recurringTransactionRepository,
                                         TransactionService transactionService) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionService = transactionService;
    }

    /**
     * Chạy định kỳ lúc 01:00 AM hàng ngày để quét và xử lý các giao dịch định kỳ đến hạn.
     * Cron format: "0 0 1 * * ?" (giây phút giờ ngày_trong_tháng tháng ngày_trong_tuần)
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processRecurringTransactions() {
        logger.info("Bắt đầu quét giao dịch định kỳ tự động...");
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository.findActiveAndDue();
        logger.info("Tìm thấy {} giao dịch định kỳ đến hạn hoặc quá hạn.", dueTransactions.size());

        for (RecurringTransaction rt : dueTransactions) {
            try {
                processSingleTransaction(rt);
            } catch (Exception e) {
                logger.error("Lỗi khi xử lý giao dịch định kỳ ID {}: {}", rt.getRecurringId(), e.getMessage(), e);
            }
        }
        logger.info("Hoàn thành quét giao dịch định kỳ tự động.");
    }

    @Transactional
    public void processSingleTransaction(RecurringTransaction rt) {
        // 1. Tạo và lưu giao dịch thông thường
        Transaction transaction = Transaction.builder()
                .userId(rt.getUserId())
                .walletId(rt.getWalletId())
                .categoryId(rt.getCategoryId())
                .amount(rt.getAmount())
                .transactionDate(LocalDate.now())
                .note(rt.getNote() != null ? rt.getNote() + " (Định kỳ tự động)" : "Giao dịch định kỳ tự động")
                .build();

        transactionService.createTransaction(transaction);
        logger.info("Đã tạo giao dịch định kỳ tự động cho user {} với số tiền {}", rt.getUserId(), rt.getAmount());

        // 2. Tính ngày đến hạn tiếp theo
        LocalDate currentDueDate = rt.getNextDueDate();
        LocalDate nextDueDate = currentDueDate;
        
        String freq = rt.getFrequency() != null ? rt.getFrequency().toUpperCase() : "MONTHLY";
        switch (freq) {
            case "DAILY":
                nextDueDate = currentDueDate.plusDays(1);
                break;
            case "WEEKLY":
                nextDueDate = currentDueDate.plusWeeks(1);
                break;
            case "MONTHLY":
                nextDueDate = currentDueDate.plusMonths(1);
                break;
            case "YEARLY":
                nextDueDate = currentDueDate.plusYears(1);
                break;
            default:
                nextDueDate = currentDueDate.plusMonths(1);
                break;
        }

        // Đảm bảo nextDueDate không nằm ở quá khứ so với hôm nay (trong trường hợp hệ thống tắt lâu ngày)
        while (nextDueDate.isBefore(LocalDate.now()) || nextDueDate.isEqual(LocalDate.now())) {
            switch (freq) {
                case "DAILY":
                    nextDueDate = nextDueDate.plusDays(1);
                    break;
                case "WEEKLY":
                    nextDueDate = nextDueDate.plusWeeks(1);
                    break;
                case "MONTHLY":
                    nextDueDate = nextDueDate.plusMonths(1);
                    break;
                case "YEARLY":
                    nextDueDate = nextDueDate.plusYears(1);
                    break;
                default:
                    nextDueDate = nextDueDate.plusMonths(1);
                    break;
            }
        }

        // 3. Cập nhật ngày đến hạn tiếp theo vào CSDL
        recurringTransactionRepository.updateNextDueDate(rt.getRecurringId(), nextDueDate);
        logger.info("Đã cập nhật ngày đến hạn tiếp theo cho giao dịch định kỳ ID {} từ {} thành {}", 
                rt.getRecurringId(), currentDueDate, nextDueDate);
    }
}
