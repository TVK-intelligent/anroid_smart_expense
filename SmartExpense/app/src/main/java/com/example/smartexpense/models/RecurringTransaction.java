package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class RecurringTransaction {
    @SerializedName("recurringId")
    private Integer recurringId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("walletId")
    private Integer walletId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("frequency")
    private String frequency; // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"

    @SerializedName("nextDueDate")
    private String nextDueDate;

    @SerializedName("note")
    private String note;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public Integer getRecurringId() { return recurringId; }
    public void setRecurringId(Integer recurringId) { this.recurringId = recurringId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getWalletId() { return walletId; }
    public void setWalletId(Integer walletId) { this.walletId = walletId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(String nextDueDate) { this.nextDueDate = nextDueDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
