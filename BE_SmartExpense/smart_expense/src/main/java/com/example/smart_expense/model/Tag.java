package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private Integer tagId;
    private Integer userId;
    private String name;
}
