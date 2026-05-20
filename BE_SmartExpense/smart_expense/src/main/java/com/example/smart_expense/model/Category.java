package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Integer categoryId;
    private Integer userId; // NULL if default system category
    private String name;
    private String type; // "INCOME" or "EXPENSE"
    private String icon;
}
