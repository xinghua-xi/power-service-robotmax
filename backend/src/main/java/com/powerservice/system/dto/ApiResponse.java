package com.powerservice.system.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // 无参构造函数（初始化时间戳）
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // 含 success 和 message 的构造函数
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // 全参构造函数
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== Getter 方法 ====================
    public boolean isSuccess() { // boolean 类型的 getter 用 is 开头（Java 规范）
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // ==================== Setter 方法 ====================
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    // timestamp 一般由构造函数初始化，也提供 setter 方便特殊场景修改
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ==================== 静态工具方法（补充完整，适配所有 Controller 场景） ====================
    // 场景1：成功 + 消息 + 数据（最常用，如查询用户、获取监控数据）
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 场景2：成功 + 消息（无数据，如删除、更新接口）
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    // 场景3：失败 + 消息（无数据，如参数错误、用户不存在）
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    // 场景4：失败 + 消息 + 数据（特殊场景，如返回错误详情）
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
