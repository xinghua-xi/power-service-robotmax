package com.powerservice.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_base")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KnowledgeBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "hit_count")
    private Integer hitCount = 0; // 保留默认值

    @Column(name = "is_active")
    private Boolean isActive = true; // 保留默认值

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 1. 无参构造函数（JPA 持久化必需）
    public KnowledgeBase() {
        // 属性声明时已赋默认值，无需额外处理
    }

    // 2. 保留 JPA 自动初始化/更新时间的逻辑
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Getter 方法（按属性逐一实现） ====================
    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() { // 解决 ChatService 中 getAnswer() 找不到符号
        return answer;
    }

    public ServiceType getServiceType() { // 解决 ChatService 中 getServiceType() 找不到符号
        return serviceType;
    }

    public String getKeywords() {
        return keywords;
    }

    public Integer getHitCount() { // 解决 ChatService 中 getHitCount() 找不到符号
        return hitCount;
    }

    public Boolean getIsActive() { // Boolean 包装类型用 get 开头（符合 Java Bean 规范）
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ==================== Setter 方法（按属性逐一实现） ====================
    public void setId(Long id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // createdAt 由 @PrePersist 自动初始化，保留 setter 兼容特殊场景（如测试）
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // updatedAt 由 @PreUpdate 自动更新，保留 setter 兼容手动更新
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}