package com.example.smart_expense.service;

import com.example.smart_expense.model.Budget;
import com.example.smart_expense.model.Notification;
import com.example.smart_expense.model.SavingsGoal;
import com.example.smart_expense.repository.BudgetRepository;
import com.example.smart_expense.repository.NotificationRepository;
import com.example.smart_expense.repository.RecurringTransactionRepository;
import com.example.smart_expense.repository.SavingsGoalRepository;
import com.example.smart_expense.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SmartAnalyticsService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final NotificationRepository notificationRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final com.example.smart_expense.repository.CategoryRepository categoryRepository;

    public SmartAnalyticsService(TransactionRepository transactionRepository,
                                 BudgetRepository budgetRepository,
                                 SavingsGoalRepository savingsGoalRepository,
                                 NotificationRepository notificationRepository,
                                 RecurringTransactionRepository recurringTransactionRepository,
                                 com.example.smart_expense.repository.CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.notificationRepository = notificationRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 1. Phát hiện Bất thường (Anomaly Detection)
     * So sánh giao dịch hiện tại với trung bình lịch sử 90 ngày qua.
     * Nếu giao dịch lớn hơn 200% trung bình, ghi nhận bất thường và lưu notification.
     */
    public String checkForAnomaly(Integer userId, Integer categoryId, BigDecimal amount) {
        // Lấy trung bình 90 ngày của category chi tiêu
        BigDecimal avgExpense = transactionRepository.getAverageExpenseForCategory(userId, categoryId, 90);
        
        // Nếu chưa có giao dịch nào trước đó (avg = 0), không coi là bất thường
        if (avgExpense.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // Ngưỡng bất thường: 200% (2.0 lần)
        BigDecimal threshold = avgExpense.multiply(new BigDecimal("2.0"));

        if (amount.compareTo(threshold) > 0) {
            // Tạo cảnh báo bất thường
            String title = "Cảnh báo: Chi tiêu bất thường!";
            String content = String.format("Hệ thống phát hiện giao dịch chi tiêu mới có giá trị %sđ cao bất thường so với mức trung bình lịch sử (%sđ) của danh mục này. Bạn có muốn xem lại không?", 
                    amount.setScale(0, RoundingMode.HALF_UP), avgExpense.setScale(0, RoundingMode.HALF_UP));

            Notification notification = Notification.builder()
                    .userId(userId)
                    .title(title)
                    .content(content)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            return content;
        }
        return null;
    }

    /**
     * 2. Phân tích Tốc độ Tiêu tiền (Burn Rate) & Đề xuất Ngân sách Hàng ngày
     * Hỗ trợ quét tất cả ngân sách hoặc so sánh tổng chi tiêu với thu nhập nếu chưa có ngân sách.
     */
    public Optional<String> checkBurnRateAndSuggest(Integer userId, Integer categoryId) {
        List<Budget> budgetsToCheck = new ArrayList<>();
        
        // Nếu truyền categoryId cụ thể, ưu tiên kiểm tra category đó
        if (categoryId != null) {
            Optional<Budget> spec = budgetRepository.findActiveBudgetByCategory(userId, categoryId);
            if (spec.isPresent()) {
                budgetsToCheck.add(spec.get());
            }
        }
        
        // Nếu không có budget cụ thể cho category truyền vào, lấy toàn bộ budget của user
        if (budgetsToCheck.isEmpty()) {
            budgetsToCheck.addAll(budgetRepository.findActiveBudgets(userId));
        }

        LocalDate today = LocalDate.now();

        // TH1: Có ngân sách thiết lập -> Tìm ngân sách có tốc độ tiêu nhanh nhất
        if (!budgetsToCheck.isEmpty()) {
            String worstAlert = null;
            double worstExcessRatio = 0.0;

            for (Budget budget : budgetsToCheck) {
                if (today.isBefore(budget.getStartDate()) || today.isAfter(budget.getEndDate())) {
                    continue;
                }

                long totalDays = ChronoUnit.DAYS.between(budget.getStartDate(), budget.getEndDate()) + 1;
                long daysPassed = ChronoUnit.DAYS.between(budget.getStartDate(), today) + 1;
                long daysRemaining = totalDays - daysPassed;
                if (daysRemaining <= 0) continue;

                BigDecimal totalSpent = transactionRepository.getTotalSpentByCategoryAndPeriod(userId, budget.getCategoryId(), budget.getStartDate(), today);
                BigDecimal budgetAmount = budget.getAmount();
                if (budgetAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

                double timeRatio = (double) daysPassed / totalDays;
                double spendRatio = totalSpent.divide(budgetAmount, 4, RoundingMode.HALF_UP).doubleValue();
                double warningThreshold = timeRatio * 1.15; // Cảnh báo nếu tiêu nhanh hơn thời gian trôi qua 15%

                if (spendRatio > warningThreshold) {
                    double excessRatio = spendRatio - warningThreshold;
                    if (excessRatio > worstExcessRatio) {
                        worstExcessRatio = excessRatio;
                        BigDecimal remainingBudget = budgetAmount.subtract(totalSpent);
                        BigDecimal safeDailyLimit = remainingBudget.compareTo(BigDecimal.ZERO) > 0 
                                ? remainingBudget.divide(BigDecimal.valueOf(daysRemaining), 0, RoundingMode.DOWN)
                                : BigDecimal.ZERO;
                        
                        String catName = categoryRepository.findById(budget.getCategoryId())
                                .map(c -> c.getName())
                                .orElse("Danh mục " + budget.getCategoryId());

                        worstAlert = String.format("Tốc độ chi tiêu cho '%s' đang quá nhanh! Đã tiêu hết %.0f%% ngân sách trong %.0f%% thời gian của tháng (%d/%d ngày). Khuyên dùng tối đa %sđ/ngày cho những ngày còn lại.",
                                catName,
                                spendRatio * 100.0,
                                timeRatio * 100.0,
                                daysPassed, totalDays,
                                safeDailyLimit.setScale(0, RoundingMode.HALF_UP).toString());
                    }
                }
            }

            if (worstAlert != null) {
                Notification notification = Notification.builder()
                        .userId(userId)
                        .title("Cảnh báo tốc độ chi tiêu!")
                        .content(worstAlert)
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                return Optional.of(worstAlert);
            }
        }

        // TH2: Không có ngân sách hoặc các ngân sách đều an toàn -> Check tổng chi tiêu so với thu nhập
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal monthlyExpense = transactionRepository.getMonthlyTotalByType(userId, "EXPENSE", startOfMonth, endOfMonth);
        BigDecimal monthlyIncome = transactionRepository.getMonthlyTotalByType(userId, "INCOME", startOfMonth, endOfMonth);
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            monthlyIncome = recurringTransactionRepository.getTotalExpectedFixedIncomesForMonth(userId);
        }
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            monthlyIncome = new BigDecimal("10000000"); // 10 Triệu VND mặc định
        }

        long totalDays = ChronoUnit.DAYS.between(startOfMonth, endOfMonth) + 1;
        long daysPassed = ChronoUnit.DAYS.between(startOfMonth, today) + 1;
        long daysRemaining = totalDays - daysPassed;

        if (monthlyExpense != null && monthlyExpense.compareTo(BigDecimal.ZERO) > 0) {
            double timeRatio = (double) daysPassed / totalDays;
            double spendRatio = monthlyExpense.divide(monthlyIncome, 4, RoundingMode.HALF_UP).doubleValue();
            
            // Cảnh báo nếu chi tiêu tháng quá lớn so với mốc thời gian hoặc chi tiêu vượt 10 triệu
            if (spendRatio > timeRatio * 1.15 || monthlyExpense.compareTo(new BigDecimal("10000000")) > 0) {
                BigDecimal remainingIncome = monthlyIncome.subtract(monthlyExpense);
                BigDecimal safeDailyLimit = remainingIncome.compareTo(BigDecimal.ZERO) > 0 && daysRemaining > 0
                        ? remainingIncome.divide(BigDecimal.valueOf(daysRemaining), 0, RoundingMode.DOWN)
                        : BigDecimal.ZERO;

                String alertContent = String.format("Cảnh báo dòng tiền: Tổng chi tiêu tháng này (%sđ) đang chiếm %.0f%% thu nhập dự kiến (%sđ) trong khi mới trôi qua %.0f%% thời gian (%d/%d ngày). Bạn nên giới hạn chi tiêu dưới %sđ/ngày.",
                        monthlyExpense.setScale(0, RoundingMode.HALF_UP).toString(),
                        spendRatio * 100.0,
                        monthlyIncome.setScale(0, RoundingMode.HALF_UP).toString(),
                        timeRatio * 100.0,
                        daysPassed, totalDays,
                        safeDailyLimit.setScale(0, RoundingMode.HALF_UP).toString());

                Notification notification = Notification.builder()
                        .userId(userId)
                        .title("Cảnh báo dòng tiền chi tiêu!")
                        .content(alertContent)
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);

                return Optional.of(alertContent);
            }
        }
        return Optional.empty();
    }

    /**
     * 3. Tối ưu Kế hoạch Tiết kiệm (Savings Goals Allocator)
     * Lấy (Thu nhập cố định - Chi phí cố định - Tổng hạn mức ngân sách hoạt động) = Dòng tiền nhàn rỗi thật
     * Phân bổ dòng tiền dư tự động vào các savings goals đang chạy theo độ ưu tiên deadline.
     */
    public List<Map<String, Object>> getSavingsOptimizationSuggestion(Integer userId) {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // Tính thu nhập cố định và chi phí cố định định kỳ hàng tháng
        BigDecimal fixedIncome = recurringTransactionRepository.getTotalExpectedFixedIncomesForMonth(userId);
        BigDecimal fixedExpense = recurringTransactionRepository.getTotalExpectedFixedExpensesForMonth(userId);

        // Lấy tổng hạn mức tất cả các ngân sách chi tiêu hàng ngày đang hoạt động của người dùng
        List<Budget> activeBudgets = budgetRepository.findActiveBudgets(userId);
        BigDecimal totalActiveBudgets = BigDecimal.ZERO;
        for (Budget b : activeBudgets) {
            if (b.getAmount() != null) {
                totalActiveBudgets = totalActiveBudgets.add(b.getAmount());
            }
        }

        // Dòng tiền dư hàng tháng thực tế (Chừa lại tiền đóng phí cố định và tiền ăn tiêu sinh hoạt hàng ngày)
        BigDecimal monthlySurplus = fixedIncome.subtract(fixedExpense).subtract(totalActiveBudgets);

        if (monthlySurplus.compareTo(BigDecimal.ZERO) <= 0) {
            // Không có tiền nhàn rỗi thật sự để gợi ý tích lũy
            return suggestions;
        }

        // Lấy các mục tiêu tích lũy đang chạy, sắp xếp theo deadline gần nhất
        List<SavingsGoal> activeGoals = savingsGoalRepository.findActiveGoalsOrderByDeadline(userId);
        if (activeGoals.isEmpty()) {
            return suggestions;
        }

        // Áp dụng thuật toán phân bổ rule-based:
        // - Mục tiêu có deadline gần nhất nhận 50% dòng dư.
        // - Mục tiêu thứ hai nhận 30% dòng dư.
        // - Các mục tiêu còn lại chia đều 20% dòng dư.
        
        BigDecimal surplusLeft = monthlySurplus;
        
        for (int i = 0; i < activeGoals.size(); i++) {
            SavingsGoal goal = activeGoals.get(i);
            BigDecimal allocationPercent;
            BigDecimal allocatedAmount;

            if (i == 0) {
                // Mục tiêu khẩn cấp nhất (deadline gần nhất)
                allocationPercent = activeGoals.size() == 1 ? new BigDecimal("1.00") : new BigDecimal("0.50");
            } else if (i == 1) {
                allocationPercent = activeGoals.size() == 2 ? new BigDecimal("0.50") : new BigDecimal("0.30");
            } else {
                // Chia đều phần còn lại cho các mục tiêu tiếp theo
                int remainingGoals = activeGoals.size() - 2;
                allocationPercent = new BigDecimal("0.20").divide(BigDecimal.valueOf(remainingGoals), 4, RoundingMode.HALF_UP);
            }

            allocatedAmount = monthlySurplus.multiply(allocationPercent).setScale(0, RoundingMode.HALF_UP);
            surplusLeft = surplusLeft.subtract(allocatedAmount);

            // Kiểm tra số tiền còn thiếu để hoàn thành mục tiêu
            BigDecimal neededAmount = goal.getTargetAmount().subtract(goal.getCurrentAmount());
            if (allocatedAmount.compareTo(neededAmount) > 0) {
                allocatedAmount = neededAmount; // Không phân bổ vượt mức target
            }

            if (allocatedAmount.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("goalId", goal.getGoalId());
                suggestion.put("deadline", goal.getDeadline());
                suggestion.put("targetAmount", goal.getTargetAmount());
                suggestion.put("currentAmount", goal.getCurrentAmount());
                suggestion.put("allocatedAmount", allocatedAmount);
                suggestion.put("allocationPercent", allocationPercent.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP) + "%");
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }
}
