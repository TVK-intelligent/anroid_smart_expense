package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;

public class BurnRateResponse {
    @SerializedName("userId")
    private Integer userId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("hasAlert")
    private boolean hasAlert;

    @SerializedName("alertMessage")
    private String alertMessage;

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public boolean isHasAlert() { return hasAlert; }
    public void setHasAlert(boolean hasAlert) { this.hasAlert = hasAlert; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
}
