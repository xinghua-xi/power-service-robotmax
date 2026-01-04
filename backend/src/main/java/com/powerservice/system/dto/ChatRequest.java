package com.powerservice.system.dto;

import jakarta.validation.constraints.NotBlank;



public class ChatRequest {
    @NotBlank(message = "消息不能为空")
    private String message;

    private String sessionId;
    private Long userId;
    //getter and setter
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
