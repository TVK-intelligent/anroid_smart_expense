package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String name;
    private String email;
    private String passwordHash;
    private String currency;
    private LocalDateTime createdAt;
}
