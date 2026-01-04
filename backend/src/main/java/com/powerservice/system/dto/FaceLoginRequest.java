package com.powerservice.system.dto;

public class FaceLoginRequest {
    private String faceData;

    private String sessionId;

    // 1. 无参构造函数（Spring MVC 接收请求、参数校验必需）
    public FaceLoginRequest() {
    }

    // 2. 全参构造函数（方便后端手动创建对象）
    public FaceLoginRequest(String faceData, String sessionId) {
        this.faceData = faceData;
        this.sessionId = sessionId;
    }

    // 3. 仅含 faceData 的构造函数（可选，按需使用）
    public FaceLoginRequest(String faceData) {
        this.faceData = faceData;
    }

    // ==================== Getter 方法 ====================
    public String getFaceData() {
        return faceData;
    }

    public String getSessionId() {
        return sessionId;
    }

    // ==================== Setter 方法 ====================
    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}