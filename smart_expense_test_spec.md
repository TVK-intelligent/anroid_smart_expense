# HƯỚNG DẪN KIỂM THỬ HỆ THỐNG SMARTEXPENSE VỚI DỮ LIỆU MẪU (MOCK DATA)

Tài liệu này đặc tả chi tiết thông tin tài khoản thử nghiệm cùng các kịch bản kiểm thử (test scenarios) cụ thể cho từng tính năng của ứng dụng quản lý tài chính cá nhân **SmartExpense**.

---

## I. THÔNG TIN TÀI KHOẢN MẪU (TEST ACCOUNT)

Hệ thống đã tự động gieo (seed) tài khoản kiểm thử này ngay khi khởi động. Bạn có thể sử dụng thông tin này để đăng nhập trực tiếp trên ứng dụng Android hoặc qua API:

*   **Email:** `test@smartexpense.com`
*   **Mật khẩu (Password):** `password123`
*   **Tên hiển thị:** `Nguyễn Văn A`
*   **Đơn vị tiền tệ mặc định:** `VND`

---

## II. DANH SÁCH DỮ LIỆU MẪU ĐÃ CẤU HÌNH

Tài khoản đã được gieo đầy đủ dữ liệu thực tế kéo dài trong 30 ngày gần đây nhằm hiển thị các biểu đồ, báo cáo, ngân sách và trạng thái tài chính sống động nhất:

### 1. Danh sách Ví tiền (Wallets)
*   **Ví Tiền mặt (Cash):** `5.000.000đ` (Thường dùng cho chi tiêu sinh hoạt hằng ngày).
*   **Tài khoản Vietcombank (Bank):** `25.000.000đ` (Nhận lương và các giao dịch lớn).
*   **Ví điện tử MoMo (E-wallet):** `2.500.000đ` (Thanh toán hóa đơn trực tuyến, mua sắm online).
*   💰 **Tổng tài sản ban đầu:** `32.500.000đ`

### 2. Danh mục chi tiêu hệ thống (Categories)
*   **CHI TIÊU (EXPENSE):** Ăn uống, Di chuyển, Nhà cửa, Hóa đơn, Mua sắm, Giải trí, Sức khỏe, Khác.
*   **THU NHẬP (INCOME):** Lương, Thưởng, Đầu tư, Thu nhập khác.

### 3. Ngân sách & Hạn mức chi tiêu tháng này (Budgets)
*   **Ăn uống (Food Budget):** `3.000.000đ/tháng`. 
    *   *Trạng thái:* Đã chi `2.620.000đ` (~87.3%). 
    *   *Kỳ vọng:* Hệ thống hiển thị **Cảnh báo màu Vàng (Sắp vượt hạn mức >80%)**.
*   **Mua sắm (Shopping Budget):** `1.000.000đ/tháng`.
    *   *Trạng thái:* Đã chi `1.050.000đ` (~105.0%).
    *   *Kỳ vọng:* Hệ thống hiển thị **Cảnh báo màu Đỏ (Đã vượt quá hạn mức >100%)**.

