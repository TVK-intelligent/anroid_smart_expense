package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("notificationId")
    private Integer notificationId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("isRead")
    private Boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public Integer getNotificationId() { return notificationId; }
    public void setNotificationId(Integer notificationId) { this.notificationId = notificationId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getTitle() {
        if (title == null) return null;
        boolean isVi = "vi".equals(com.example.smartexpense.utils.LocaleHelper.getAppLanguage());
        if (isVi) {
            String t = title.toLowerCase().trim();
            if (t.contains("budget alert:") || t.contains("budget limit exceeded warning")) {
                return "Cảnh báo vượt ngân sách";
            }
            if (t.contains("warning: unusual spending") || t.contains("unusual spending warning")) {
                return "Cảnh báo: Chi tiêu bất thường!";
            }
            if (t.contains("overdue debt alert")) {
                return "Cảnh báo nợ quá hạn";
            }
            if (t.contains("recurring bill reminder")) {
                return "Nhắc nhở hóa đơn định kỳ";
            }
            return title;
        } else {
            String t = title.toLowerCase().trim();
            if (t.contains("cảnh báo ngân sách")) {
                String cat = title.substring(18).trim();
                return "Budget Alert: " + com.example.smartexpense.api.CategoryCache.localizeCategoryName(cat);
            }
            if (t.contains("chi tiêu bất thường")) {
                return "Warning: Unusual Spending!";
            }
            if (t.contains("nợ quá hạn")) {
                return "Overdue Debt Alert";
            }
            if (t.contains("nhắc nhở hóa đơn")) {
                return "Recurring Bill Reminder";
            }
            return title;
        }
    }
    public void setTitle(String title) { this.title = title; }

    public String getContent() {
        if (content == null) return null;
        boolean isVi = "vi".equals(com.example.smartexpense.utils.LocaleHelper.getAppLanguage());
        if (isVi) {
            return content;
        } else {
            String c = content.toLowerCase().trim();
            String translated = content;
            if (c.contains("vượt quá 100% ngân sách") || c.contains("ngân sách")) {
                translated = translated
                    .replace("Bạn đã chi tiêu", "You spent")
                    .replace("(vượt quá 100% ngân sách", "(exceeding 100% of the budget")
                    .replace("cho danh mục", "for category")
                    .replace("Mua sắm", "Shopping")
                    .replace("trong tháng này!", "this month!");
            }
            if (c.contains("khoản nợ")) {
                translated = translated
                    .replace("Khoản nợ", "The debt to")
                    .replace("với số tiền", "with the amount of")
                    .replace("đã quá hạn thanh toán", "is overdue by")
                    .replace("ngày! Vui lòng tất toán.", "days! Please settle it.");
            }
            if (c.contains("giao dịch định kỳ") || c.contains("chuẩn bị thanh toán")) {
                translated = translated
                    .replace("Giao dịch định kỳ", "Recurring transaction")
                    .replace("đã được tự động lên lịch và chuẩn bị thanh toán.", "has been automatically scheduled and is ready for payment.");
            }
            if (c.contains("hệ thống phát hiện giao dịch chi tiêu mới")) {
                translated = translated
                    .replace("Hệ thống phát hiện giao dịch chi tiêu mới có giá trị", "The system detected a new spending transaction of")
                    .replace("cao bất thường so với mức trung bình lịch sử", "which is unusually high compared to historical average of")
                    .replace("của danh mục này. Bạn có muốn xem lại không?", "for this category. Do you want to review it?");
            }
            return translated;
        }
    }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
