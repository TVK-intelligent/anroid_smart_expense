Dựa trên kế hoạch SmartExpense trong file của bạn, nên chia prompt cho Codex theo từng task nhỏ, dễ rollback, đi theo thứ tự: login → ví → giao dịch → dashboard → báo cáo → ngân sách.

1. Prompt chuẩn nền tảng backend
Đọc toàn bộ project BE_SmartExpense/smart_expense.

Nhiệm vụ:
- Kiểm tra lại schema/entity/repository hiện có cho các module: user, wallet, category, transaction, budget.
- Không làm thêm tính năng mới.
- Chuẩn hóa response lỗi API theo format:
  {
    "message": "...",
    "code": "...",
    "details": [...]
  }
- Thêm validate cơ bản cho request.
- Đảm bảo không trả password/passwordHash về client.

Chỉ sửa phần backend cần thiết. Sau khi sửa, liệt kê file đã thay đổi.
2. Prompt Auth: đăng ký / đăng nhập / đăng xuất
Hoàn thiện module Authentication cho SmartExpense.

Yêu cầu:
- API đăng ký bằng name, email, password.
- API đăng nhập bằng email/password.
- Check lỗi: email trùng, sai mật khẩu, thiếu field.
- Response login trả về userId hoặc token/session phù hợp với code hiện tại.
- Không trả password hash về Android.
- Giữ code đơn giản, không thêm JWT nếu project hiện tại chưa có.

Sau khi sửa, ghi rõ API endpoint, request/response mẫu.
3. Prompt Android Auth + lưu phiên
Hoàn thiện luồng login/register trên Android app SmartExpense.

Yêu cầu:
- Login thành công thì lưu userId/token vào SharedPreferences.
- Mở lại app nếu đã login thì vào Dashboard.
- Logout ở Profile thì xóa session và quay về Login.
- Không hard-code userId trong Fragment/Activity.
- API lỗi thì hiện Toast hoặc message rõ ràng, không crash app.

Chỉ sửa Android, không sửa backend.
4. Prompt Wallet backend
Hoàn thiện module Wallet backend.

Yêu cầu:
- API tạo ví: tên ví, loại ví, số dư ban đầu.
- API lấy danh sách ví theo userId.
- API sửa tên ví, loại ví, số dư ban đầu nếu phù hợp.
- API xóa ví: chỉ cho xóa nếu ví chưa có giao dịch, nếu có thì trả lỗi rõ ràng.
- API tính tổng số dư tất cả ví của user.
- Mọi query phải filter theo user_id.

Không làm UI. Sau khi sửa, ghi endpoint và logic chính.
5. Prompt Wallet Android
Hoàn thiện màn hình ví tiền trên Android.

Yêu cầu:
- Hiển thị danh sách ví của user đang đăng nhập.
- Hiển thị tổng số dư tất cả ví.
- Thêm ví mới gồm: tên ví, loại ví, số dư ban đầu.
- Sửa ví cơ bản.
- Xóa ví và hiển thị lỗi nếu backend không cho xóa.
- Có loading, empty state, error state.
- Format tiền theo VND.

Chỉ sửa Android.
6. Prompt Transaction backend
Hoàn thiện module Transaction backend.

Yêu cầu:
- API thêm giao dịch gồm: type INCOME/EXPENSE, amount, walletId, categoryId, transactionDate, note.
- Không cho amount <= 0.
- Khi thêm giao dịch:
  - EXPENSE thì trừ số dư ví.
  - INCOME thì cộng số dư ví.
- API sửa giao dịch và cập nhật lại số dư ví chính xác.
- API xóa giao dịch và rollback lại số dư ví chính xác.
- API lấy danh sách giao dịch gần nhất.
- API lọc theo ngày, ví, danh mục, loại giao dịch.
- Dùng transaction DB khi thêm/sửa/xóa giao dịch và update số dư ví.

Ưu tiên logic đúng, chưa cần tối ưu phức tạp.
7. Prompt Transaction Android
Hoàn thiện màn hình giao dịch Android.

Yêu cầu:
- Màn hình thêm giao dịch gọn: số tiền, loại thu/chi, ví, danh mục, ngày, ghi chú.
- Lấy ví và danh mục từ API, không hard-code userId.
- Thêm giao dịch thành công thì quay lại danh sách và refresh.
- Danh sách giao dịch hiển thị gần nhất trước.
- Có lọc cơ bản theo ngày, ví, danh mục, loại thu/chi.
- Sửa/xóa giao dịch và refresh lại dữ liệu.
- Không cho nhập số tiền <= 0.
- Có loading/error/empty state.

