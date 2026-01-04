package com.powerservice.system.util;
import java.io.InputStream;  // 新增这一行
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 对话消息类，用于表示多轮对话中的一条消息
 */
class Message {
    private String role;  // 角色：user或assistant
    private String content;  // 消息内容

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }
}

/**
 * DeepSeek API客户端工具类
 * 用于封装DeepSeek API的调用逻辑
 */
@Component
public class DeepSeekApiClient {

    // 注入配置参数
    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    // 新增：是否使用模拟服务（true为使用模拟服务，false为调用真实API）
    @Value("${deepseek.use-mock:false}")
    private boolean useMock;

    // 为OkHttpClient添加超时设置，解决超时问题
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时30秒
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时60秒
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时60秒
            .callTimeout(120, java.util.concurrent.TimeUnit.SECONDS)   // 调用总超时120秒
            .build();

    // 模拟回答映射表
    private static final Map<String, String> mockResponses = new HashMap<>();

    static {
        // 初始化常见问题的模拟回答
        String paymentMethods = "您可以通过以下方式快捷缴费：\n1. 微信/支付宝生活缴费功能\n2. 电力公司官方APP\n3. 银行代扣服务\n4. 线下营业厅自助终端";
        
        // 添加多种关于缴费方法的问题变体
        mockResponses.put("如何快捷缴费", paymentMethods);
        mockResponses.put("缴电费有那些方法", paymentMethods);
        mockResponses.put("电费缴费方式", paymentMethods);
        mockResponses.put("怎么交电费", paymentMethods);
        mockResponses.put("缴费方式", paymentMethods);
        
        mockResponses.put("电费查询", "您可以通过以下方式查询电费：\n1. 登录电力公司官方网站\n2. 使用电力公司APP\n3. 发送短信查询\n4. 拨打电力服务热线");
        mockResponses.put("故障报修", "如果您遇到电力故障，请拨打电力服务热线95598进行报修，或通过官方APP在线提交报修申请。");
        mockResponses.put("开户流程", "电力开户流程：\n1. 准备身份证、房产证等材料\n2. 前往当地电力营业厅\n3. 填写开户申请表\n4. 工作人员审核后办理开户");
        mockResponses.put("电价标准", "当前电价标准根据不同用户类型有所区别：\n- 居民用电：0.56元/度（第一档）\n- 商业用电：1.02元/度\n- 工业用电：0.85元/度");
    }

