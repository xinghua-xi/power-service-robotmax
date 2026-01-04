package com.powerservice.system.controller;

import com.powerservice.system.util.DeepSeekApiClient;
import com.powerservice.system.util.RedisCacheUtil;
import com.powerservice.system.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI控制器
 * 提供与DeepSeek API交互的接口
 */
@RestController
@RequestMapping("/api")
public class AiController {

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * AI对话接口
     * @param param 接收前端传递的{prompt: "用户问题"}
     * @return AI响应结果
     */
    @PostMapping("/chat")
    public ApiResponse<Object> chat(@RequestBody Map<String, String> param) {
        String prompt = param.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ApiResponse.error("提问内容不能为空");
        }

        try {
            // 1. 生成缓存Key（去除空格，提高缓存命中率）
            String cacheKey = "ai_chat:" + prompt.trim();

            // 2. 先查缓存
            if (redisCacheUtil.hasCache(cacheKey)) {
                String cachedResponse = redisCacheUtil.getCache(cacheKey);
                return ApiResponse.success("AI对话成功（缓存）", cachedResponse);
            }

            // 3. 缓存不存在，调用DeepSeek API
            String response = deepSeekApiClient.chatWithDeepSeek(prompt);

            // 4. 将结果写入缓存（设置过期时间为1小时）
            redisCacheUtil.setCache(cacheKey, response, 1, TimeUnit.HOURS);

            return ApiResponse.success("AI对话成功", response);
        } catch (Exception e) {  // 修改为捕获 Exception 或确保方法声明抛出 IOException
            e.printStackTrace();
            return ApiResponse.error("AI调用失败: " + e.getMessage());
        }
    }


    /**
     * AI对话接口（SSE流式响应）
     * @param prompt 用户问题
     * @return SSE流式响应结果
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam(required = false) String prompt) {
        // 1. 空参数处理：直接返回错误，不创建流式连接
        if (prompt == null || prompt.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                emitter.send("data: 请输入有效的问题！\n\n", new MediaType("text", "event-stream", StandardCharsets.UTF_8));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 设置SSE超时时间为5分钟，避免连接过早断开
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 添加连接关闭时的回调
        emitter.onCompletion(() -> {
            System.out.println("SSE连接已正常关闭");
        });

        emitter.onTimeout(() -> {
            try {
                emitter.send("data: 连接超时，请稍后重试！\n\n", new MediaType("text", "event-stream", StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                emitter.complete();
                System.out.println("SSE连接已超时关闭");
            }
        });

        emitter.onError((e) -> {
            System.out.println("SSE连接发生错误: " + e.getMessage());
        });

        // 异步处理SSE响应，避免阻塞
        CompletableFuture.runAsync(() -> {
            String cacheKey = "ai_chat:" + prompt.trim();

            try {
                // 先查缓存
                if (redisCacheUtil.hasCache(cacheKey)) {
                    String cachedResponse = redisCacheUtil.getCache(cacheKey);
                    // 模拟流式发送缓存的响应（纯文本格式）
                    for (int i = 0; i < cachedResponse.length(); i += 10) {
                        int end = Math.min(i + 10, cachedResponse.length());
                        String chunk = cachedResponse.substring(i, end);
                        try {
                            Thread.sleep(50);
                            // 直接发送纯文本，不包装JSON
                            emitter.send(chunk, new MediaType("text", "plain", StandardCharsets.UTF_8));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    emitter.complete();
                    return;
                }

                // 缓存不存在，调用DeepSeek API的流式接口
                StringBuilder fullResponse = new StringBuilder();
                boolean[] errorOccurred = {false}; // 使用数组来在lambda中修改值

                try {
                    deepSeekApiClient.chatWithDeepSeekStream(prompt, line -> {
                        try {
                            if (line.startsWith("API调用失败:") || line.startsWith("网络错误:")) {
                                // 直接发送错误信息
                                emitter.send(line, new MediaType("text", "plain", StandardCharsets.UTF_8));
                                errorOccurred[0] = true;
                                return;
                            }

                            // 解析DeepSeek回复的纯文本内容
                            if (line.startsWith("data: ")) {
                                String jsonStr = line.substring("data: ".length());
                                try {
                                    ObjectNode node = objectMapper.readValue(jsonStr, ObjectNode.class);
                                    ArrayNode choices = (ArrayNode) node.get("choices");
                                    if (choices != null && choices.size() > 0) {
                                        ObjectNode choice = (ObjectNode) choices.get(0);
                                        ObjectNode delta = (ObjectNode) choice.get("delta");
                                        if (delta != null) {
                                            String content = delta.path("content").asText("");
                                            if (!content.isEmpty()) {
                                                // 直接发送纯文本内容，不包装JSON
                                                emitter.send(content, new MediaType("text", "plain", StandardCharsets.UTF_8));
                                                fullResponse.append(content);
                                            }
                                        }
                                    }
                                } catch (JsonProcessingException e) {
                                    // 忽略JSON解析错误
                                }
                            }
                        } catch (IOException e) {
                            try {
                                // 直接发送错误信息
                                emitter.send("错误：发送响应时出错: " + e.getMessage(), new MediaType("text", "plain", StandardCharsets.UTF_8));
                            } catch (IOException ignored) {
                            }
                            errorOccurred[0] = true;
                        }
                    });

                    // 对话完成后，将完整结果写入缓存
                    if (!errorOccurred[0] && fullResponse.length() > 0) {
                        redisCacheUtil.setCache(cacheKey, fullResponse.toString(), 1, TimeUnit.HOURS);
                    }
                } catch (Exception e) {
                    System.err.println("API调用异常: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        emitter.send("data: 错误：API调用异常: " + e.getMessage() + "\n\n", new MediaType("text", "event-stream", StandardCharsets.UTF_8));
                    } catch (IOException ignored) {
                    }
                    emitter.completeWithError(e);
                    return;
                }

                // 关闭SSE连接
                emitter.complete();
            } catch (Exception e) {
                System.err.println("流式请求处理异常: " + e.getMessage());
                e.printStackTrace();
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}