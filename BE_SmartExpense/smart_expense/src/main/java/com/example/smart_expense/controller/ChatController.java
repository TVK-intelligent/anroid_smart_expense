package com.example.smart_expense.controller;

import com.example.smart_expense.dto.ChatRequest;
import com.example.smart_expense.dto.ChatResponse;
import com.example.smart_expense.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat
     * Endpoint xử lý tin nhắn hội thoại với trợ lý ảo tài chính thông minh.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(ChatResponse.builder()
                    .reply("Lỗi: Yêu cầu thiếu tham số userId.")
                    .build());
        }

        ChatResponse response = chatService.generateReply(request.getUserId(), request.getMessage());
        return ResponseEntity.ok(response);
    }
}
