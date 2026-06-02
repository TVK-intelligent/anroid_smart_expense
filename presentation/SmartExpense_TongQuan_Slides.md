# SmartExpense - Slide Tổng Quan Đồ Án

> Bộ nội dung này dùng cho buổi trình bày tổng quan/tiến độ, chưa phải bảo vệ cuối khóa.  
> Các tên "Thành viên 1-5" là placeholder để nhóm thay bằng tên thật.

---

## Slide 1 - Trang Bìa

**SmartExpense**  
Ứng dụng quản lý chi tiêu cá nhân

- Môn học: [Tên môn học]
- Nhóm: [Tên/Số nhóm]
- Lớp: [Tên lớp]
- Thành viên:
  - Thành viên 1
  - Thành viên 2
  - Thành viên 3
  - Thành viên 4
  - Thành viên 5

**Ghi chú thuyết trình:**  
Nhóm giới thiệu ngắn tên đề tài, mục tiêu chính là xây dựng một ứng dụng giúp người dùng quản lý tài chính cá nhân trên điện thoại.

---

## Slide 2 - Tổng Quan Đề Tài

**Bối cảnh**

- Người dùng thường khó theo dõi nhiều khoản thu/chi trong ngày.
- Việc quản lý ví, ngân sách và mục tiêu tiết kiệm dễ bị rời rạc.
- Nếu không có thống kê/cảnh báo, người dùng khó nhận ra thói quen chi tiêu bất thường.

**Mục tiêu**

- Xây dựng ứng dụng hỗ trợ ghi nhận, quản lý và phân tích chi tiêu cá nhân.
- Cung cấp cái nhìn tổng quan về số dư, ví, giao dịch, ngân sách và mục tiêu tích lũy.
- Hỗ trợ người dùng ra quyết định tài chính đơn giản hơn.

**Đối tượng sử dụng**

- Sinh viên
- Người đi làm
- Cá nhân muốn theo dõi tài chính hằng ngày

**Ghi chú thuyết trình:**  
Nhấn mạnh đề tài xuất phát từ nhu cầu thực tế: ai cũng có nhiều khoản thu chi nhỏ, nếu không ghi lại thì rất khó kiểm soát.

---

## Slide 3 - Ý Tưởng Và Phạm Vi Hiện Tại

**Ý tưởng chính**

- App Android cho phép người dùng quản lý chi tiêu cá nhân.
- Backend Spring Boot xử lý API và nghiệp vụ.
- MySQL lưu dữ liệu người dùng, ví, giao dịch, danh mục, ngân sách và mục tiêu.

**Phạm vi hiện tại**

- Đăng ký, đăng nhập và lưu phiên người dùng.
- Quản lý ví và giao dịch.
- Theo dõi ngân sách, mục tiêu tiết kiệm.
- Phân tích rule-based: cảnh báo bất thường, burn rate, gợi ý tiết kiệm.

**Ghi chú thuyết trình:**  
Giải thích rõ đây là giai đoạn tổng quan: nhóm đã có khung chức năng chính, một số phần phân tích đang ở mức luật xử lý đơn giản, chưa phải AI hoàn chỉnh.

---

## Slide 4 - Kiến Trúc Hệ Thống

**Android App**

- Java Android
- Activity/Fragment
- Material UI
- Retrofit gọi API backend

**Backend**

- Spring Boot REST API
- Controller - Service - Repository
- JDBC/JdbcTemplate truy vấn database

**Database**

- MySQL
- Các bảng chính: `users`, `wallets`, `categories`, `transactions`, `budgets`, `savings_goals`, `notifications`

**Luồng dữ liệu**

Người dùng thao tác trên app -> Retrofit -> REST API -> Repository/JDBC -> MySQL

**Ghi chú thuyết trình:**  
Slide này nên trình bày bằng một sơ đồ 3 khối: Mobile App, Backend API, Database. Mỗi khối chỉ cần 2-3 từ khóa để dễ nhìn.

