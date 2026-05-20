package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Transaction {
    @SerializedName("transactionId")
    private Integer transactionId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("walletId")
    private Integer walletId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("transactionDate")
    private String transactionDate;

    @SerializedName("note")
    private String note;

    // Getters and Setters
    public Integer getTransactionId() { return transactionId; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getWalletId() { return walletId; }
    public void setWalletId(Integer walletId) { this.walletId = walletId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