### 4. Lịch sử 12 giao dịch mẫu gần đây (Transactions)
*   *(Chi)* `-5.000.000đ` | Ví Vietcombank | Danh mục: **Nhà cửa** | Tiền thuê nhà tháng này.
*   *(Thu)* `+30.000.000đ` | Ví Vietcombank | Danh mục: **Lương** | Tiền lương tháng này.
*   *(Chi)* `-1.150.000đ` | Ví MoMo | Danh mục: **Hóa đơn** | Tiền điện nước sinh hoạt.
*   *(Chi)* `-250.000đ` | Ví MoMo | Danh mục: **Hóa đơn** | Tiền cước Internet Viettel.
*   *(Chi)* `-650.000đ` | Ví MoMo | Danh mục: **Mua sắm** | Mua quần áo trên Shopee.
*   *(Chi)* `-400.000đ` | Ví MoMo | Danh mục: **Mua sắm** | Mua sách kỹ năng Tiki.
*   *(Chi)* `-350.000đ` | Ví MoMo | Danh mục: **Sức khỏe** | Mua thuốc tây và vitamin.
*   *(Chi)* `-45.000đ` | Ví Tiền mặt | Danh mục: **Di chuyển** | Đặt GrabBike đi làm.
*   *(Chi)* `-60.000đ` | Ví MoMo | Danh mục: **Di chuyển** | Gọi GrabCar đi gặp bạn.
*   *(Chi)* `-850.000đ` | Ví Tiền mặt | Danh mục: **Ăn uống** | Ăn lẩu gia đình cuối tuần.
*   *(Chi)* `-120.000đ` | Ví Tiền mặt | Danh mục: **Ăn uống** | Cà phê gặp mặt đồng nghiệp.
*   *(Chi)* `-1.200.000đ` | Ví Tiền mặt | Danh mục: **Ăn uống** | Tiền ăn trưa văn phòng 2 tuần.
*   *(Chi)* `-450.000đ` | Ví Vietcombank | Danh mục: **Ăn uống** | Mua thực phẩm tại WinMart.
*   *(Thu)* `+2.000.000đ` | Ví Vietcombank | Danh mục: **Thưởng** | Thưởng dự án đột xuất.

### 5. Lập quỹ tích lũy (Savings Goals)
*   **Mua Laptop Mới:** Mục tiêu `20.000.000đ` | Đã có: `8.000.000đ` | Trạng thái: `IN_PROGRESS` (Đang tích lũy).
*   **Quỹ Khẩn Cấp:** Mục tiêu `10.000.000đ` | Đã có: `10.000.000đ` | Trạng thái: `COMPLETED` (Đã hoàn thành).
*   **Du lịch Đà Lạt:** Mục tiêu `5.000.000đ` | Đã có: `1.500.000đ` | Trạng thái: `IN_PROGRESS` (Đang tích lũy).

### 6. Ghi chép Vay & Nợ (Debts & Loans)
*   **Anh Tuấn (Cho vay - LOAN):** `3.000.000đ` | Trạng thái: `UNPAID` (Chưa thu hồi).
*   **Chị Thảo (Nợ tiền - DEBT):** `1.500.000đ` | Trạng thái: `UNPAID` (Nợ quá hạn thanh toán 5 ngày).
*   **Bạn Nam (Cho vay - LOAN):** `500.000đ` | Trạng thái: `PAID` (Đã thanh toán xong).

### 7. Hóa đơn/Giao dịch định kỳ tự động (Recurring Transactions)
*   **Tiền thuê nhà:** `5.000.000đ` thanh toán từ Vietcombank vào ngày 1 mỗi tháng.
*   **Tiền mạng Internet:** `250.000đ` thanh toán từ MoMo vào ngày 10 mỗi tháng.

---

## III. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (TEST SCENARIOS)

### 1. Kịch bản 1: Đăng ký, Đăng nhập & Duy trì phiên (P0)
*   **Mục tiêu:** Xác nhận người dùng đăng nhập thành công và giữ được trạng thái phiên khi tắt/mở ứng dụng.
*   **Các bước thực hiện:**
    1.  Mở màn hình Đăng nhập (Login).
    2.  Nhập Email: `test@smartexpense.com` và Mật khẩu: `password123`. Nhấn nút **Đăng nhập**.
    3.  *Kỳ vọng:* Đăng nhập thành công, chuyển hướng thẳng vào màn hình Tổng quan (Dashboard).
    4.  Nhập sai mật khẩu hoặc bo trống trường thông tin.
    5.  *Kỳ vọng:* Ứng dụng báo lỗi rõ ràng "Email hoặc mật khẩu không chính xác!", không bị crash.
    6.  Đóng ứng dụng hoàn toàn (Kill app từ màn hình đa nhiệm) sau đó mở lại.
    7.  *Kỳ vọng:* Ứng dụng tự động bỏ qua màn hình Login và hiển thị thẳng Dashboard (lưu session).
    8.  Vào phần Profile (Cá nhân), chọn **Đăng xuất**.
    9.  *Kỳ vọng:* Xóa session thành công, quay về màn hình Login và không thể quay lại Dashboard bằng nút Back.

