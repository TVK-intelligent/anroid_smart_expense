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

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