    /**
     * 调用DeepSeek对话API（单轮对话）
     * @param prompt 用户提问内容
     * @return AI响应结果（原始JSON格式）
     */
    public String chatWithDeepSeek(String prompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));
        return chatWithDeepSeek(messages);
    }

    /**
     * 调用DeepSeek对话API（多轮对话）
     * @param messages 对话历史消息列表
     * @return AI响应结果（原始JSON格式）
     */
    public String chatWithDeepSeek(List<Message> messages) {
        // 如果使用模拟服务
        if (useMock) {
            // 获取最后一条用户消息作为prompt
            String lastPrompt = messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .reduce((first, second) -> second)
                    .map(Message::getContent)
                    .orElse("如何使用电力服务");
            return getMockResponse(lastPrompt);
        }

        // 使用Jackson构造JSON请求体
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBodyNode = objectMapper.createObjectNode();
        requestBodyNode.put("model", model);
        requestBodyNode.put("temperature", 0.3); // 降低temperature，减少创造性思考，加快生成速度
        requestBodyNode.put("max_tokens", 256);

        // 构造messages数组
        ArrayNode messagesArray = requestBodyNode.putArray("messages");
        for (Message message : messages) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", message.getRole());
            messageNode.put("content", message.getContent());
        }

        // 转换为JSON字符串
        String requestBody;
        if (requestBodyNode != null) {
            try {
                requestBody = objectMapper.writeValueAsString(requestBodyNode);
            } catch (JsonProcessingException e) {
                // 处理JSON序列化异常
                throw new RuntimeException("Failed to serialize requestBodyNode to JSON string", e);
            }
        } else {
            requestBody = null;
        }


        // 构建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")  // DeepSeek对话接口
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)  // 鉴权头
                .addHeader("Content-Type", "application/json")
                .build();

        // 发送请求并解析响应
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                // 如果真实API调用失败，尝试返回模拟响应
                System.err.println("真实API调用失败，返回模拟响应: " + response.code() + " " + response.message());
                // 获取最后一条用户消息作为prompt
                String lastPrompt = messages.stream()
                        .filter(m -> "user".equals(m.getRole()))
                        .reduce((first, second) -> second)
                        .map(Message::getContent)
                        .orElse("如何使用电力服务");
                return getMockResponse(lastPrompt);
            }
            return response.body().string();  // 返回原始JSON响应
        } catch (Exception e) {
            // 任何异常都返回模拟响应
            System.err.println("API调用异常，返回模拟响应: " + e.getMessage());
            // 获取最后一条用户消息作为prompt
            String lastPrompt = messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .reduce((first, second) -> second)
                    .map(Message::getContent)
                    .orElse("如何使用电力服务");
            return getMockResponse(lastPrompt);
        }
    }
    
    /**
     * 获取模拟AI响应
     * @param prompt 用户提问
     * @return 模拟的AI响应（JSON格式）
     */
    private String getMockResponse(String prompt) {
        // 查找匹配的模拟回答
        String answer = mockResponses.getOrDefault(prompt, "感谢您的提问！我是电力服务智能助手，请问还有什么可以帮您的？");

        // 构建符合DeepSeek API响应格式的JSON
        return buildMockResponseJson(answer, prompt.length());
    }

    /**
     * 构建模拟响应JSON
     * @param answer 回答内容
     * @param promptLength 提问长度
     * @return 模拟的AI响应（JSON格式）
     */
    private String buildMockResponseJson(String answer, int promptLength) {
        return String.format("{\"id\": \"mock-%d\", \"object\": \"chat.completion\", \"created\": %d, \"model\": \"%s\", \"choices\": [{\"index\": 0, \"message\": {\"role\": \"assistant\", \"content\": \"%s\"}, \"finish_reason\": \"stop\"}], \"usage\": {\"prompt_tokens\": %d, \"completion_tokens\": %d, \"total_tokens\": %d}}",
                System.currentTimeMillis(),
                System.currentTimeMillis() / 1000,
                model,
                answer.replace("\"", "\\\""),
                promptLength / 2, // 估算的prompt tokens
                answer.length() / 2, // 估算的completion tokens
                (promptLength + answer.length()) / 2); // 估算的总tokens
    }
    
    /**
     * 调用DeepSeek对话API（流式响应，多轮对话）
     * @param messages 对话历史消息列表
     * @param callback 流式响应回调函数，用于处理实时返回的内容
     */
    public void chatWithDeepSeekStream(List<Message> messages, Consumer<String> callback) {
        System.out.println("[DeepSeek API] 开始处理流式请求");
        
        // 如果使用模拟服务
        if (useMock) {
            System.out.println("[DeepSeek API] 使用模拟服务");
            // 获取最后一条用户消息作为prompt
            String lastPrompt = messages.stream()
                    .filter(m -> "user".equals(m.getRole()))
                    .reduce((first, second) -> second)
                    .map(Message::getContent)
                    .orElse("如何使用电力服务");
                    
            System.out.println("[DeepSeek API] 模拟请求内容: " + lastPrompt);
                    
            // 模拟流式响应
            String mockResponse = getMockResponse(lastPrompt);
            
            System.out.println("[DeepSeek API] 模拟响应内容: " + mockResponse);
            
            // 提取实际的回答内容
            String content;
            try {
                int contentStart = mockResponse.indexOf("\"content\": \"") + 11; // 11 is the length of "content": ""
                int contentEnd = mockResponse.indexOf("\"}, \"finish_reason\"");
                if (contentStart < 11 || contentEnd <= contentStart) {
                    // 解析失败，使用默认回答
                    content = "感谢您的提问！我是电力服务智能助手，请问还有什么可以帮您的？";
                } else {
                    content = mockResponse.substring(contentStart, contentEnd);
                    content = content.replace("\\\"", "");
                }
            } catch (Exception e) {
                // 解析异常，使用默认回答
                System.err.println("[DeepSeek API] 模拟响应解析异常: " + e.getMessage());
                e.printStackTrace();
                content = "感谢您的提问！我是电力服务智能助手，请问还有什么可以帮您的？";
            }
            
            System.out.println("[DeepSeek API] 提取的模拟回答内容: " + content);
            
            // 简单模拟流式输出，将响应按字符分割，逐段发送
            boolean interrupted = false;
            try {
                for (int i = 0; i < content.length() && !interrupted; i += 10) {
                    int end = Math.min(i + 10, content.length());
                    try {
                        Thread.sleep(100); // 模拟打字效果
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        interrupted = true;
                        System.err.println("[DeepSeek API] 模拟流式响应被中断: " + e.getMessage());
                        break;
                    }
                    String chunk = content.substring(i, end);
                    System.out.println("[DeepSeek API] 发送模拟响应片段: " + chunk);
                    callback.accept(chunk);
                }
            } finally {
                // 确保即使中断也能完成响应处理
                if (interrupted) {
                    // 可以选择发送一个结束标记，但这里保持简单
                    System.out.println("[DeepSeek API] 模拟流式响应已中断");
                } else {
                    System.out.println("[DeepSeek API] 模拟流式响应完成");
                }
            }
            return;
        }

        // 使用Jackson构造JSON请求体
        System.out.println("[DeepSeek API] 使用真实API服务");
        
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBodyNode = objectMapper.createObjectNode();
        requestBodyNode.put("model", model);
        requestBodyNode.put("temperature", 0.3); // 降低temperature，减少创造性思考，加快生成速度
        requestBodyNode.put("max_tokens",200 ); // 限制回复长度，符合营业厅问答的简洁需求
        requestBodyNode.put("stream", true); // 启用流式响应

        // 构造messages数组
        ArrayNode messagesArray = requestBodyNode.putArray("messages");
        for (Message message : messages) {
            ObjectNode messageNode = messagesArray.addObject();
            messageNode.put("role", message.getRole());
            messageNode.put("content", message.getContent());
        }

        // 转换为JSON字符串
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestBodyNode);
        } catch (JsonProcessingException e) {
            // 处理JSON序列化异常
            throw new RuntimeException("Failed to serialize request body to JSON", e);
        }


        // 打印请求日志
        System.out.println("[DeepSeek API] 请求URL: " + baseUrl + "/chat/completions");
        System.out.println("[DeepSeek API] 请求头: Authorization=Bearer " + (apiKey != null ? "********" : "null"));
        System.out.println("[DeepSeek API] 请求体: " + requestBody);

        // 构建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")  // DeepSeek对话接口
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)  // 鉴权头
                .addHeader("Content-Type", "application/json")
                .build();

        // 发送请求并处理流式响应
        System.out.println("[DeepSeek API] 正在发送请求...");
        try (Response response = okHttpClient.newCall(request).execute()) {
            // 打印响应状态日志
            System.out.println("[DeepSeek API] 响应状态码: " + response.code());
            System.out.println("[DeepSeek API] 响应消息: " + response.message());
            
            if (!response.isSuccessful()) {
                // 读取错误响应内容
                String errorResponse = "";
                if (response.body() != null) {
                    errorResponse = response.body().string();
                    System.out.println("[DeepSeek API] 错误响应内容: " + errorResponse);
                }
                // API调用失败，发送友好的错误信息
                String errorMessage = "API调用失败: 服务器返回状态码 " + response.code() + "，错误信息: " + errorResponse;
                System.err.println("[DeepSeek API] " + errorMessage);
                callback.accept(errorMessage);
                return;
            }

            // 处理流式响应
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    String errorMessage = "API调用失败: 服务器没有返回响应内容";
                    System.err.println("[DeepSeek API] " + errorMessage);
                    callback.accept(errorMessage);
                    return;
                }

                // 逐行读取流式响应，使用更直接的UTF-8解码方式
                System.out.println("[DeepSeek API] 开始读取流式响应...");
                
                // 使用OkHttp的charStream()方法，它会自动处理字符编码
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[DeepSeek API] 接收到响应行: " + line);
                        
                        if (line.isEmpty() || line.equals("data: [DONE]")) {
                            continue;
                        }

                        // 直接转发原始响应行，不做任何处理
                        callback.accept(line);
                    }
                    
                    System.out.println("[DeepSeek API] 流式响应处理完成");
                }
            }
        } catch (IOException e) {
            // 网络异常，发送友好的错误信息
            String errorMessage = "网络错误: 无法连接到API服务器，请检查网络连接";
            System.err.println("[DeepSeek API] " + errorMessage);
            System.err.println("[DeepSeek API] 异常详情: " + e.getMessage());
            e.printStackTrace();
            callback.accept(errorMessage);
        }
        
        System.out.println("[DeepSeek API] 流式请求处理结束");
    }
    
    /**
     * 调用DeepSeek对话API（流式响应，单轮对话）
     * @param prompt 用户提问内容
     * @param callback 流式响应回调函数
     */
    public void chatWithDeepSeekStream(String prompt, Consumer<String> callback) {
        List<Message> messages = new ArrayList<>();
        // 添加电力服务营业厅智能问答助手的系统提示词
        messages.add(new Message("system", "你是电力服务营业厅智能问答助手，需用亲切通俗的语气服务用户。核心负责解答电费查询缴纳、电表报装/故障报修、用电套餐办理、峰谷电价标准、停电通知查询、充电桩安装申请这些电力相关问题；回答需简洁，步骤清晰，遇到无法解答的问题请引导用户拨打电力客服热线 95598，禁止回复与电力服务无关的内容。"));
        messages.add(new Message("user", prompt));
        chatWithDeepSeekStream(messages, callback);
    }
}