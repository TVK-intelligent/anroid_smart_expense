package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Budget {
    @SerializedName("budgetId")
    private Integer budgetId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("startDate")
    private String startDate; // Keep LocalDate as String for simple deserialization

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("alertThreshold")
    private BigDecimal alertThreshold;

    // Getters and Setters
    public Integer getBudgetId() { return budgetId; }
    public void setBudgetId(Integer budgetId) { this.budgetId = budgetId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public BigDecimal getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }
}