---

## Slide 5 - Các Chức Năng Chính

**Tài khoản người dùng**

- Đăng ký tài khoản
- Đăng nhập
- Lưu thông tin phiên bằng SharedPreferences

**Ví tài chính**

- Thêm ví mới
- Sửa/xóa ví
- Xem danh sách ví và tổng số dư

**Giao dịch**

- Thêm giao dịch thu/chi
- Xem lịch sử giao dịch
- Sửa, xóa và lọc giao dịch

**Ghi chú thuyết trình:**  
Đây là nhóm chức năng cốt lõi nhất. Khi demo, nên ưu tiên đăng nhập, thêm ví, thêm giao dịch và xem lịch sử.

---

## Slide 6 - Ngân Sách, Mục Tiêu Và Thông Báo

**Danh mục**

- Quản lý danh mục thu/chi.
- Hỗ trợ danh mục mặc định và danh mục theo người dùng.

**Ngân sách**

- Thiết lập ngân sách theo danh mục.
- Theo dõi mức chi tiêu so với giới hạn đã đặt.

**Mục tiêu tiết kiệm**

- Tạo mục tiêu tích lũy.
- Nạp thêm tiền vào mục tiêu.
- Theo dõi trạng thái hoàn thành.

**Thông báo**

- Lưu và hiển thị cảnh báo tài chính.
- Đánh dấu đã đọc hoặc đọc tất cả.

**Ghi chú thuyết trình:**  
Nhóm chức năng này giúp app không chỉ ghi chép mà còn hỗ trợ quản lý tài chính theo kế hoạch.

---

## Slide 7 - Chức Năng Phân Tích Thông Minh

**Cảnh báo giao dịch bất thường**

- So sánh số tiền giao dịch với mức chi trung bình theo danh mục.
- Cảnh báo khi khoản chi vượt ngưỡng bất thường.

**Burn Rate**

- Kiểm tra tốc độ chi tiêu theo ngân sách.
- Đưa ra nhận xét nếu người dùng có nguy cơ tiêu quá nhanh.

**Gợi ý tiết kiệm**

- Dựa trên mục tiêu và dữ liệu chi tiêu.
- Gợi ý cách tối ưu để đạt mục tiêu tốt hơn.

**Lưu ý**

- Hiện tại logic phân tích là rule-based.
- Nhóm có thể phát triển thêm AI/thống kê nâng cao ở giai đoạn sau.

**Ghi chú thuyết trình:**  
Nên nói "thông minh" ở đây là bước đầu theo luật nghiệp vụ. Như vậy phần trình bày trung thực với source hiện tại và không bị quá hứa hẹn.

---

## Slide 8 - Tiến Độ Đến Hiện Tại

**Đã hoàn thành**

- Giao diện Android các màn hình chính:
  - Splash
  - Login/Register
  - Dashboard
  - Add Transaction
  - Transaction History
  - Analytics
  - Profile
- Backend REST API cho các nghiệp vụ chính.
- Kết nối app với backend bằng Retrofit.
- Dựng MySQL bằng container và kiểm tra API trả dữ liệu thành công.

**Đang tiếp tục hoàn thiện**

- Bổ sung dữ liệu mẫu đầy đủ hơn.
- Kiểm thử các luồng nghiệp vụ chính.
- Chỉnh UI/UX và thông báo lỗi.
- Hoàn thiện slide, báo cáo và kịch bản demo.

**Ghi chú thuyết trình:**  
Slide này nên nói theo tiến độ thật: nhóm đã có hệ thống chạy được, nhưng vẫn cần hoàn thiện dữ liệu, test và phần trình bày cuối.

---

## Slide 9 - Phân Công Công Việc

