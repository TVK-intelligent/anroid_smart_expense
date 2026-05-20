package com.example.smart_expense.service;

import com.example.smart_expense.dto.MonthlyReportResponse;
import com.example.smart_expense.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({TransactionRepository.class, ReportService.class})
class ReportServiceJdbcTest {

    private final JdbcTemplate jdbcTemplate;
    private final ReportService reportService;

    ReportServiceJdbcTest(JdbcTemplate jdbcTemplate, ReportService reportService) {
        this.jdbcTemplate = jdbcTemplate;
        this.reportService = reportService;
    }

    @Test
    void monthlyReport_totalsMatchTransactions_andFilteredByUser() {
        jdbcTemplate.update("INSERT INTO categories (user_id, name, type, icon) VALUES (NULL, 'Salary', 'INCOME', 'i')");
        jdbcTemplate.update("INSERT INTO categories (user_id, name, type, icon) VALUES (NULL, 'Food', 'EXPENSE', 'i')");

        Integer salaryCategoryId = jdbcTemplate.queryForObject("SELECT category_id FROM categories WHERE name='Salary'", Integer.class);
        Integer foodCategoryId = jdbcTemplate.queryForObject("SELECT category_id FROM categories WHERE name='Food'", Integer.class);

        // user 1: May 2026
        jdbcTemplate.update("INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note) VALUES (1, 1, ?, 1000.00, ?, 's')",
                salaryCategoryId, LocalDate.of(2026, 5, 1));
        jdbcTemplate.update("INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note) VALUES (1, 1, ?, 200.00, ?, 'f1')",
                foodCategoryId, LocalDate.of(2026, 5, 1));
        jdbcTemplate.update("INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note) VALUES (1, 1, ?, 300.00, ?, 'f2')",
                foodCategoryId, LocalDate.of(2026, 5, 2));

        // user 2: should be ignored
        jdbcTemplate.update("INSERT INTO transactions (user_id, wallet_id, category_id, amount, transaction_date, note) VALUES (2, 1, ?, 9999.00, ?, 'noise')",
                foodCategoryId, LocalDate.of(2026, 5, 1));

        MonthlyReportResponse report = reportService.getMonthlyReport(1, 5, 2026);

        assertThat(report.getTotalIncome()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(report.getTotalExpense()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(report.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));

        assertThat(report.getIncomeExpenseByDay()).hasSize(2);
        assertThat(report.getIncomeExpenseByDay().get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(report.getIncomeExpenseByDay().get(0).getIncome()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(report.getIncomeExpenseByDay().get(0).getExpense()).isEqualByComparingTo(new BigDecimal("200.00"));

        assertThat(report.getExpenseByCategory()).hasSize(1);
        assertThat(report.getExpenseByCategory().get(0).getCategoryName()).isEqualTo("Food");
        assertThat(report.getExpenseByCategory().get(0).getTotalSpent()).isEqualByComparingTo(new BigDecimal("500.00"));
    }
}

