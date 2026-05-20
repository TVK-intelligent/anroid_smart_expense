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
public class Notification {
    private Integer notificationId;
    private Integer userId;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
