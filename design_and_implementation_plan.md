# SmartExpense: Tài liệu Thiết kế Hệ thống & Kế hoạch Triển khai

Tài liệu này trình bày chi tiết về cấu trúc cơ sở dữ liệu (MySQL), các thuật toán xử lý logic thông minh (Rule-based) không cần AI, thiết kế RESTful API và lộ trình xây dựng ứng dụng quản lý chi tiêu thông minh **SmartExpense**.

---

## 1. Tổng quan Kiến trúc Công nghệ

Dựa trên cấu trúc thư mục hiện tại trong workspace, hệ thống được phân chia thành hai phần chính:

*   **Backend (`BE_SmartExpense/smart_expense`)**:
    *   **Ngôn ngữ/Framework**: Java 21, Spring Boot 4.0.6, Spring MVC.
    *   **Data Access Layer**: Spring JDBC (`JdbcTemplate` / `NamedParameterJdbcTemplate`) kết hợp với MySQL.
    *   **Tiện ích phụ trợ**: Lombok giúp tối giản mã nguồn boilerplate.
*   **Frontend (`SmartExpense`)**:
    *   **Ngôn ngữ**: Kotlin.
    *   **UI Framework**: Android Jetpack Compose hiện đại, reactive và tối ưu hiệu năng.

---

## 2. Thiết kế Cơ sở Dữ liệu (MySQL Schema)

Dưới đây là sơ đồ DDL chi tiết cho hệ quản trị cơ sở dữ liệu MySQL, được thiết kế chuẩn hóa và tối ưu hóa index để thực thi nhanh các câu truy vấn phức tạp của thuật toán phân tích.

