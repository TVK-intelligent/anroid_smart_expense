package com.example.smart_expense.controller;

import com.example.smart_expense.service.SmartAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final SmartAnalyticsService smartAnalyticsService;

    public AnalyticsController(SmartAnalyticsService smartAnalyticsService) {
        this.smartAnalyticsService = smartAnalyticsService;
    }

    /**
     * Kiểm tra chi tiêu bất thường trước hoặc sau khi thêm giao dịch.
     * GET /api/analytics/anomaly-check?userId=1&categoryId=2&amount=5000000
     */
    @GetMapping("/anomaly-check")
    public ResponseEntity<Map<String, Object>> checkAnomaly(
            @RequestParam Integer userId,
            @RequestParam Integer categoryId,
            @RequestParam BigDecimal amount) {
        
        boolean isAnomalous = smartAnalyticsService.checkForAnomaly(userId, categoryId, amount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("categoryId", categoryId);
        response.put("amount", amount);
        response.put("isAnomalous", isAnomalous);
        response.put("message", isAnomalous 
                ? "Giao dịch chi tiêu cao bất thường so với thói quen của bạn." 
                : "Chi tiêu ở mức bình thường.");
                
        return ResponseEntity.ok(response);
    }

    /**
     * Phân tích tốc độ tiêu dùng so với ngân sách hiện tại.
     * GET /api/analytics/burn-rate?userId=1&categoryId=2
     */
    @GetMapping("/burn-rate")
    public ResponseEntity<Map<String, Object>> checkBurnRate(
            @RequestParam Integer userId,
            @RequestParam Integer categoryId) {
            
        Optional<String> suggestion = smartAnalyticsService.checkBurnRateAndSuggest(userId, categoryId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("categoryId", categoryId);
        response.put("hasAlert", suggestion.isPresent());
        response.put("alertMessage", suggestion.orElse("Tốc độ chi tiêu đang nằm trong mức an toàn cho phép."));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Nhận gợi ý phân bổ tiền nhàn rỗi thông minh vào các mục tiêu tích lũy.
     * GET /api/analytics/savings-suggestions?userId=1
     */
    @GetMapping("/savings-suggestions")
    public ResponseEntity<List<Map<String, Object>>> getSavingsSuggestions(
            @RequestParam Integer userId) {
            
        List<Map<String, Object>> suggestions = smartAnalyticsService.getSavingsOptimizationSuggestion(userId);
        return ResponseEntity.ok(suggestions);
    }
}