### 2. Kịch bản 2: Dashboard Tổng quan & Số dư Ví tiền (P0)
*   **Mục tiêu:** Xác nhận các thông tin tài chính trực quan hiển thị đúng số liệu mẫu đã nạp.
*   **Các bước thực hiện:**
    1.  Tại màn hình Dashboard, kiểm tra các con số thống kê:
        *   **Tổng số dư khả dụng:** `32.500.000đ`.
        *   **Tổng thu nhập trong tháng:** `32.000.000đ` (Lương 30M + Thưởng 2M).
        *   **Tổng chi tiêu trong tháng:** `11.175.000đ` (Tổng tất cả các khoản chi).
    2.  Kiểm tra danh sách 3-5 giao dịch gần đây nhất trên Dashboard.
        *   *Kỳ vọng:* Hiển thị đúng chi tiết giao dịch (ngày gần nhất xếp trên đầu, hiển thị đúng icon danh mục và số tiền).
    3.  Chuyển sang tab hoặc màn hình **Danh sách ví tiền (Wallets)**.
        *   *Kỳ vọng:* Hiển thị đủ 3 ví: Tiền mặt (`5M`), Vietcombank (`25M`), MoMo (`2.5M`).

### 3. Kịch bản 3: Thêm mới, Sửa và Xóa giao dịch (P0)
*   **Mục tiêu:** Đảm bảo việc thêm/sửa/xóa giao dịch hoạt động chuẩn xác và số dư ví được tự động cập nhật ngay lập tức (Real-time).
*   **Các bước thực hiện (Kiểm thử Thêm giao dịch):**
    1.  Nhấn nút **Thêm giao dịch (+)** trên ứng dụng.
    2.  Nhập số tiền: `150.000đ`.
    3.  Chọn loại ví: **Tiền mặt**.
    4.  Chọn danh mục: **Ăn uống** (hoặc danh mục chi tiêu bất kỳ).
    5.  Chọn ngày giao dịch: Hôm nay. Nhập ghi chú: "Ăn tối bún đậu mắm tôm". Nhấn **Lưu**.
    6.  *Kỳ vọng:*
        *   Giao dịch mới xuất hiện ở đầu danh sách lịch sử.
        *   Số dư **Ví Tiền mặt** giảm ngay lập tức từ `5.000.000đ` xuống `4.850.000đ`.
        *   Tổng số dư toàn khoản giảm từ `32.500.000đ` xuống `32.350.000đ`.
*   **Các bước thực hiện (Kiểm thử Sửa giao dịch):**
    1.  Chọn giao dịch vừa tạo ("Ăn tối bún đậu mắm tôm") trong danh sách lịch sử để vào chế độ Sửa.
    2.  Sửa số tiền từ `150.000đ` thành `100.000đ`. Chọn **Lưu**.
    3.  *Kỳ vọng:*
        *   Số dư **Ví Tiền mặt** tự động hoàn trả 50K chênh lệch, tăng lên thành `4.900.000đ`.
*   **Các bước thực hiện (Kiểm thử Xóa giao dịch):**
    1.  Chọn giao dịch vừa sửa ("Ăn tối bún đậu mắm tôm") và nhấn biểu tượng **Xóa (Delete)**.
    2.  *Kỳ vọng:*
        *   Giao dịch biến mất khỏi lịch sử.
        *   Số dư **Ví Tiền mặt** được khôi phục về giá trị ban đầu là `5.000.000đ`.