```sql
-- Tạo Database
CREATE DATABASE IF NOT EXISTS smart_expense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_expense;

-- 1. Bảng Users
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    currency VARCHAR(10) DEFAULT 'VND',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Bảng Wallets (Quản lý nguồn tiền)
CREATE TABLE wallets (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    type VARCHAR(50) NOT NULL, -- e.g., 'CASH', 'BANK', 'E-WALLET'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Bảng Categories (Phân loại thu/chi)
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL, -- NULL nghĩa là danh mục hệ thống mặc định; NOT NULL là danh mục tùy chỉnh của user
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    icon VARCHAR(100) DEFAULT 'default_icon',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Bảng Transactions (Giao dịch thu/chi)
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    wallet_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Bảng Budgets (Ngân sách chi tiêu chi tiết)
CREATE TABLE budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    alert_threshold DECIMAL(5, 2) DEFAULT 80.00, -- ví dụ: 80%
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Bảng Savings_Goals (Mục tiêu tiết kiệm)
CREATE TABLE savings_goals (
    goal_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    deadline DATE NOT NULL,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Bảng Notifications (Hệ thống thông báo thông minh)
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Bảng Recurring_Transactions (Giao dịch lặp lại định kỳ)
CREATE TABLE recurring_transactions (
    recurring_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    wallet_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    frequency ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') NOT NULL,
    next_due_date DATE NOT NULL,
    note TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. Bảng Debts_Loans (Quản lý Vay/Nợ vay trả)
CREATE TABLE debts_loans (
    debt_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    person_name VARCHAR(100) NOT NULL, -- Người vay hoặc chủ nợ
    type ENUM('DEBT', 'LOAN') NOT NULL, -- DEBT: Mình nợ người ta; LOAN: Người ta nợ mình
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) DEFAULT 0.00, -- Lãi suất nếu có (%)
    due_date DATE,
    status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID') DEFAULT 'UNPAID',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Bảng Tags & Transaction_Tags (Hệ thống phân tích sự kiện)
CREATE TABLE tags (
    tag_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_tag (user_id, name)
) ENGINE=InnoDB;

CREATE TABLE transaction_tags (
    transaction_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (transaction_id, tag_id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

---

## 3. Chi tiết Thuật toán "Thông minh" (Rule-based)

Để hệ thống hoạt động thông minh mà không cần huấn luyện các mô hình Machine Learning nặng nề, chúng ta áp dụng các công thức toán học và cấu trúc truy vấn hiệu năng cao trực tiếp trên cơ sở dữ liệu:

### 3.1. Phát hiện Bất thường (Anomaly Detection)

*   **Mục tiêu**: Cảnh báo tức thì khi người dùng nhập một giao dịch chi tiêu có giá trị lớn đột biến so với thói quen sinh hoạt thường ngày của họ cho danh mục đó.
*   **Logic**:
    1.  Tính giá trị trung bình chi tiêu hàng ngày hoặc theo từng giao dịch của `category_id` tương ứng trong vòng 90 ngày qua (loại trừ các ngày không có chi tiêu).
    2.  Nếu giao dịch mới có `amount > 2.0 * Average_Amount` (vượt 200%), hệ thống sẽ ghi nhận đây là bất thường và gửi một Notification gợi ý xem xét lại giao dịch này.
*   **Mẫu câu truy vấn SQL tính toán lịch sử**:
    ```sql
    SELECT AVG(amount) as avg_expense
    FROM transactions
    WHERE user_id = :userId 
      AND category_id = :categoryId
      AND transaction_date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY);
    ```

### 3.2. Cảnh báo Tốc độ Chi tiêu & Đề xuất Ngân sách (Burn Rate & Daily Budgeting)

*   **Mục tiêu**: Giúp người dùng điều tiết hành vi tiêu dùng khi thấy họ tiêu quá nhanh trong một chu kỳ ngân sách.
*   **Logic**:
    1.  Xác định ngân sách hiện tại (`amount`) của một danh mục chi tiêu trong khoảng thời gian hiện tại (`start_date` đến `end_date`).
    2.  Tính tổng số tiền đã chi cho danh mục đó trong chu kỳ: `Total_Spent`.
    3.  Tính số ngày đã trôi qua (`Days_Passed`) và tổng số ngày của chu kỳ (`Total_Days`).
    4.  Tính tỷ lệ tiến độ thời gian: $T_{ratio} = \frac{Days\_Passed}{Total\_Days}$
    5.  Tính tỷ lệ đã tiêu: $S_{ratio} = \frac{Total\_Spent}{Budget\_Amount}$
    6.  **Nếu $S_{ratio} > T_{ratio} \times 1.25$** (tiêu vượt mức phân bổ theo thời gian 25%):
        *   Tính số tiền còn lại: $Remaining = Budget\_Amount - Total\_Spent$
        *   Tính số ngày còn lại của kỳ: $Days\_Remaining = Total\_Days - Days\_Passed$
        *   Tính toán đề xuất chi tiêu an toàn mỗi ngày: $Safe\_Daily\_Limit = \frac{Remaining}{Days\_Remaining}$
        *   Đẩy thông báo cảnh báo kèm gợi ý thông minh: *"Bạn đã tiêu hết {S_ratio}% ngân sách mua sắm chỉ trong {Days_Passed} ngày. Hãy giới hạn chi tiêu tối đa {Safe_Daily_Limit}đ/ngày cho đến hết tháng để không bị vỡ kế hoạch."*

### 3.3. Tối ưu Kế hoạch Tiết kiệm (Savings Allocator)

*   **Mục tiêu**: Tự động gợi ý phân bổ tiền nhàn rỗi vào các mục tiêu tích lũy dựa trên mức độ ưu tiên thời gian (deadline gần nhất).
*   **Logic**:
    1.  Tính toán thu nhập bình quân và chi phí cố định (lấy từ dữ liệu `Recurring_Transactions` và trung bình của `Transactions` trong 3 tháng qua).
    2.  Ước lượng dòng tiền dư thừa hàng tháng: $Surplus = Avg\_Income - Avg\_Fixed\_Expenses$.
    3.  Tìm các `Savings_Goals` đang hoạt động (`status = 'IN_PROGRESS'`), sắp xếp theo mức độ cấp bách tăng dần (sắp đến `deadline` nhất).
    4.  Phân bổ số tiền dư thừa theo tỷ lệ ưu tiên (ví dụ: mục tiêu gần nhất nhận 50%, mục tiêu tiếp theo 30%, còn lại 20%).
    5.  Gợi ý người dùng trích xuất cụ thể từng khoản tiền vào từng tài khoản tích lũy để hoàn thành mục tiêu đúng hạn.

---

## 4. Thiết kế RESTful API

Chúng ta sẽ thiết kế một bộ API rõ ràng, bảo mật và dễ tích hợp với Retrofit hoặc Ktor trên Android.

### 4.1. Authentication & Users
*   `POST /api/auth/register` - Đăng ký tài khoản mới.
*   `POST /api/auth/login` - Đăng nhập nhận JWT Token.
*   `GET /api/users/profile` - Lấy thông tin cá nhân và thiết lập tiền tệ.

### 4.2. Wallets & Transactions
*   `GET /api/wallets` - Danh sách các ví/tài khoản ngân hàng của user.
*   `POST /api/wallets` - Tạo ví mới (Tiền mặt, Momo, Techcombank, etc.).
*   `GET /api/transactions` - Lọc và hiển thị danh sách các giao dịch (hỗ trợ phân trang và filter theo ngày, ví, danh mục).
*   `POST /api/transactions` - Ghi nhận giao dịch mới (hệ thống sẽ tự động kích hoạt tiến trình kiểm tra **Anomaly Detection** bất đồng bộ để gửi thông báo nếu phát hiện bất thường).

### 4.3. Budgets & Smart Analytics
*   `GET /api/budgets` - Lấy danh sách ngân sách và tiến độ chi tiêu thực tế.
*   `POST /api/budgets` - Thiết lập ngân sách mới cho một danh mục.
*   `GET /api/analytics/anomaly-check?categoryId={id}&amount={val}` - Endpoint phụ trợ để frontend check nhanh xem số tiền định nhập có bất thường không trước khi nhấn lưu.
*   `GET /api/analytics/suggestions` - Tổng hợp các gợi ý tài chính thông minh (tốc độ tiêu tiền, tối ưu hóa mục tiêu tiết kiệm) để hiển thị trên Dashboard chính.

---

## 5. Lộ trình Triển khai Chi tiết

### Pha 1: Xây dựng Cơ sở hạ tầng Backend
1.  Thiết lập file cấu hình MySQL trong `application.properties`.
2.  Viết các file Migration để tạo bảng tự động (hoặc file `schema.sql`).
3.  Xây dựng các Entity, Repository (sử dụng `JdbcTemplate` và `RowMapper` của Spring JDBC), Service và Controller cơ bản cho phần Authentication, Users, Wallets và Categories.

### Pha 2: Hiện thực hóa Tính năng Thông minh (Smart Analytics Engine)
1.  Viết các Service xử lý logic phát hiện giao dịch bất thường (Anomaly Detection Service).
2.  Xây dựng Scheduler hoặc trigger tự động để tính toán tốc độ chi tiêu (Burn rate) và lập báo cáo gợi ý định kỳ.
3.  Cài đặt module quản lý thông báo (`Notifications`) giúp lưu trữ các cảnh báo từ hệ thống.

### Pha 3: Thiết kế Giao diện Android hiện đại (Jetpack Compose)
1.  Xây dựng luồng Đăng ký / Đăng nhập.
2.  Thiết kế Dashboard chính hiển thị số dư tổng thể, biểu đồ phân bổ chi tiêu (Pie chart) trực quan, và danh sách các ví.
3.  Tạo màn hình **"Trung tâm Cảnh báo Thông minh"** để hiển thị các gợi ý tối ưu chi tiêu và phát hiện bất thường.
4.  Tích hợp Retrofit để kết nối toàn bộ API từ Backend lên Android.

---

> [!NOTE]
> Với việc sử dụng Spring JDBC thay vì JPA Hibernate, chúng ta có toàn quyền kiểm soát hiệu năng truy vấn SQL, rất phù hợp cho các câu truy vấn phức tạp của hệ thống phân tích chi tiêu thông minh này.

> [!TIP]
> Để tăng tính chuyên nghiệp, trên ứng dụng Jetpack Compose chúng ta có thể vẽ các biểu đồ bằng cách tự thiết kế Canvas trực tiếp hoặc sử dụng thư viện biểu đồ chuyên dụng như MPAndroidChart / Compose Charts để người dùng có cái nhìn trực quan nhất về dòng tiền của mình.
