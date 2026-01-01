package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.domain.entity.AIChatMessage;
import com.erkang.domain.entity.AIChatSession;
import com.erkang.domain.entity.AITask;
import com.erkang.mapper.AIChatMessageMapper;
import com.erkang.mapper.AIChatSessionMapper;
import com.erkang.mapper.AITaskMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI服务
 * _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AITaskMapper aiTaskMapper;
    private final AIChatSessionMapper aiChatSessionMapper;
    private final AIChatMessageMapper aiChatMessageMapper;

    // 敏感信息正则模式
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\d{17}[\\dXx]");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:姓名|患者|病人)[：:]*([\\u4e00-\\u9fa5]{2,4})");
    
    // 免责声明
    private static final String DISCLAIMER = "\n\n【免责声明】以上内容仅供健康参考，不构成医疗诊断或治疗建议。如有不适，请及时就医。";
    
    // 急危重症关键词
    private static final String[] EMERGENCY_KEYWORDS = {
        "胸痛", "呼吸困难", "意识丧失", "大出血", "剧烈头痛", "高烧不退",
        "心悸", "窒息", "抽搐", "昏迷", "中毒", "严重过敏"
    };

    /**
     * 创建对话会话
     */
    @Transactional
    @Auditable(action = "CREATE_AI_SESSION", module = "ai")
    public AIChatSession createSession(Long userId, String sessionType) {
        AIChatSession session = new AIChatSession();
        session.setUserId(userId);
        session.setSessionType(sessionType != null ? sessionType : "HEALTH_QA");
        session.setStatus("ACTIVE");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        aiChatSessionMapper.insert(session);
        
        log.info("创建AI会话: sessionId={}, userId={}", session.getId(), userId);
        return session;
    }

    /**
     * 健康问答
     * _Requirements: 10.1_
     */
    @Transactional
    @Auditable(action = "AI_HEALTH_QA", module = "ai")
    public String healthQA(Long sessionId, Long userId, String question) {
        long startTime = System.currentTimeMillis();
        
        // 1. 输入脱敏
        String sanitizedQuestion = sanitizeInput(question);
        
        // 2. 保存用户消息
        saveMessage(sessionId, "USER", sanitizedQuestion);
        
        // 3. 创建AI任务
        AITask task = createTask("HEALTH_QA", userId, sanitizedQuestion);
        
        try {
            // 4. 调用AI模型（模拟）
            String rawResponse = callAIModel(sanitizedQuestion);
            
            // 5. 输出合规校验
            String compliantResponse = ensureCompliance(rawResponse, sanitizedQuestion);
            
            // 6. 保存AI响应
            saveMessage(sessionId, "ASSISTANT", compliantResponse);
            
            // 7. 更新任务状态
            long latency = System.currentTimeMillis() - startTime;
            completeTask(task, compliantResponse, (int) latency);
            
            log.info("AI健康问答完成: sessionId={}, latency={}ms", sessionId, latency);
            return compliantResponse;
            
        } catch (Exception e) {
            failTask(task, e.getMessage());
            throw e;
        }
    }

    /**
     * 输入脱敏
     * _Requirements: 10.6_
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        String result = input;
        
        // 先脱敏身份证号（18位，避免被手机号正则误匹配）
        result = ID_CARD_PATTERN.matcher(result).replaceAll("[身份证号已隐藏]");
        
        // 再脱敏手机号
        result = PHONE_PATTERN.matcher(result).replaceAll("[手机号已隐藏]");
        
        // 脱敏姓名
        Matcher nameMatcher = NAME_PATTERN.matcher(result);
        while (nameMatcher.find()) {
            String name = nameMatcher.group(1);
            if (name != null && name.length() >= 2) {
                String masked = name.charAt(0) + "*".repeat(name.length() - 1);
                result = result.replace(name, masked);
            }
        }
        
        return result;
    }

    /**
     * 输出合规校验
     * _Requirements: 10.2, 10.3, 10.4_
     */
    public String ensureCompliance(String response, String question) {
        if (response == null) {
            return DISCLAIMER;
        }
        
        StringBuilder result = new StringBuilder();
        
        // 检查急危重症风险
        String emergencyWarning = checkEmergencyRisk(question);
        if (emergencyWarning != null) {
            result.append(emergencyWarning).append("\n\n");
        }
        
        // 过滤处方药剂量信息
        String filteredResponse = filterPrescriptionDosage(response);
        result.append(filteredResponse);
        
        // 添加免责声明
        if (!response.contains("免责声明")) {
            result.append(DISCLAIMER);
        }
        
        return result.toString();
    }

    /**
     * 检查急危重症风险
     * _Requirements: 10.4_
     */
    private String checkEmergencyRisk(String question) {
        if (question == null) {
            return null;
        }
        
        for (String keyword : EMERGENCY_KEYWORDS) {
            if (question.contains(keyword)) {
                return "⚠️ 【紧急提醒】您描述的症状可能涉及急危重症，请立即就医或拨打120急救电话！";
            }
        }
        return null;
    }

    /**
     * 过滤处方药剂量信息
     * _Requirements: 10.3_
     */
    private String filterPrescriptionDosage(String response) {
        if (response == null) {
            return "";
        }
        
        // 过滤具体剂量信息（如：每次2片、每日3次、500mg等）
        String result = response;
        result = result.replaceAll("每次\\d+[片粒支ml毫升]", "每次[剂量请遵医嘱]");
        result = result.replaceAll("每日\\d+次", "每日[次数请遵医嘱]");
        result = result.replaceAll("\\d+mg", "[剂量请遵医嘱]");
        result = result.replaceAll("\\d+毫克", "[剂量请遵医嘱]");
        
        return result;
    }

    /**
     * 模拟调用AI模型
     */
    private String callAIModel(String question) {
        // 实际项目中这里会调用大模型API
        // 这里返回模拟响应
        return "根据您的描述，这可能是常见的耳鼻喉科症状。建议您：\n" +
               "1. 保持充足休息\n" +
               "2. 多饮水\n" +
               "3. 避免刺激性食物\n" +
               "4. 如症状持续或加重，请及时就医";
    }

    /**
     * 保存消息
     */
    private void saveMessage(Long sessionId, String role, String content) {
        AIChatMessage message = new AIChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTokens(content != null ? content.length() / 4 : 0); // 简单估算
        message.setCreatedAt(LocalDateTime.now());
        aiChatMessageMapper.insert(message);
    }

    /**
     * 创建AI任务
     */
    private AITask createTask(String taskType, Long userId, String requestData) {
        AITask task = new AITask();
        task.setTaskType(taskType);
        task.setUserId(userId);
        task.setRequestData(requestData);
        task.setStatus("PROCESSING");
        task.setCreatedAt(LocalDateTime.now());
        aiTaskMapper.insert(task);
        return task;
    }

    /**
     * 完成任务
     */
    private void completeTask(AITask task, String responseData, int latencyMs) {
        task.setResponseData(responseData);
        task.setStatus("COMPLETED");
        task.setLatencyMs(latencyMs);
        task.setTokensUsed(responseData != null ? responseData.length() / 4 : 0);
        task.setCompletedAt(LocalDateTime.now());
        aiTaskMapper.updateById(task);
    }

    /**
     * 任务失败
     */
    private void failTask(AITask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setCompletedAt(LocalDateTime.now());
        aiTaskMapper.updateById(task);
    }

    /**
     * 关闭会话
     */
    @Transactional
    public AIChatSession closeSession(Long sessionId) {
        AIChatSession session = aiChatSessionMapper.selectById(sessionId);
        if (session != null) {
            session.setStatus("CLOSED");
            session.setUpdatedAt(LocalDateTime.now());
            aiChatSessionMapper.updateById(session);
        }
        return session;
    }

    /**
     * 查询会话详情
     */
    public AIChatSession getSessionById(Long sessionId) {
        return aiChatSessionMapper.selectById(sessionId);
    }

    /**
     * 查询用户会话列表
     */
    public List<AIChatSession> listSessionsByUserId(Long userId) {
        LambdaQueryWrapper<AIChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIChatSession::getUserId, userId)
               .orderByDesc(AIChatSession::getCreatedAt);
        return aiChatSessionMapper.selectList(wrapper);
    }

    /**
     * 查询会话消息
     */
    public List<AIChatMessage> listMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<AIChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIChatMessage::getSessionId, sessionId)
               .orderByAsc(AIChatMessage::getCreatedAt);
        return aiChatMessageMapper.selectList(wrapper);
    }
}
