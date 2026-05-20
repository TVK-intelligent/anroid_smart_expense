package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class AnomalyResponse {
    @SerializedName("userId")
    private Integer userId;

    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("isAnomalous")
    private boolean isAnomalous;

    @SerializedName("message")
    private String message;

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public boolean isAnomalous() { return isAnomalous; }
    public void setAnomalous(boolean anomalous) { isAnomalous = anomalous; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
