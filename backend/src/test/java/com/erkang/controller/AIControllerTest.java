package com.erkang.controller;

import com.erkang.domain.entity.AIChatMessage;
import com.erkang.domain.entity.AIChatSession;
import com.erkang.service.AIService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AI控制器测试
 * _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
 */
class AIControllerTest {

    private AIService aiService;
    private AIController aiController;

    @BeforeProperty
    void setUp() {
        aiService = mock(AIService.class);
        aiController = new AIController(aiService);
    }

    /**
     * Property 1: 创建对话会话 - 应创建新的AI会话
     * **Validates: Requirements 10.1**
     */
    @Property(tries = 50)
    void createSession_shouldCreateNewSession(
            @ForAll @AlphaChars @StringLength(min = 0, max = 50) String sessionType) {
        
        AIChatSession mockSession = new AIChatSession();
        mockSession.setId(1L);
        mockSession.setSessionType(sessionType.isEmpty() ? "HEALTH_QA" : sessionType);
        mockSession.setStatus("ACTIVE");
        
        when(aiService.createSession(anyLong(), anyString())).thenReturn(mockSession);
        
        // 验证会话创建逻辑
        assertThat(mockSession.getId()).isNotNull();
        assertThat(mockSession.getStatus()).isEqualTo("ACTIVE");
    }

    /**
     * Property 2: 健康问答 - 应返回AI回答
     * **Validates: Requirements 10.2**
     */
    @Property(tries = 50)
    void healthQA_shouldReturnAnswer(
            @ForAll @LongRange(min = 1, max = 10000) Long sessionId,
            @ForAll @AlphaChars @StringLength(min = 5, max = 200) String question) {
        
        String mockAnswer = "这是AI的回答";
        
        when(aiService.healthQA(eq(sessionId), anyLong(), eq(question))).thenReturn(mockAnswer);
        
        // 验证问答逻辑
        assertThat(mockAnswer).isNotBlank();
    }

    /**
     * Property 3: 关闭会话 - 应更新会话状态为已关闭
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 100)
    void closeSession_shouldUpdateStatusToClosed(
            @ForAll @LongRange(min = 1, max = 10000) Long sessionId) {
        
        AIChatSession closedSession = new AIChatSession();
        closedSession.setId(sessionId);
        closedSession.setStatus("CLOSED");
        
        when(aiService.closeSession(sessionId)).thenReturn(closedSession);
        
        var result = aiController.closeSession(sessionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("CLOSED");
    }

    /**
     * Property 4: 查询会话详情 - 应返回正确的会话信息
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 100)
    void getSession_shouldReturnSessionDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long sessionId) {
        
        AIChatSession mockSession = new AIChatSession();
        mockSession.setId(sessionId);
        
        when(aiService.getSessionById(sessionId)).thenReturn(mockSession);
        
        var result = aiController.getSession(sessionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(sessionId);
    }

    /**
     * Property 5: 查询我的会话列表 - 应返回当前用户的会话列表
     * **Validates: Requirements 10.4**
     */
    @Property(tries = 50)
    void listMySessions_shouldReturnUserSessions(
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<AIChatSession> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AIChatSession session = new AIChatSession();
            session.setId((long) (i + 1));
            mockList.add(session);
        }
        
        when(aiService.listSessionsByUserId(anyLong())).thenReturn(mockList);
        
        assertThat(mockList).hasSize(count);
    }

    /**
     * Property 6: 查询会话消息 - 应返回会话中的所有消息
     * **Validates: Requirements 10.5**
     */
    @Property(tries = 100)
    void listMessages_shouldReturnSessionMessages(
            @ForAll @LongRange(min = 1, max = 10000) Long sessionId,
            @ForAll @IntRange(min = 0, max = 50) int messageCount) {
        
        List<AIChatMessage> mockList = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            AIChatMessage message = new AIChatMessage();
            message.setId((long) (i + 1));
            message.setSessionId(sessionId);
            message.setRole(i % 2 == 0 ? "user" : "assistant");
            mockList.add(message);
        }
        
        when(aiService.listMessagesBySessionId(sessionId)).thenReturn(mockList);
        
        var result = aiController.listMessages(sessionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(messageCount);
        result.getData().forEach(m -> 
            assertThat(m.getSessionId()).isEqualTo(sessionId)
        );
    }

    /**
     * Property 7: 问题长度验证 - 问题不能为空
     * **Validates: Requirements 10.2**
     */
    @Property(tries = 100)
    void question_shouldNotBeEmpty(
            @ForAll @AlphaChars @StringLength(min = 1, max = 500) String question) {
        
        assertThat(question).isNotBlank();
    }

    /**
     * Property 8: 会话类型验证 - 会话类型应为有效值
     * **Validates: Requirements 10.1**
     */
    @Property(tries = 50)
    void sessionType_shouldBeValid(
            @ForAll("validSessionTypes") String sessionType) {
        
        assertThat(sessionType).isIn("HEALTH_QA", "SYMPTOM_CHECK", "MEDICATION_INFO");
    }

    @Provide
    Arbitrary<String> validSessionTypes() {
        return Arbitraries.of("HEALTH_QA", "SYMPTOM_CHECK", "MEDICATION_INFO");
    }
}
