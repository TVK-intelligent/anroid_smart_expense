package com.example.smartexpense.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("categoryId")
    private Integer categoryId;

    @SerializedName("userId")
    private Integer userId;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type; // "INCOME" or "EXPENSE"

    @SerializedName("icon")
    private String icon;

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() {
        return com.example.smartexpense.api.CategoryCache.localizeCategoryName(name);
    }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() {
        return getName() != null ? getName() : "Category";
    }
}

