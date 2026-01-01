package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.domain.entity.AIChatMessage;
import com.erkang.domain.entity.AIChatSession;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI控制器
 * _Requirements: 10.1, 10.5_
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * 创建对话会话
     */
    @PostMapping("/session")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "CREATE_AI_SESSION", module = "ai")
    public Result<AIChatSession> createSession(@RequestParam(required = false) String sessionType) {
        Long userId = UserContext.getUserId();
        AIChatSession session = aiService.createSession(userId, sessionType);
        return Result.success(session);
    }

    /**
     * 健康问答
     */
    @PostMapping("/session/{sessionId}/chat")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "AI_HEALTH_QA", module = "ai")
    public Result<Map<String, String>> healthQA(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        String question = request.get("question");
        String answer = aiService.healthQA(sessionId, userId, question);
        return Result.success(Map.of("answer", answer));
    }

    /**
     * 关闭会话
     */
    @PostMapping("/session/{sessionId}/close")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<AIChatSession> closeSession(@PathVariable Long sessionId) {
        AIChatSession session = aiService.closeSession(sessionId);
        return Result.success(session);
    }

    /**
     * 查询会话详情
     */
    @GetMapping("/session/{sessionId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<AIChatSession> getSession(@PathVariable Long sessionId) {
        AIChatSession session = aiService.getSessionById(sessionId);
        return Result.success(session);
    }

    /**
     * 查询我的会话列表
     */
    @GetMapping("/session/my")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<AIChatSession>> listMySessions() {
        Long userId = UserContext.getUserId();
        List<AIChatSession> list = aiService.listSessionsByUserId(userId);
        return Result.success(list);
    }

    /**
     * 查询会话消息
     */
    @GetMapping("/session/{sessionId}/messages")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<List<AIChatMessage>> listMessages(@PathVariable Long sessionId) {
        List<AIChatMessage> list = aiService.listMessagesBySessionId(sessionId);
        return Result.success(list);
    }
}
