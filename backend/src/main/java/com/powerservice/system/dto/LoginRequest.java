package com.powerservice.system.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe = false; // 保留默认值初始化

    // 1. 无参构造函数（Spring MVC 接收请求、参数校验必需）
    public LoginRequest() {
        // rememberMe 已通过属性声明初始化，无需额外处理
    }

    // 2. 仅含用户名密码的构造函数（方便手动创建对象）
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.rememberMe = false; // 保持默认值
    }

    // 3. 全参构造函数（支持自定义 rememberMe）
    public LoginRequest(String username, String password, Boolean rememberMe) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    // ==================== Getter 方法 ====================
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // Boolean 包装类型用 get 开头（符合 Java Bean 规范）
    public Boolean getRememberMe() {
        return rememberMe;
    }

    // ==================== Setter 方法 ====================
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}