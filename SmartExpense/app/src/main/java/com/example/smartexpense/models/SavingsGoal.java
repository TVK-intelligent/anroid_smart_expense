package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class SavingsGoal {
    @SerializedName("goalId")
    private Integer goalId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("targetAmount")
    private BigDecimal targetAmount;

    @SerializedName("currentAmount")
    private BigDecimal currentAmount;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("status")
    private String status;

    // Getters and Setters
    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