### 4. Kịch bản 4: Cảnh báo Ngân sách & Hạn mức chi tiêu (P0)
*   **Mục tiêu:** Xác minh hệ thống phát hiện chính xác các ngưỡng ngân sách chi tiêu và đưa ra cảnh báo kịp thời.
*   **Các bước thực hiện:**
    1.  Truy cập tab **Ngân sách (Budgets)** hoặc xem phần cảnh báo trên Dashboard.
    2.  *Kỳ vọng:*
        *   Ngân sách **Ăn uống** hiển thị thanh tiến độ màu vàng/cam kèm thông tin: `Đã chi: 2.620.000đ / Hạn mức: 3.000.000đ (Đạt 87%)`.
        *   Ngân sách **Mua sắm** hiển thị thanh tiến độ màu đỏ kèm thông tin cảnh báo: `Đã chi: 1.050.000đ / Hạn mức: 1.000.000đ (Vượt mức 105%)`.
    3.  Vào màn hình **Thông báo (Notifications)**.
        *   *Kỳ vọng:* Nhìn thấy thông báo đẩy: `"Cảnh báo ngân sách Mua sắm: Bạn đã chi tiêu 1,050,000 VND (vượt quá 100% ngân sách 1,000,000 VND)..."`.
    4.  *(Nâng cao)* Thử thêm một giao dịch chi tiêu trị giá `400.000đ` từ ví **Tiền mặt** thuộc danh mục **Ăn uống**.
        *   *Kỳ vọng:* Ngân sách Ăn uống vượt ngưỡng 100% (`3.020.000đ`). Ứng dụng lập tức chuyển trạng thái ngân sách này từ Vàng sang Đỏ (Vượt hạn mức) và sinh ra một thông báo đẩy cảnh báo vượt ngân sách Ăn uống.

### 5. Kịch bản 5: Báo cáo & Phân tích trực quan (P0)
*   **Mục tiêu:** Xác nhận các chỉ số biểu đồ phản ánh đúng thực tế thu chi.
*   **Các bước thực hiện:**
    1.  Mở màn hình **Báo cáo (Analytics/Reports)** và chọn chu kỳ **Tháng này**.
    2.  *Kỳ vọng:*
        *   Tổng Thu: `32.000.000đ`, Tổng Chi: `11.175.000đ`.
        *   Biểu đồ tròn tỷ lệ chi tiêu theo danh mục hiển thị:
            *   *Ăn uống:* `2.620.000đ`
            *   *Nhà cửa:* `5.000.000đ` (Danh mục chiếm tỷ trọng lớn nhất)
            *   *Hóa đơn:* `1.400.000đ`
            *   *Mua sắm:* `1.050.000đ`
            *   *Sức khỏe:* `350.000đ`
            *   *Di chuyển:* `105.000đ`
        *   Danh sách sắp xếp danh mục chi tiêu nhiều nhất hiển thị đúng thứ tự: `Nhà cửa > Ăn uống > Hóa đơn > Mua sắm > Sức khỏe > Di chuyển`.

### 6. Kịch bản 6: Quản lý Quỹ tích lũy / Tiết kiệm (P1)
*   **Mục tiêu:** Kiểm tra hoạt động theo dõi tiền gửi tiết kiệm và tự động cập nhật trạng thái mục tiêu.
*   **Các bước thực hiện:**
    1.  Mở màn hình **Tích lũy (Savings Goals)**.
    2.  *Kỳ vọng:*
        *   Mục tiêu **Quỹ Khẩn Cấp** hiển thị tích xanh hoặc nhãn `Đã hoàn thành (COMPLETED)` với tỷ lệ đạt `100% (10M/10M)`.
        *   Mục tiêu **Mua Laptop Mới** hiển thị trạng thái `Đang tiến hành` với tiến trình đạt `40% (8M/20M)`.
    3.  Nhấp vào mục tiêu **Mua Laptop Mới** và chọn chức năng **Gửi thêm tiền (Add Funds)**.
    4.  Nhập số tiền nạp quỹ: `2.000.000đ`. Nhấn xác nhận.
    5.  *Kỳ vọng:*
        *   Số tiền hiện tại của mục tiêu tăng lên thành `10.000.000đ`.
        *   Tiến trình tích lũy cập nhật lên `50%`.

### 7. Kịch bản 7: Quản lý Sổ nợ (Vay và Cho vay) (P1)
*   **Mục tiêu:** Kiểm thử quy trình giám sát công nợ và cảnh báo quá hạn.
*   **Các bước thực hiện:**
    1.  Mở màn hình **Sổ nợ (Debts & Loans)**.
    2.  *Kỳ vọng:*
        *   Hiển thị khoản cho **Anh Tuấn** vay `3.000.000đ` (Chưa đòi).
        *   Hiển thị khoản nợ **Chị Thảo** `1.500.000đ` có gắn nhãn màu đỏ hoặc cảnh báo **Quá hạn**.
    3.  Chọn khoản vay **Anh Tuấn**, nhấn nút **Đã thu hồi nợ (Mark as Paid)**.
    4.  *Kỳ vọng:* Khoản vay chuyển trạng thái thành `PAID` (Đã trả xong), số dư ví liên quan tự động cộng thêm `3.000.000đ`.

