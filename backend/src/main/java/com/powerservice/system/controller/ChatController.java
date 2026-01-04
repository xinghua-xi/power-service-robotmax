package com.powerservice.system.controller;

import com.powerservice.system.dto.ApiResponse;
import com.powerservice.system.dto.ChatRequest;
import com.powerservice.system.dto.ChatResponse;
import com.powerservice.system.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.processMessage(request);
            return ResponseEntity.ok(ApiResponse.success("消息处理成功", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<Object>> getChatHistory(@PathVariable String sessionId) {
        try {
            var history = chatService.getChatHistory(sessionId);
            return ResponseEntity.ok(ApiResponse.success("获取聊天记录成功", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("聊天服务运行正常", null));
    }
}
