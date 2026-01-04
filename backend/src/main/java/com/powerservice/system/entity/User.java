package com.powerservice.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String role = "USER"; // 保留默认值

    @Column(name = "face_data", columnDefinition = "TEXT")
    private String faceData;

    @Column(name = "face_registered")
    private Boolean faceRegistered = false; // 保留默认值

    @Column(name = "is_active")
    private Boolean isActive = true; // 保留默认值

    @Column(name = "last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastLoginTime;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;

    // 1. 无参构造函数（JPA 持久化必需）
    public User() {
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
    public Long getId() { // 解决 AuthService 中 getId() 找不到符号
        return id;
    }

    public String getUsername() { // 解决 AuthService 中 getUsername() 找不到符号
        return username;
    }

    public String getPassword() { // 解决 AuthService 中 getPassword() 找不到符号
        return password;
    }

    public String getRealName() { // 解决 AuthService/UserController 中 getRealName()/setRealName() 找不到符号
        return realName;
    }

    public String getPhone() { // 解决 UserController 中 setPhone() 找不到符号
        return phone;
    }

    public String getEmail() { // 解决 UserController 中 setEmail() 找不到符号
        return email;
    }

    public String getRole() { // 解决 AuthService 中 getRole() 找不到符号
        return role;
    }

    public String getFaceData() { // 解决 AuthService 中 getFaceData() 找不到符号
        return faceData;
    }

    public Boolean getFaceRegistered() { // 解决 AuthService 中 getFaceRegistered() 找不到符号
        return faceRegistered;
    }

    public Boolean getIsActive() { // 解决 AuthService 中 getIsActive() 找不到符号
        return isActive;
    }

    public LocalDateTime getLastLoginTime() { // 解决 AuthService 中 setLastLoginTime() 找不到符号
        return lastLoginTime;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRealName(String realName) { // 解决 UserController 中 setRealName() 找不到符号
        this.realName = realName;
    }

    public void setPhone(String phone) { // 解决 UserController 中 setPhone() 找不到符号
        this.phone = phone;
    }

    public void setEmail(String email) { // 解决 UserController 中 setEmail() 找不到符号
        this.email = email;
    }

    public void setRole(String role) { // 解决 UserController 中 setRole() 找不到符号
        this.role = role;
    }

    public void setFaceData(String faceData) { // 解决 AuthService 中 setFaceData() 找不到符号
        this.faceData = faceData;
    }

    public void setFaceRegistered(Boolean faceRegistered) { // 解决 AuthService 中 setFaceRegistered() 找不到符号
        this.faceRegistered = faceRegistered;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) { // 解决 AuthService 中 setLastLoginTime() 找不到符号
        this.lastLoginTime = lastLoginTime;
    }

    // createdAt 由 @PrePersist 自动初始化，保留 setter 兼容特殊场景
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // updatedAt 由 @PreUpdate 自动更新，保留 setter 兼容手动更新
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
