package com.example.smartexpense.models.dashboard;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class TopExpenseCategory {
    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("totalSpent")
    private BigDecimal totalSpent;

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() {
        return com.example.smartexpense.api.CategoryCache.localizeCategoryName(categoryName);
    }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }

    @Override
    public String toString() {
        return categoryName != null ? categoryName : "Category";
    }
}

