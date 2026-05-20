package com.example.smart_expense.controller;

import com.example.smart_expense.model.Notification;
import com.example.smart_expense.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Lấy toàn bộ thông báo (bao gồm cả thông báo thông minh tự động sinh) của người dùng.
     * GET /api/notifications?userId=1
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Đánh dấu một thông báo là đã đọc.
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Integer id) {
        notificationRepository.markAsRead(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã đánh dấu thông báo là đã đọc");
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh dấu toàn bộ thông báo của người dùng là đã đọc.
     * PUT /api/notifications/read-all?userId=1
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@RequestParam Integer userId) {
        notificationRepository.markAllAsReadForUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã đánh dấu toàn bộ thông báo là đã đọc");
        return ResponseEntity.ok(response);
    }
}
