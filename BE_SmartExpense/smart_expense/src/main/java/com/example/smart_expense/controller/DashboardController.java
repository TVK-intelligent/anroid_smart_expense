package com.example.smart_expense.controller;

import com.example.smart_expense.dto.DashboardResponse;
import com.example.smart_expense.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard tổng quan.
     * GET /api/dashboard?userId=1
     */
    @GetMapping
    public ResponseEntity<?> getDashboard(@RequestParam Integer userId) {
        try {
            DashboardResponse response = dashboardService.getDashboard(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