Chỉ sửa Android.
8. Prompt Dashboard backend
Tạo/hoàn thiện API Dashboard cho SmartExpense.

Dashboard cần trả về:
- totalBalance: tổng số dư hiện tại.
- monthlyIncome: tổng thu trong tháng hiện tại.
- monthlyExpense: tổng chi trong tháng hiện tại.
- monthlyNet: thu - chi.
- recentTransactions: 5 giao dịch gần nhất.
- topExpenseCategories: 3 danh mục chi nhiều nhất trong tháng.
- budgetWarnings: danh sách ngân sách gần/vượt mức nếu đã có.

Tất cả dữ liệu phải filter theo user_id.
Không làm UI.
9. Prompt Dashboard Android
Hoàn thiện màn hình Dashboard Android.

Yêu cầu hiển thị:
- Tổng số dư hiện tại.
- Tổng thu tháng này.
- Tổng chi tháng này.
- Chênh lệch thu - chi.
- 5 giao dịch gần nhất.
- 3 danh mục chi nhiều nhất.
- Cảnh báo ngân sách nếu có.

Yêu cầu UX:
- Có loading state.
- Nếu chưa có ví/giao dịch thì hiện hướng dẫn: tạo ví hoặc thêm giao dịch.
- Format tiền VND.
- UI gọn, không thêm animation/phần trang trí thừa.
10. Prompt Report / Analytics backend
Hoàn thiện API báo cáo tháng cho SmartExpense.

Yêu cầu:
- Input: userId, month, year.
- Trả về:
  - totalIncome
  - totalExpense
  - balance
  - incomeExpenseByDay hoặc byWeek
  - expenseByCategory cho biểu đồ donut
  - topExpenseCategories
- Số liệu phải khớp với bảng transaction.
- Tất cả query phải filter theo user_id.

Không làm Android UI.
11. Prompt Analytics Android
Hoàn thiện màn hình Analytics/Báo cáo trên Android.

Yêu cầu:
- Chọn tháng/năm để xem báo cáo.
- Hiển thị tổng thu, tổng chi, còn lại.
- Biểu đồ cột thu/chi theo ngày hoặc tuần bằng MPAndroidChart.
- Biểu đồ donut/pie tỷ lệ chi theo danh mục.
- Danh sách danh mục chi nhiều nhất.
- Có loading/error/empty state.
- Số liệu hiển thị phải lấy từ API, không tự tính sai lệch ở client.
12. Prompt Budget backend
Hoàn thiện module Budget backend.

Yêu cầu:
- Tạo ngân sách theo user, category, month, year, limitAmount.
- Lấy danh sách ngân sách trong tháng.
- Tính spentAmount, remainingAmount, percentUsed.
- Rule cảnh báo:
  - < 80%: normal
  - >= 80% và < 100%: warning
  - >= 100%: exceeded
- Khi thêm/sửa/xóa giao dịch, ngân sách liên quan phải cập nhật đúng.
- Không tạo notification trùng nhiều lần cho cùng ngưỡng 80% hoặc 100% trong cùng tháng.

Chỉ sửa backend.
13. Prompt Budget Android
Hoàn thiện màn hình ngân sách trên Android.

Yêu cầu:
- Tạo ngân sách theo danh mục và tháng.
- Hiển thị: đã chi / giới hạn / còn lại / phần trăm.
- Màu trạng thái:
  - bình thường nếu < 80%
  - cảnh báo nếu >= 80%
  - vượt mức nếu >= 100%
- Dashboard có thể hiển thị ngân sách gần/vượt mức.
- Có loading/error/empty state.
- Format tiền VND.
14. Prompt polish cuối cùng
Rà soát toàn bộ app SmartExpense sau khi hoàn thiện MVP.

Yêu cầu:
- Không hard-code userId.
- Tất cả màn hình gọi API phải có loading/error/empty state.
- Format tiền VND thống nhất.
- Không crash khi API trả null/list rỗng/lỗi mạng.
- Kiểm tra lại luồng:
  1. Đăng ký
  2. Đăng nhập
  3. Tạo ví
  4. Thêm giao dịch thu/chi
  5. Xem dashboard
  6. Xem báo cáo
  7. Tạo ngân sách
  8. Cảnh báo 80%/100%
- Chỉ sửa bug và polish, không thêm tính năng mới.