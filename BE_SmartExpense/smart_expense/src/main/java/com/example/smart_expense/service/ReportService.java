package com.example.smart_expense.service;

import com.example.smart_expense.dto.ExpenseByCategory;
import com.example.smart_expense.dto.IncomeExpenseByDay;
import com.example.smart_expense.dto.MonthlyReportResponse;
import com.example.smart_expense.dto.TopExpenseCategory;
import com.example.smart_expense.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyReportResponse getMonthlyReport(Integer userId, Integer month, Integer year) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (month == null) throw new IllegalArgumentException("month is required");
        if (year == null) throw new IllegalArgumentException("year is required");
        if (month < 1 || month > 12) throw new IllegalArgumentException("month must be between 1 and 12");
        if (year < 1900 || year > 3000) throw new IllegalArgumentException("year is invalid");

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.getMonthlyTotalByType(userId, "INCOME", start, end);
        BigDecimal totalExpense = transactionRepository.getMonthlyTotalByType(userId, "EXPENSE", start, end);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<IncomeExpenseByDay> byDay = new ArrayList<>();
        List<Map<String, Object>> byDayRows = transactionRepository.getIncomeExpenseByDay(userId, start, end);
        for (Map<String, Object> row : byDayRows) {
            Object dateObj = row.get("transaction_date");
            LocalDate date;
            if (dateObj instanceof java.sql.Date sqlDate) {
                date = sqlDate.toLocalDate();
            } else if (dateObj instanceof LocalDate localDate) {
                date = localDate;
            } else {
                continue;
            }

            BigDecimal income = (BigDecimal) row.get("total_income");
            BigDecimal expense = (BigDecimal) row.get("total_expense");
            if (income == null) income = BigDecimal.ZERO;
            if (expense == null) expense = BigDecimal.ZERO;

            byDay.add(IncomeExpenseByDay.builder()
                    .date(date)
                    .income(income)
                    .expense(expense)
                    .build());
        }

        List<ExpenseByCategory> expenseByCategory = new ArrayList<>();
        List<Map<String, Object>> categoryRows = transactionRepository.getExpenseByCategory(userId, start, end);
        for (Map<String, Object> row : categoryRows) {
            expenseByCategory.add(ExpenseByCategory.builder()
                    .categoryId(((Number) row.get("category_id")).intValue())
                    .categoryName((String) row.get("name"))
                    .totalSpent((BigDecimal) row.get("total_spent"))
                    .build());
        }

        List<TopExpenseCategory> topExpenseCategories = new ArrayList<>();
        List<Map<String, Object>> topRows = transactionRepository.getTopExpenseCategories(userId, start, end, 5);
        for (Map<String, Object> row : topRows) {
            topExpenseCategories.add(TopExpenseCategory.builder()
                    .categoryId(((Number) row.get("category_id")).intValue())
                    .categoryName((String) row.get("name"))
                    .totalSpent((BigDecimal) row.get("total_spent"))
                    .build());
        }

        return MonthlyReportResponse.builder()
                .userId(userId)
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .incomeExpenseByDay(byDay)
                .expenseByCategory(expenseByCategory)
                .topExpenseCategories(topExpenseCategories)
                .build();
    }
}

