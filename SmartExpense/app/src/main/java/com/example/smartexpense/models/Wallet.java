package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class Wallet {
    @SerializedName("walletId")
    private Integer walletId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("name")
    private String name;

    @SerializedName("balance")
    private BigDecimal balance;

    @SerializedName("type")
    private String type;

    // Getters and Setters
    public Integer getWalletId() { return walletId; }
    public void setWalletId(Integer walletId) { this.walletId = walletId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() {
        if (name == null) return null;
        boolean isVi = "vi".equals(com.example.smartexpense.utils.LocaleHelper.getAppLanguage());
        if (isVi) {
            switch (name.toLowerCase().trim()) {
                case "main wallet": return "Ví chính";
                case "momo wallet": return "Ví MoMo";
                case "visa card": return "Thẻ VISA";
                case "savings wallet": return "Ví tiết kiệm";
                case "bank account": return "Tài khoản ngân hàng";
            }
            return name;
        } else {
            switch (name.toLowerCase().trim()) {
                case "ví chính": return "Main Wallet";
                case "ví momo": return "MoMo Wallet";
                case "thẻ visa": return "Visa Card";
                case "ví tiết kiệm": return "Savings Wallet";
                case "tài khoản ngân hàng": return "Bank Account";
            }
            String translated = name;
            if (translated.toLowerCase().startsWith("ví ")) {
                translated = translated.substring(3) + " Wallet";
            } else if (translated.toLowerCase().startsWith("thẻ ")) {
                translated = translated.substring(4) + " Card";
            }
            return translated;
        }
    }
    public void setName(String name) { this.name = name; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        String displayName = getName();
        if (displayName != null && !displayName.trim().isEmpty()) return displayName;
        boolean isVi = "vi".equals(com.example.smartexpense.utils.LocaleHelper.getAppLanguage());
        return walletId != null ? ((isVi ? "Ví " : "Wallet ") + walletId) : (isVi ? "Ví" : "Wallet");
    }
}
