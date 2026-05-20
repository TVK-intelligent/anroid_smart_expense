package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class SavingsSuggestion {
    @SerializedName("goalId")
    private Integer goalId;

    @SerializedName("deadline")
    private String deadline;

    @SerializedName("targetAmount")
    private BigDecimal targetAmount;

    @SerializedName("currentAmount")
    private BigDecimal currentAmount;

    @SerializedName("allocatedAmount")
    private BigDecimal allocatedAmount;

    @SerializedName("allocationPercent")
    private String allocationPercent;

    // Getters and Setters
    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }

    public String getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(String allocationPercent) { this.allocationPercent = allocationPercent; }
}
