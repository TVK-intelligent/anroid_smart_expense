package com.example.smart_expense.service;

import com.example.smart_expense.dto.ChatResponse;
import com.example.smart_expense.dto.DashboardResponse;
import com.example.smart_expense.dto.TopExpenseCategory;
import com.example.smart_expense.model.Budget;
import com.example.smart_expense.model.Wallet;
import com.example.smart_expense.repository.BudgetRepository;
import com.example.smart_expense.repository.CategoryRepository;
import com.example.smart_expense.repository.TransactionRepository;
import com.example.smart_expense.repository.WalletRepository;
import com.example.smart_expense.repository.SavingsGoalRepository;
import com.example.smart_expense.model.SavingsGoal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ChatService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final DashboardService dashboardService;
    private final SmartAnalyticsService smartAnalyticsService;
    private final SavingsGoalRepository savingsGoalRepository;

    public ChatService(WalletRepository walletRepository,
                       TransactionRepository transactionRepository,
                       BudgetRepository budgetRepository,
                       CategoryRepository categoryRepository,
                       DashboardService dashboardService,
                       SmartAnalyticsService smartAnalyticsService,
                       SavingsGoalRepository savingsGoalRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.dashboardService = dashboardService;
        this.smartAnalyticsService = smartAnalyticsService;
        this.savingsGoalRepository = savingsGoalRepository;
    }

    public ChatResponse generateReply(Integer userId, String message) {
        if (userId == null || message == null || message.trim().isEmpty()) {
            return ChatResponse.builder()
                    .reply("Chào bạn! Tôi có thể giúp gì cho bạn trong việc quản lý chi tiêu hôm nay?")
                    .suggestions(Arrays.asList("Xem ví tiền", "Chi tiêu tháng này", "Tư vấn tiết kiệm"))
                    .build();
        }

        String msgLower = message.toLowerCase().trim();

        // 1. INTENT: WALLET / BALANCES / ASSETS
        if (containsAny(msgLower, "ví", "so du", "số dư", "tài sản", "còn bao nhiêu tiền", "tiền của tôi", "wallet", "balance")) {
            return handleWalletIntent(userId);
        }

        // 2. INTENT: BUDGETS
        if (containsAny(msgLower, "ngân sách", "hạn mức", "vượt mức", "cảnh báo", "budget")) {
            return handleBudgetIntent(userId);
        }

        // 3. INTENT: EXPENSES / INCOMES / DASHBOARD
        if (containsAny(msgLower, "tiêu bao nhiêu", "chi tiêu", "đã tiêu", "thu nhập", "kiếm được", "thống kê", "dashboard", "giao dịch")) {
            return handleDashboardIntent(userId);
        }

        // 4. INTENT: SAVINGS RECOMMENDATIONS
        if (containsAny(msgLower, "tiết kiệm", "đề xuất", "tư vấn", "lời khuyên", "saving")) {
            return handleSavingsIntent(userId);
        }

        // 5. FALLBACK / GENERAL FINANCE ADVISOR
        return handleFallbackIntent(userId, msgLower);
    }

    private ChatResponse handleWalletIntent(Integer userId) {
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        if (wallets.isEmpty()) {
            return ChatResponse.builder()
                    .reply("Chào bạn! Hiện tại bạn chưa thiết lập ví tiền nào trong tài khoản. Hãy tạo ví đầu tiên để tôi có thể giúp bạn quản lý số dư nhé!")
                    .suggestions(Collections.singletonList("Tạo ví mới"))
                    .build();
        }

        BigDecimal total = walletRepository.getTotalBalanceByUserId(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("💰 **Thông tin tài sản của bạn:**\n\n");
        sb.append(String.format("• **Tổng số dư:** %sđ\n", formatCurrency(total)));
        sb.append("• **Chi tiết từng ví:**\n");

        for (Wallet w : wallets) {
            String typeText = getWalletTypeText(w.getType());
            sb.append(String.format("   - **%s** (%s): %sđ\n", w.getName(), typeText, formatCurrency(w.getBalance())));
        }

        sb.append("\nBạn có muốn thực hiện ghi chép giao dịch chi tiêu mới từ ví nào không?");

        return ChatResponse.builder()
                .reply(sb.toString())
                .suggestions(Arrays.asList("Thêm giao dịch", "Chi tiêu tháng này", "Xem ngân sách"))
                .build();
    }

    private ChatResponse handleBudgetIntent(Integer userId) {
        List<Budget> activeBudgets = budgetRepository.findActiveBudgets(userId);
        if (activeBudgets.isEmpty()) {
            return ChatResponse.builder()
                    .reply("Hiện tại bạn chưa thiết lập bất kỳ ngân sách chi tiêu nào trong tháng này. Tôi đề xuất bạn nên đặt hạn mức ngân sách cho các danh mục dễ phát sinh chi tiêu như 'Ăn uống' hay 'Mua sắm' để tránh vung tay quá trán nhé!")
                    .suggestions(Arrays.asList("Đặt ngân sách mới", "Chi tiêu tháng này"))
                    .build();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📅 **Tình hình ngân sách tháng này:**\n\n");
        LocalDate today = LocalDate.now();

        boolean hasWarning = false;
        for (Budget b : activeBudgets) {
            if (b.getAmount() == null || b.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;
            if (today.isBefore(b.getStartDate()) || today.isAfter(b.getEndDate())) continue;

            BigDecimal spent = transactionRepository.getTotalExpenseByCategoryAndPeriod(userId, b.getCategoryId(), b.getStartDate(), b.getEndDate());
            BigDecimal percent = spent.multiply(new BigDecimal("100")).divide(b.getAmount(), 2, RoundingMode.HALF_UP);
            String categoryName = resolveCategoryName(b.getCategoryId());

            String statusIcon = "✅";
            if (spent.compareTo(b.getAmount()) > 0) {
                statusIcon = "🚨 (ĐÃ VƯỢT HẠN MỨC)";
                hasWarning = true;
            } else if (percent.compareTo(new BigDecimal("80.0")) >= 0) {
                statusIcon = "⚠️ (SẮP VƯỢT HẠN MỨC)";
                hasWarning = true;
            }

            sb.append(String.format("%s **%s**:\n", statusIcon, categoryName));
            sb.append(String.format("   - Đã chi: %sđ / Hạn mức: %sđ (%s%%)\n", 
                    formatCurrency(spent), formatCurrency(b.getAmount()), percent.setScale(0, RoundingMode.HALF_UP)));
            sb.append(String.format("   - Còn lại: %sđ\n\n", formatCurrency(b.getAmount().subtract(spent))));
        }

        if (hasWarning) {
            sb.append("💡 **Lời khuyên:** Bạn đang có ngân sách sắp hoặc đã vượt hạn mức. Hãy cân nhắc cắt giảm chi tiêu ở danh mục này trong những ngày tới nhé!");
        } else {
            sb.append("🎉 Tuyệt vời! Tất cả ngân sách của bạn hiện đều nằm trong tầm kiểm soát an toàn.");
        }

        return ChatResponse.builder()
                .reply(sb.toString())
                .suggestions(Arrays.asList("Tư vấn tiết kiệm", "Xem lịch sử giao dịch", "Ví tiền của tôi"))
                .build();
    }

    private ChatResponse handleDashboardIntent(Integer userId) {
        DashboardResponse db = dashboardService.getDashboard(userId);
        StringBuilder sb = new StringBuilder();
        
        sb.append("📊 **Thống kê chi tiêu tài chính tháng này:**\n\n");
        sb.append(String.format("• **Tổng thu nhập (lương/thu khác):** +%sđ\n", formatCurrency(db.getMonthlyIncome())));
        sb.append(String.format("• **Tổng chi tiêu:** -%sđ\n", formatCurrency(db.getMonthlyExpense())));
        
        BigDecimal net = db.getMonthlyNet();
        if (net.compareTo(BigDecimal.ZERO) >= 0) {
            sb.append(String.format("• **Thặng dư tích lũy:** +%sđ (Dòng tiền Dương) 🟢\n\n", formatCurrency(net)));
        } else {
            sb.append(String.format("• **Thâm hụt chi tiêu:** %sđ (Dòng tiền Âm) 🔴\n\n", formatCurrency(net)));
        }

        if (!db.getTopExpenseCategories().isEmpty()) {
            sb.append("🔥 **3 danh mục bạn chi nhiều nhất:**\n");
            for (int i = 0; i < db.getTopExpenseCategories().size(); i++) {
                TopExpenseCategory item = db.getTopExpenseCategories().get(i);
                sb.append(String.format("   %d. **%s**: %sđ\n", i+1, item.getCategoryName(), formatCurrency(item.getTotalSpent())));
            }
            sb.append("\n");
        }

        sb.append("Bạn muốn xem kỹ hơn các giao dịch gần đây hay cần tôi tư vấn tiết kiệm?");

        return ChatResponse.builder()
                .reply(sb.toString())
                .suggestions(Arrays.asList("Tư vấn tiết kiệm", "Xem ngân sách", "Ví tiền của tôi"))
                .build();
    }

    private ChatResponse handleSavingsIntent(Integer userId) {
        List<Map<String, Object>> recommendations = smartAnalyticsService.getSavingsOptimizationSuggestion(userId);
        if (recommendations.isEmpty()) {
            // Check if user has goals but maybe no cash flow, or simply no goals
            BigDecimal totalBalance = walletRepository.getTotalBalanceByUserId(userId);
            if (totalBalance.compareTo(BigDecimal.ZERO) <= 0) {
                return ChatResponse.builder()
                        .reply("Hiện tại tổng số dư trong các ví của bạn đang ở mức 0đ hoặc âm. Lời khuyên đầu tiên của tôi là hãy tập trung tối ưu hóa các khoản chi tiêu thiết yếu và ghi chép đầy đủ để tìm ra điểm rò rỉ dòng tiền trước khi lập kế hoạch tích lũy nhé!")
                        .suggestions(Arrays.asList("Thêm giao dịch", "Xem ví tiền"))
                        .build();
            } else {
                return ChatResponse.builder()
                        .reply("Bạn chưa lập mục tiêu tiết kiệm (Savings Goal) nào trong ứng dụng. Tích lũy có mục đích rõ ràng sẽ giúp bạn có động lực hơn 200%. Bạn có muốn tôi giúp lên kế hoạch tích lũy cho mục tiêu nào không (như Mua Laptop, Quỹ dự phòng, Đi du lịch...)?")
                        .suggestions(Arrays.asList("Tạo mục tiêu tiết kiệm", "Xem ví tiền"))
                        .build();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("💡 **Đề xuất phân bổ dòng tiền nhàn rỗi thông minh:**\n");
        sb.append("Dựa trên chênh lệch thu nhập - chi phí định kỳ, tôi đề xuất phân bổ số tiền nhàn rỗi tháng này vào các heo đất tiết kiệm của bạn như sau:\n\n");

        for (Map<String, Object> rec : recommendations) {
            Integer goalId = (Integer) rec.get("goalId");
            BigDecimal allocated = (BigDecimal) rec.get("allocatedAmount");
            String percent = (String) rec.get("allocationPercent");
            
            String goalName = resolveGoalName(goalId);
            sb.append(String.format("• 🎯 Mục tiêu **%s**:\n", goalName));
            sb.append(String.format("   - Phân bổ: **%sđ** (%s dòng tiền dư)\n", formatCurrency(allocated), percent));
            sb.append(String.format("   - Đã tích lũy: %sđ / Mục tiêu: %sđ\n\n", 
                    formatCurrency((BigDecimal) rec.get("currentAmount")), formatCurrency((BigDecimal) rec.get("targetAmount"))));
        }

        sb.append("Việc tích lũy tự động này sẽ giúp bạn nhanh chóng đạt mục tiêu đúng thời hạn hạn chót!");

        return ChatResponse.builder()
                .reply(sb.toString())
                .suggestions(Arrays.asList("Xem ví tiền", "Xem ngân sách", "Chi tiêu tháng này"))
                .build();
    }

    private ChatResponse handleFallbackIntent(Integer userId, String message) {
        // Retrieve some user stats to make the fallback response extremely personal!
        BigDecimal total = walletRepository.getTotalBalanceByUserId(userId);
        DashboardResponse db = dashboardService.getDashboard(userId);
        BigDecimal net = db.getMonthlyNet();

        StringBuilder sb = new StringBuilder();
        
        if (message.contains("xin chào") || message.contains("hello") || message.contains("chào")) {
            sb.append("Chào bạn! Tôi là trợ lý tài chính cá nhân thông minh của SmartExpense. 🙋‍♂️\n\n");
            sb.append(String.format("Hôm nay, bạn đang có tổng tài sản là **%sđ** trong ví. ", formatCurrency(total)));
            if (net.compareTo(BigDecimal.ZERO) < 0) {
                sb.append("Hãy lưu ý là tháng này bạn đang chi tiêu vượt dòng tiền thu nhập. ");
            } else {
                sb.append("Bạn đang quản lý tài chính rất tốt đấy! ");
            }
            sb.append("Bạn muốn hỏi tôi về ví tiền, ngân sách chi tiêu, hay đề xuất tích lũy tiết kiệm?");
        } else if (message.contains("làm sao") || message.contains("bí quyết") || message.contains("làm thế nào") || message.contains("tiết kiệm")) {
            sb.append("💡 **5 quy tắc vàng giúp bạn quản lý tài chính hiệu quả hơn cùng SmartExpense:**\n\n");
            sb.append("1. **Áp dụng Quy tắc 50/30/20**: 50% cho nhu cầu thiết yếu, 30% cho sở thích, và 20% tích lũy đầu tư.\n");
            sb.append("2. **Đặt ngân sách cho từng danh mục**: Hãy giới hạn chi tiêu ăn uống hay mua sắm trong tầm kiểm soát.\n");
            sb.append("3. **Ghi chép giao dịch lập tức**: Thao tác thêm giao dịch chỉ tốn 10 giây nhưng giúp bạn kiểm soát 100% dòng tiền.\n");
            sb.append("4. **Tích lũy trước, chi tiêu sau**: Ngay khi nhận thu nhập, hãy trích ít nhất 10-20% vào heo đất tiết kiệm.\n");
            sb.append("5. **Quét bất thường**: Sử dụng tính năng quét chi tiêu bất thường khi nhập giao dịch lớn để cân nhắc lại trước khi mua.\n\n");
            sb.append("Hãy thử hỏi tôi câu khác như: *'Tình hình ngân sách'* hoặc *'Tổng số dư'* để tôi kiểm tra giúp bạn.");
        } else {
            sb.append("Tôi hiểu ý bạn là đang quan tâm đến tình hình tài chính của mình. ");
            sb.append(String.format("Hiện tại bạn đang có **%sđ** trong ví tiền. ", formatCurrency(total)));
            if (!db.getTopExpenseCategories().isEmpty()) {
                sb.append(String.format("Trong đó danh mục chi tiêu lớn nhất của bạn là **%s**. ", db.getTopExpenseCategories().get(0).getCategoryName()));
            }
            sb.append("\n\nBạn có thể thử đặt các câu hỏi rõ ràng hơn như:\n");
            sb.append("• *Ví tôi còn bao nhiêu tiền?*\n");
            sb.append("• *Thống kê chi tiêu tháng này*\n");
            sb.append("• *Kiểm tra ngân sách của tôi*\n");
            sb.append("• *Tư vấn tiết kiệm thông minh*");
        }

        return ChatResponse.builder()
                .reply(sb.toString())
                .suggestions(Arrays.asList("Xem ví tiền", "Chi tiêu tháng này", "Tư vấn tiết kiệm"))
                .build();
    }

    // Helper utilities
    private boolean containsAny(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        try {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            return nf.format(amount.setScale(0, RoundingMode.HALF_UP));
        } catch (Exception e) {
            return amount.setScale(0, RoundingMode.HALF_UP).toString();
        }
    }

    private String getWalletTypeText(String type) {
        if (type == null) return "Khác";
        switch (type.trim().toUpperCase()) {
            case "CASH": return "Tiền mặt";
            case "BANK": return "Ngân hàng";
            case "E-WALLET": return "Ví điện tử";
            default: return type;
        }
    }

    private String resolveCategoryName(Integer categoryId) {
        if (categoryId == null) return "Chưa phân loại";
        return categoryRepository.findById(categoryId)
                .map(c -> c.getName())
                .orElse("Chưa phân loại");
    }

    private String resolveGoalName(Integer goalId) {
        if (goalId == null) return "Mục tiêu";
        return savingsGoalRepository.findById(goalId)
                .map(SavingsGoal::getGoalName)
                .orElse("Mục tiêu");
    }
}
