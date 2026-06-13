package com.example.smartexpense.models.dashboard;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class BudgetWarning {
    @SerializedName("budgetId")
    private Integer budgetId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("budgetAmount")
    private BigDecimal budgetAmount;

    @SerializedName("spentAmount")
    private BigDecimal spentAmount;

    @SerializedName("spentPercent")
    private BigDecimal spentPercent;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("status")
    private String status; // NEAR_LIMIT / OVER_LIMIT

    public Integer getBudgetId() { return budgetId; }
    public void setBudgetId(Integer budgetId) { this.budgetId = budgetId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() {
        return com.example.smartexpense.api.CategoryCache.localizeCategoryName(categoryName);
    }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getSpentPercent() { return spentPercent; }
    public void setSpentPercent(BigDecimal spentPercent) { this.spentPercent = spentPercent; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