| Thành viên | Vai trò chính | Công việc phụ trách |
| --- | --- | --- |
| Thành viên 1 | Frontend Android | Dashboard, Profile, điều hướng app |
| Thành viên 2 | Frontend Android | Giao dịch, thêm/sửa/xóa/lọc giao dịch |
| Thành viên 3 | Backend Spring Boot | REST API, nghiệp vụ ví/giao dịch/người dùng |
| Thành viên 4 | Database | MySQL, thiết kế bảng, dữ liệu mẫu, kết nối DB |
| Thành viên 5 | Testing & tài liệu | Kiểm thử, slide, báo cáo, chuẩn bị demo |

**Ghi chú thuyết trình:**  
Nhóm thay tên thật vào bảng. Nếu có thành viên làm chéo nhiều phần, vẫn nên trình bày theo vai trò chính để slide gọn.

---

## Slide 10 - Khó Khăn Và Hướng Phát Triển

**Khó khăn**

- Kết nối thiết bị Android thật với backend chạy local.
- Cấu hình MySQL/container và đồng bộ database với backend.
- Đồng bộ dữ liệu giữa giao diện app, API và database.
- Xử lý lỗi mạng, lỗi nhập liệu và dữ liệu chưa đầy đủ.

**Hướng phát triển**

- Bổ sung dữ liệu mẫu cho demo.
- Hoàn thiện validation và xử lý lỗi.
- Cải thiện bảo mật đăng nhập.
- Thêm biểu đồ thống kê trực quan.
- Test kỹ các luồng chính trước kiểm tra cuối khóa.

**Ghi chú thuyết trình:**  
Nên nói khó khăn theo hướng kỹ thuật đã gặp thật, sau đó chuyển sang kế hoạch cải thiện để thể hiện nhóm đang kiểm soát tiến độ.

---

## Slide 11 - Kịch Bản Demo Ngắn

1. Mở app SmartExpense.
2. Đăng ký hoặc đăng nhập tài khoản.
3. Vào Dashboard xem tổng quan số dư.
4. Thêm một ví mới.
5. Thêm một giao dịch thu/chi.
6. Xem lịch sử giao dịch, thử lọc/sửa/xóa.
7. Mở Analytics để giới thiệu burn rate, ngân sách và mục tiêu tiết kiệm.
8. Kết thúc bằng tiến độ hiện tại và kế hoạch hoàn thiện.

**Ghi chú thuyết trình:**  
Demo nên ngắn, tránh đi quá sâu từng form. Mục tiêu là chứng minh app, backend và database đã kết nối được và các chức năng chính đã có khung hoạt động.

---

## Slide 12 - Kết Luận

**Tổng kết**

- SmartExpense là ứng dụng quản lý tài chính cá nhân trên Android.
- Hệ thống đã có đủ 3 thành phần chính: mobile app, backend API và database.
- Các chức năng cốt lõi đã được xây dựng ở mức nền tảng.
- Giai đoạn tiếp theo tập trung hoàn thiện dữ liệu, kiểm thử và nâng chất lượng demo.

**Thông điệp cuối**

SmartExpense hướng đến việc giúp người dùng theo dõi chi tiêu rõ ràng hơn, quản lý ngân sách tốt hơn và hình thành thói quen tài chính cá nhân lành mạnh.

**Ghi chú thuyết trình:**  
Kết thúc bằng việc nhấn mạnh đây là bản tổng quan tiến độ, nhóm đã có nền tảng để tiếp tục hoàn thiện cho kiểm tra cuối khóa.

---

# Checklist Chuẩn Bị Trước Khi Trình Bày

- Thay tên thật của 5 thành viên.
- Điền môn học, lớp, tên nhóm.
- Chụp 3-5 ảnh màn hình app để đưa vào slide:
  - Login/Register
  - Dashboard
  - Add Transaction
  - Transaction History
  - Analytics
- Chuẩn bị sẵn backend và database trước khi demo.
- Kiểm tra điện thoại đã `adb reverse tcp:8080 tcp:8080` nếu demo bằng thiết bị thật.
- Tập demo theo đúng kịch bản ngắn để tránh mất thời gian.