### 8. Kịch bản 8: Trợ lý tài chính ảo Chatbot AI (Tối ưu hóa tài chính)
*   **Mục tiêu:** Kiểm tra trí tuệ nhân tạo phân tích dòng tiền nhàn rỗi dựa trên dữ liệu thật của tài khoản kiểm thử.
*   **Các bước thực hiện:**
    1.  Truy cập màn hình **Trợ lý AI (Financial Chatbot)**.
    2.  Gửi tin nhắn: `"Ví của tôi còn bao nhiêu tiền?"`
    3.  *Kỳ vọng:* AI trả lời chi tiết tổng tài sản là `32.500.000 VND` và liệt kê rõ số tiền của từng ví (Tiền mặt 5M, Vietcombank 25M, MoMo 2.5M).
    4.  Gửi tin nhắn: `"Kiểm tra ngân sách tháng này"`
    5.  *Kỳ vọng:* AI phát hiện ra ngân sách **Mua sắm** đã bị vượt hạn mức (chi 1.05M so với hạn mức 1M) và ngân sách **Ăn uống** sắp vượt hạn mức (87.3%), đưa ra lời khuyên cắt giảm chi tiêu thông minh.
    6.  Gửi tin nhắn: `"Tư vấn tiết kiệm"`
    7.  *Kỳ vọng:* AI chạy thuật toán **Savings Allocator** phân bổ thông minh số tiền nhàn rỗi (Dòng tiền dư ròng) vào 2 mục tiêu tích lũy đang hoạt động (`Mua Laptop Mới` và `Du lịch Đà Lạt`) theo tỷ lệ tiến độ tối ưu.

---

## IV. CÁCH RESET DỮ LIỆU KIỂM THỬ NHANH CHÓNG (RESET DATA API)

Trong quá trình kiểm thử, nếu bạn vô tình xóa/sửa dữ liệu và muốn khôi phục lại trạng thái ban đầu để kiểm thử lại từ đầu, bạn không cần phải cài đặt lại ứng dụng. Chỉ cần thực hiện thao tác gọi API khôi phục:

### Cách thực hiện (sử dụng Postman / cURL):
*   **Method:** `POST`
*   **URL:** `http://localhost:8080/api/test/seed`
*   **Headers:** Không yêu cầu.
*   **Request Body:** Không có.

### Lệnh cURL chạy nhanh trên Terminal/Command Prompt:
```bash
curl -X POST http://localhost:8080/api/test/seed
```

### Phản hồi kỳ vọng (Response):
```json
{
  "success": true,
  "message": "Khởi tạo dữ liệu thử nghiệm (Mock Data) thành công!",
  "account": {
    "email": "test@smartexpense.com",
    "password": "password123",
    "name": "Nguyễn Văn A"
  },
  "wallets": [
    {"name": "Tiền mặt", "type": "CASH", "balance": 5000000},
    {"name": "Tài khoản Vietcombank", "type": "BANK", "balance": 25000000},
    {"name": "Ví MoMo", "type": "E-WALLET", "balance": 2500000}
  ],
  "transaction_count": 12,
  "budgets_seeded": ["Ăn uống (Cảnh báo vàng: 87.3%)", "Mua sắm (Cảnh báo đỏ: 105%)"],
  "savings_goals_seeded": 3,
  "debts_loans_seeded": 3,
  "recurring_transactions_seeded": 2,
  "notifications_seeded": 3
}
```

Mọi dữ liệu của tài khoản `test@smartexpense.com` sẽ lập tức được đặt lại sạch sẽ về đúng giá trị chuẩn mực ban đầu để bạn bắt đầu một chu trình kiểm thử mới!
