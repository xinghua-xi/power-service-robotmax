package com.powerservice.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_records")
public class ChatRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "bot_response", nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;

    @Column(name = "response_time")
    private Integer responseTime;

    @Column(name = "user_satisfaction")
    private Integer userSatisfaction;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 1. 无参构造函数（JPA 持久化必需）
    public ChatRecord() {
    }

    // 2. @PrePersist 注解保留（自动初始化创建时间）
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==================== Getter 方法（按属性逐一实现） ====================
    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getBotResponse() {
        return botResponse;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public Integer getUserSatisfaction() {
        return userSatisfaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ==================== Setter 方法（按属性逐一实现） ====================
    public void setId(Long id) {
        this.id = id;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public void setUserSatisfaction(Integer userSatisfaction) {
        this.userSatisfaction = userSatisfaction;
    }

    // 注意：createdAt 由 @PrePersist 自动初始化，setter 仍保留（兼容特殊场景手动赋值）
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
