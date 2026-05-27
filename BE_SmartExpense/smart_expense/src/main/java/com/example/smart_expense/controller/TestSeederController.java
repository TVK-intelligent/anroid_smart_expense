package com.example.smart_expense.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestSeederController implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public TestSeederController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Run database seeding on startup if the test user doesn't exist yet
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users WHERE email = 'test@smartexpense.com'", Integer.class);
            if (count == null || count == 0) {
                System.out.println(">>> SmartExpense Startup: Seeding comprehensive test account 'test@smartexpense.com'...");
                seedDataInternal();
                System.out.println(">>> SmartExpense Startup: Seeded successfully!");
            } else {
                System.out.println(">>> SmartExpense Startup: Test account 'test@smartexpense.com' already exists. Skipping startup seed.");
            }
        } catch (Exception e) {
            System.err.println(">>> SmartExpense Startup Error during seeding: " + e.getMessage());
        }
    }

    /**
     * API to manually reset and seed the mock data at any time.
     * POST /api/test/seed
     */
    @PostMapping("/seed")
    public ResponseEntity<?> seedMockData() {
        try {
            Map<String, Object> result = seedDataInternal();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi trong quá trình tạo dữ liệu thử nghiệm!");
            errorResponse.put("details", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Transactional
    protected Map<String, Object> seedDataInternal() {
        Map<String, Object> response = new HashMap<>();

        // 1. Khởi tạo danh mục mặc định hệ thống (nếu chưa có)
        ensureSystemCategories();

        // 2. Tìm kiếm và xóa tài khoản test cũ để tránh trùng lặp dữ liệu
        Integer testUserId = null;
        try {
            testUserId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM users WHERE email = 'test@smartexpense.com'", Integer.class);
        } catch (Exception e) {
            // Không tìm thấy, coi như chưa tồn tại
        }

        if (testUserId != null) {
            cleanupUserData(testUserId);
        }

        // 3. Tạo người dùng test mới
        // Password hash là 'password123'
        jdbcTemplate.update(
                "INSERT INTO users (name, email, password_hash, currency, created_at) VALUES (?, ?, ?, ?, NOW())",
                "Nguyễn Văn A", "test@smartexpense.com", "password123", "VND"
        );
        testUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM users WHERE email = 'test@smartexpense.com'", Integer.class);

        if (testUserId == null) {
            throw new RuntimeException("Không thể tạo tài khoản người dùng test!");
        }

        // 4. Tạo các ví cho người dùng test
        // Ví Tiền mặt (CASH) - 5,000,000 VND
        jdbcTemplate.update(
                "INSERT INTO wallets (user_id, name, balance, type, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Tiền mặt", new BigDecimal("5000000.00"), "CASH"
        );
        Integer walletIdCash = jdbcTemplate.queryForObject(
                "SELECT wallet_id FROM wallets WHERE user_id = ? AND name = 'Tiền mặt' LIMIT 1", Integer.class, testUserId);

        // Ví Ngân hàng Vietcombank (BANK) - 25,000,000 VND
        jdbcTemplate.update(
                "INSERT INTO wallets (user_id, name, balance, type, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Tài khoản Vietcombank", new BigDecimal("25000000.00"), "BANK"
        );
        Integer walletIdBank = jdbcTemplate.queryForObject(
                "SELECT wallet_id FROM wallets WHERE user_id = ? AND name = 'Tài khoản Vietcombank' LIMIT 1", Integer.class, testUserId);

        // Ví điện tử MoMo (E-WALLET) - 2,500,000 VND
        jdbcTemplate.update(
                "INSERT INTO wallets (user_id, name, balance, type, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Ví MoMo", new BigDecimal("2500000.00"), "E-WALLET"
        );
        Integer walletIdMoMo = jdbcTemplate.queryForObject(
                "SELECT wallet_id FROM wallets WHERE user_id = ? AND name = 'Ví MoMo' LIMIT 1", Integer.class, testUserId);

        // 5. Lấy ID của các danh mục mặc định để liên kết
        Map<String, Integer> categoryMap = new HashMap<>();
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("SELECT category_id, name FROM categories WHERE user_id IS NULL");
        for (Map<String, Object> cat : categories) {
            categoryMap.put((String) cat.get("name"), (Integer) cat.get("category_id"));
        }

        Integer catFood = categoryMap.get("Ăn uống");
        Integer catTransport = categoryMap.get("Di chuyển");
        Integer catHousing = categoryMap.get("Nhà cửa");
        Integer catBills = categoryMap.get("Hóa đơn");
        Integer catShopping = categoryMap.get("Mua sắm");
        Integer catEntertainment = categoryMap.get("Giải trí");
        Integer catHealth = categoryMap.get("Sức khỏe");
        Integer catSalary = categoryMap.get("Lương");
        Integer catBonus = categoryMap.get("Thưởng");

        LocalDate today = LocalDate.now();

        // 6. Gieo dữ liệu giao dịch trong vòng 30 ngày qua
        // a. Các khoản THU (INCOME)
        // Lương tháng hiện tại: 30,000,000 VND chuyển vào Vietcombank
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdBank, catSalary, new BigDecimal("30000000.00"), Date.valueOf(today.minusDays(20)), "Lương tháng này", Timestamp.valueOf(LocalDateTime.now().minusDays(20))
        );
        // Tiền thưởng dự án: 2,000,000 VND chuyển vào Vietcombank
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdBank, catBonus, new BigDecimal("2000000.00"), Date.valueOf(today.minusDays(5)), "Thưởng nóng dự án đột xuất", Timestamp.valueOf(LocalDateTime.now().minusDays(5))
        );

        // b. Các khoản CHI (EXPENSE)
        // Tiền thuê nhà: 5,000,000 VND thanh toán từ Vietcombank
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdBank, catHousing, new BigDecimal("5000000.00"), Date.valueOf(today.minusDays(23)), "Tiền thuê nhà và phí dịch vụ tháng", Timestamp.valueOf(LocalDateTime.now().minusDays(23))
        );
        // Tiền điện nước: 1,150,000 VND thanh toán từ ví MoMo
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catBills, new BigDecimal("1150000.00"), Date.valueOf(today.minusDays(14)), "Hóa đơn điện nước tháng này", Timestamp.valueOf(LocalDateTime.now().minusDays(14))
        );
        // Tiền mạng Internet: 250,000 VND thanh toán từ ví MoMo
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catBills, new BigDecimal("250000.00"), Date.valueOf(today.minusDays(14)), "Tiền cước Internet Viettel", Timestamp.valueOf(LocalDateTime.now().minusDays(14))
        );
        // Mua quần áo: 650,000 VND thanh toán từ ví MoMo (Thuộc Mua sắm)
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catShopping, new BigDecimal("650000.00"), Date.valueOf(today.minusDays(10)), "Mua áo khoác và quần jean trên Shopee", Timestamp.valueOf(LocalDateTime.now().minusDays(10))
        );
        // Mua sách: 400,000 VND thanh toán từ ví MoMo (Thuộc Mua sắm -> Tổng cộng Mua sắm = 1,050,000 VND -> Vượt ngân sách 1,000,000 VND!)
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catShopping, new BigDecimal("400000.00"), Date.valueOf(today.minusDays(8)), "Mua sách kỹ năng sống và sách kinh doanh Tiki", Timestamp.valueOf(LocalDateTime.now().minusDays(8))
        );
        // Mua thuốc lá & vitamin: 350,000 VND từ MoMo
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catHealth, new BigDecimal("350000.00"), Date.valueOf(today.minusDays(1)), "Mua vitamin C và thuốc cảm cúm Pharmacity", Timestamp.valueOf(LocalDateTime.now().minusDays(1))
        );
        // Grab đi làm: 45,000 VND từ Cash
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdCash, catTransport, new BigDecimal("45000.00"), Date.valueOf(today.minusDays(12)), "Gọi xe GrabBike đi làm trời mưa", Timestamp.valueOf(LocalDateTime.now().minusDays(12))
        );
        // Grab đi uống cà phê: 60,000 VND từ MoMo
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdMoMo, catTransport, new BigDecimal("60000.00"), Date.valueOf(today.minusDays(2)), "Gọi xe GrabCar đi gặp bạn thân", Timestamp.valueOf(LocalDateTime.now().minusDays(2))
        );
        // Ăn tối gia đình: 850,000 VND từ Cash
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdCash, catFood, new BigDecimal("850000.00"), Date.valueOf(today.minusDays(9)), "Ăn lẩu cuối tuần cùng gia đình", Timestamp.valueOf(LocalDateTime.now().minusDays(9))
        );
        // Cà phê đồng nghiệp: 120,000 VND từ Cash
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdCash, catFood, new BigDecimal("120000.00"), Date.valueOf(today.minusDays(6)), "Cà phê Highland thảo luận dự án", Timestamp.valueOf(LocalDateTime.now().minusDays(6))
        );
        // Ăn trưa văn phòng: 1,200,000 VND (Cộng dồn nhiều ngày) thanh toán từ Cash (Ăn uống)
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdCash, catFood, new BigDecimal("1200000.00"), Date.valueOf(today.minusDays(4)), "Tiền ăn trưa văn phòng tích lũy 2 tuần qua", Timestamp.valueOf(LocalDateTime.now().minusDays(4))
        );
        // Mua thực phẩm: 450,000 VND từ Vietcombank (Ăn uống -> Tổng Ăn uống = 2,620,000 VND -> Gần đạt ngưỡng ngân sách 3,000,000 VND!)
        jdbcTemplate.update(
                "INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                testUserId, walletIdBank, catFood, new BigDecimal("450000.00"), Date.valueOf(today.minusDays(4)), "Mua rau củ, thịt cá sạch tại WinMart", Timestamp.valueOf(LocalDateTime.now().minusDays(4))
        );

        // 7. Gieo dữ liệu ngân sách (BUDGETS) trong tháng này
        // Ngân sách Ăn uống: 3,000,000 VND (Đã tiêu 2,620,000 VND ~ 87.3% -> Cảnh báo ngưỡng Vàng!)
        jdbcTemplate.update(
                "INSERT INTO budgets (user_id, category_id, amount, start_date, end_date, alert_threshold, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                testUserId, catFood, new BigDecimal("3000000.00"), Date.valueOf(today.withDayOfMonth(1)), Date.valueOf(today.withDayOfMonth(today.lengthOfMonth())), new BigDecimal("80.00")
        );
        // Ngân sách Mua sắm: 1,000,000 VND (Đã tiêu 1,050,000 VND ~ 105% -> Cảnh báo ngưỡng Đỏ!)
        jdbcTemplate.update(
                "INSERT INTO budgets (user_id, category_id, amount, start_date, end_date, alert_threshold, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                testUserId, catShopping, new BigDecimal("1000000.00"), Date.valueOf(today.withDayOfMonth(1)), Date.valueOf(today.withDayOfMonth(today.lengthOfMonth())), new BigDecimal("80.00")
        );

        // 8. Gieo dữ liệu mục tiêu tích lũy (SAVINGS GOALS)
        // Mục tiêu 1: Mua Laptop Mới (IN_PROGRESS)
        jdbcTemplate.update(
                "INSERT INTO savings_goals (user_id, goal_name, target_amount, current_amount, deadline, status, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Mua Laptop Mới", new BigDecimal("20000000.00"), new BigDecimal("8000000.00"), Date.valueOf(today.plusMonths(3)), "IN_PROGRESS"
        );
        // Mục tiêu 2: Quỹ Khẩn Cấp (COMPLETED)
        jdbcTemplate.update(
                "INSERT INTO savings_goals (user_id, goal_name, target_amount, current_amount, deadline, status, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Quỹ Khẩn Cấp", new BigDecimal("10000000.00"), new BigDecimal("10000000.00"), Date.valueOf(today.minusDays(1)), "COMPLETED"
        );
        // Mục tiêu 3: Du lịch Đà Lạt (IN_PROGRESS)
        jdbcTemplate.update(
                "INSERT INTO savings_goals (user_id, goal_name, target_amount, current_amount, deadline, status, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Du lịch Đà Lạt", new BigDecimal("5000000.00"), new BigDecimal("1500000.00"), Date.valueOf(today.plusMonths(1)), "IN_PROGRESS"
        );

        // 9. Gieo dữ liệu ghi nợ/cho vay (DEBTS & LOANS)
        // Cho Anh Tuấn vay tiền (LOAN) - 3,000,000 VND - Chưa đòi (UNPAID)
        jdbcTemplate.update(
                "INSERT INTO debt_loans (user_id, person_name, type, amount, interest_rate, due_date, status, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Anh Tuấn", "LOAN", new BigDecimal("3000000.00"), BigDecimal.ZERO, Date.valueOf(today.plusDays(15)), "UNPAID", "Cho đồng nghiệp vay mua điện thoại mới"
        );
        // Nợ Chị Thảo tiền mua đồ (DEBT) - 1,500,000 VND - Quá hạn thanh toán (UNPAID)
        jdbcTemplate.update(
                "INSERT INTO debt_loans (user_id, person_name, type, amount, interest_rate, due_date, status, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Chị Thảo", "DEBT", new BigDecimal("1500000.00"), BigDecimal.ZERO, Date.valueOf(today.minusDays(5)), "UNPAID", "Tiền nhờ mua hộ đồng phục bóng đá"
        );
        // Cho bạn Nam vay tiền (LOAN) - 500,000 VND - Đã trả đủ (PAID)
        jdbcTemplate.update(
                "INSERT INTO debt_loans (user_id, person_name, type, amount, interest_rate, due_date, status, note, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                testUserId, "Bạn Nam", "LOAN", new BigDecimal("500000.00"), BigDecimal.ZERO, Date.valueOf(today.minusDays(10)), "PAID", "Cho Nam mượn tiền ăn cưới"
        );

        // 10. Gieo giao dịch định kỳ (RECURRING TRANSACTIONS)
        // Tiền nhà định kỳ hàng tháng vào ngày 1
        jdbcTemplate.update(
                "INSERT INTO recurring_transactions (user_id, wallet_id, category_id, amount, frequency, next_due_date, note, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                testUserId, walletIdBank, catHousing, new BigDecimal("5000000.00"), "MONTHLY", Date.valueOf(today.plusMonths(1).withDayOfMonth(1)), "Thanh toán tiền thuê nhà định kỳ hàng tháng", true
        );
        // Tiền mạng Internet định kỳ hàng tháng vào ngày 10
        jdbcTemplate.update(
                "INSERT INTO recurring_transactions (user_id, wallet_id, category_id, amount, frequency, next_due_date, note, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                testUserId, walletIdMoMo, catBills, new BigDecimal("250000.00"), "MONTHLY", Date.valueOf(today.plusMonths(1).withDayOfMonth(10)), "Tiền cước cáp quang Internet định kỳ", true
        );

        // 11. Gieo dữ liệu thông báo (NOTIFICATIONS)
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, title, content, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Cảnh báo ngân sách Mua sắm", "Bạn đã chi tiêu 1,050,000 VND (vượt quá 100% ngân sách 1,000,000 VND) cho danh mục Mua sắm trong tháng này!", false
        );
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, title, content, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Cảnh báo nợ quá hạn", "Khoản nợ Chị Thảo với số tiền 1,500,000 VND đã quá hạn thanh toán 5 ngày! Vui lòng tất toán.", false
        );
        jdbcTemplate.update(
                "INSERT INTO notifications (user_id, title, content, is_read, created_at) VALUES (?, ?, ?, ?, NOW())",
                testUserId, "Nhắc nhở hóa đơn định kỳ", "Giao dịch định kỳ 'Tiền cước cáp quang Internet định kỳ' (250,000 VND) đã được tự động lên lịch và chuẩn bị thanh toán.", false
        );

        // Chuẩn bị thông tin phản hồi
        response.put("success", true);
        response.put("message", "Khởi tạo dữ liệu thử nghiệm (Mock Data) thành công!");
        response.put("account", Map.of(
                "email", "test@smartexpense.com",
                "password", "password123",
                "name", "Nguyễn Văn A"
        ));
        response.put("wallets", List.of(
                Map.of("name", "Tiền mặt", "type", "CASH", "balance", 5000000),
                Map.of("name", "Tài khoản Vietcombank", "type", "BANK", "balance", 25000000),
                Map.of("name", "Ví MoMo", "type", "E-WALLET", "balance", 2500000)
        ));
        response.put("transaction_count", 12);
        response.put("budgets_seeded", List.of("Ăn uống (Cảnh báo vàng: 87.3%)", "Mua sắm (Cảnh báo đỏ: 105%)"));
        response.put("savings_goals_seeded", 3);
        response.put("debts_loans_seeded", 3);
        response.put("recurring_transactions_seeded", 2);
        response.put("notifications_seeded", 3);

        return response;
    }

    private void ensureSystemCategories() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories WHERE user_id IS NULL", Integer.class);
        if (count == null || count == 0) {
            System.out.println(">>> SmartExpense: Initializing system default categories...");
            // Chi tiêu (EXPENSE)
            insertCategory(null, "Ăn uống", "EXPENSE", "food");
            insertCategory(null, "Di chuyển", "EXPENSE", "transport");
            insertCategory(null, "Nhà cửa", "EXPENSE", "home");
            insertCategory(null, "Hóa đơn", "EXPENSE", "bill");
            insertCategory(null, "Mua sắm", "EXPENSE", "shopping");
            insertCategory(null, "Giải trí", "EXPENSE", "entertainment");
            insertCategory(null, "Sức khỏe", "EXPENSE", "health");
            insertCategory(null, "Khác", "EXPENSE", "others");

            // Thu nhập (INCOME)
            insertCategory(null, "Lương", "INCOME", "salary");
            insertCategory(null, "Thưởng", "INCOME", "bonus");
            insertCategory(null, "Đầu tư", "INCOME", "investment");
            insertCategory(null, "Thu nhập khác", "INCOME", "other_income");
        }
    }

    private void insertCategory(Integer userId, String name, String type, String icon) {
        jdbcTemplate.update(
                "INSERT INTO categories (user_id, name, type, icon) VALUES (?, ?, ?, ?)",
                userId, name, type, icon
        );
    }

    private void cleanupUserData(Integer userId) {
        // Xóa sạch các bảng tham chiếu đến user_id
        jdbcTemplate.update("DELETE FROM transactions WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM budgets WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM wallets WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM savings_goals WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM debt_loans WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM recurring_transactions WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM notifications WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);
    }
}
