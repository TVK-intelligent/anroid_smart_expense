package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class BudgetDetailResponse {
    @SerializedName("budgetId")
    private Integer budgetId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("limitAmount")
    private BigDecimal limitAmount;

    @SerializedName("spentAmount")
    private BigDecimal spentAmount;

    @SerializedName("spentPercent")
    private BigDecimal spentPercent;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("alertThreshold")
    private BigDecimal alertThreshold;

    @SerializedName("status")
    private String status; // "NORMAL" | "NEAR_LIMIT" | "OVER_LIMIT"

    // Getters and Setters
    public Integer getBudgetId() { return budgetId; }
    public void setBudgetId(Integer budgetId) { this.budgetId = budgetId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getSpentPercent() { return spentPercent; }
    public void setSpentPercent(BigDecimal spentPercent) { this.spentPercent = spentPercent; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public BigDecimal getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
