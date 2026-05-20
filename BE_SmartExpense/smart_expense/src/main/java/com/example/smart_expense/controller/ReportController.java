package com.example.smart_expense.controller;

import com.example.smart_expense.dto.MonthlyReportResponse;
import com.example.smart_expense.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Báo cáo tháng.
     * GET /api/reports/monthly?userId=1&month=5&year=2026
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(@RequestParam Integer userId,
                                              @RequestParam Integer month,
                                              @RequestParam Integer year) {
        try {
            MonthlyReportResponse response = reportService.getMonthlyReport(userId, month, year);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

