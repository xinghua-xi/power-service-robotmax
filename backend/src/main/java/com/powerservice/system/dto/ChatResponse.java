package com.powerservice.system.dto;

public class ChatResponse {
    private String response;
    private String serviceType;
    private Boolean needMoreInfo;
    private String sessionId;

    // 无参构造函数（初始化 needMoreInfo 为 false）
    public ChatResponse() {
        this.needMoreInfo = false;
    }

    // 仅含 response 的构造函数
    public ChatResponse(String response) {
        this.response = response;
        this.needMoreInfo = false;
    }

    // 含 response + needMoreInfo 的构造函数
    public ChatResponse(String response, Boolean needMoreInfo) {
        this.response = response;
        this.needMoreInfo = needMoreInfo;
    }

    // 含 response + serviceType + needMoreInfo 的构造函数
    public ChatResponse(String response, String serviceType, Boolean needMoreInfo) {
        this.response = response;
        this.serviceType = serviceType;
        this.needMoreInfo = needMoreInfo;
    }

    // ==================== Getter 方法 ====================
    // String 类型常规 getter
    public String getResponse() {
        return response;
    }

    public String getServiceType() {
        return serviceType;
    }

    // Boolean 包装类型用 get 开头（区别于 boolean 基本类型的 is 开头）
    public Boolean getNeedMoreInfo() {
        return needMoreInfo;
    }

    public String getSessionId() {
        return sessionId;
    }

    // ==================== Setter 方法 ====================
    public void setResponse(String response) {
        this.response = response;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setNeedMoreInfo(Boolean needMoreInfo) {
        this.needMoreInfo = needMoreInfo;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}