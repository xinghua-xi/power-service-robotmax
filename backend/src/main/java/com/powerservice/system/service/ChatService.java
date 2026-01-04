package com.powerservice.system.service;

import com.powerservice.system.dto.ChatRequest;
import com.powerservice.system.dto.ChatResponse;
import com.powerservice.system.entity.ChatRecord;
import com.powerservice.system.entity.KnowledgeBase;
import com.powerservice.system.entity.ServiceType;
import com.powerservice.system.entity.User;
import com.powerservice.system.repository.ChatRecordRepository;
import com.powerservice.system.repository.KnowledgeBaseRepository;
import com.powerservice.system.repository.ServiceTypeRepository;
import com.powerservice.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private ChatRecordRepository chatRecordRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatResponse processMessage(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String userMessage = request.getMessage();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : generateSessionId();

        // 查找用户
        User user = null;
        if (request.getUserId() != null) {
            Optional<User> userOptional = userRepository.findById(request.getUserId());
            user = userOptional.orElse(null);
        }

        // 1. 尝试从知识库匹配答案
        List<KnowledgeBase> knowledgeMatches = knowledgeBaseRepository.findByKeyword(userMessage);

        if (!knowledgeMatches.isEmpty()) {
            KnowledgeBase kb = knowledgeMatches.get(0);
            String response = kb.getAnswer();
            String serviceType = kb.getServiceType() != null ? kb.getServiceType().getName() : null;

            // 增加命中次数
            kb.setHitCount(kb.getHitCount() + 1);
            knowledgeBaseRepository.save(kb);

            // 保存聊天记录
            saveChatRecord(sessionId, user, userMessage, response, kb.getServiceType(), startTime);

            return new ChatResponse(response, serviceType, false);
        }

        // 2. 基于关键词的回复逻辑
        ChatResponse customResponse = generateCustomResponse(userMessage);

        // 保存聊天记录
        ServiceType serviceType = null;
        if (customResponse.getServiceType() != null) {
            serviceType = serviceTypeRepository.findByName(customResponse.getServiceType()).orElse(null);
        }
        saveChatRecord(sessionId, user, userMessage, customResponse.getResponse(), serviceType, startTime);

        customResponse.setSessionId(sessionId);
        return customResponse;
    }

    private ChatResponse generateCustomResponse(String userMessage) {
        // 简单的关键词匹配逻辑
        if (userMessage.contains("故障") || userMessage.contains("报修")) {
            return new ChatResponse(
                    "您好，故障报修服务已受理。请提供您的详细地址和故障现象描述，我们将尽快安排维修人员处理。",
                    "故障报修", true
            );
        } else if (userMessage.contains("业务") || userMessage.contains("办理")) {
            return new ChatResponse(
                    "电力业务办理包括新装、增容、变更用电等。请问您需要办理哪项具体业务？",
                    "电力业务", true
            );
        } else if (userMessage.contains("咨询") || userMessage.contains("问题")) {
            return new ChatResponse(
                    "用电咨询请详细描述您遇到的问题，我们会为您提供专业的解答。",
                    "用电咨询", true
            );
        } else if (userMessage.contains("安全") || userMessage.contains("宣传")) {
            return new ChatResponse(
                    "安全用电提醒：请勿私拉乱接电线，定期检查家用电器，雷雨天气注意用电安全，远离电力设施。",
                    "安全宣传", false
            );
        } else if (userMessage.contains("政策") || userMessage.contains("电价")) {
            return new ChatResponse(
                    "现行电价政策为阶梯电价，具体标准可查询当地供电营业厅或官方网站。您也可以提供具体问题，我会详细为您解读。",
                    "政策解读", true
            );
        } else if (userMessage.contains("电表") || userMessage.contains("计量")) {
            return new ChatResponse(
                    "电表问题包括计量不准、显示异常、安装问题等。请提供您的用户编号和具体问题描述，我们将安排核查。",
                    "电表问题", true
            );
        } else if (userMessage.contains("电话") || userMessage.contains("联系")) {
            return new ChatResponse(
                    "我们的24小时客服电话是95598，紧急情况请直接拨打。平时咨询也可以通过这个智能助手进行。",
                    null, false
            );
        } else if (userMessage.contains("上门") || userMessage.contains("预约")) {
            return new ChatResponse(
                    "上门服务需要预约登记，请提供您的姓名、联系电话、详细地址和需要服务的具体内容。",
                    "上门服务", true
            );
        } else {
            return new ChatResponse(
                    "抱歉，我没有完全理解您的问题。您可以尝试选择下方的服务分类，或者描述更具体的问题，我会尽力为您解答。",
                    null, false
            );
        }
    }

    private void saveChatRecord(String sessionId, User user, String userMessage, String botResponse, 
                                ServiceType serviceType, long startTime) {
        ChatRecord record = new ChatRecord();
        record.setSessionId(sessionId);
        record.setUser(user);
        record.setUserMessage(userMessage);
        // 确保bot_response不为null
        record.setBotResponse(botResponse != null ? botResponse : "");
        record.setServiceType(serviceType);
        record.setResponseTime((int) (System.currentTimeMillis() - startTime));
        record.setCreatedAt(LocalDateTime.now());

        chatRecordRepository.save(record);
    }

    private String generateSessionId() {
        return "SESS_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public List<ChatRecord> getChatHistory(String sessionId) {
        return chatRecordRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
