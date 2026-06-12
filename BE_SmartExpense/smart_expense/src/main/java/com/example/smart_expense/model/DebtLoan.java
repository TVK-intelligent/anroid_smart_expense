package com.example.smart_expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "debt_loans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebtLoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer debtId;
    private Integer userId;
    private String personName;
    private String type; // "DEBT" (chủ nợ) or "LOAN" (con nợ)
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LocalDate dueDate;
    private String status; // "UNPAID", "PARTIALLY_PAID", "PAID"
    private String note;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
