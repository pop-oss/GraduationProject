package com.erkang.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket消息处理器
 * _Requirements: 11.1, 11.2, 11.3_
 */
@Slf4j
@Component
public class ErkangWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService heartbeatScheduler;

    public ErkangWebSocketHandler(WebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        startHeartbeat();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.addSession(userId, session);
            log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");
            
            switch (type) {
                case "PING" -> handlePing(session);
                case "JOIN_CONSULTATION" -> handleJoinConsultation(session, userId, msg);
                case "LEAVE_CONSULTATION" -> handleLeaveConsultation(session, userId, msg);
                default -> log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
            log.info("WebSocket连接关闭: userId={}, status={}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("WebSocket传输错误: userId={}, error={}", userId, exception.getMessage());
    }

    private void handlePing(WebSocketSession session) throws IOException {
        WSMessage pong = WSMessage.of(WSMessageType.PONG, null);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
    }

    private void handleJoinConsultation(WebSocketSession session, Long userId, Map<String, Object> msg) {
        Object consultationIdObj = msg.get("consultationId");
        if (consultationIdObj != null) {
            Long consultationId = Long.valueOf(consultationIdObj.toString());
            sessionManager.joinConsultation(consultationId, userId);
            log.info("用户加入问诊房间: userId={}, consultationId={}", userId, consultationId);
        }
    }

    private void handleLeaveConsultation(WebSocketSession session, Long userId, Map<String, Object> msg) {
        Object consultationIdObj = msg.get("consultationId");
        if (consultationIdObj != null) {
            Long consultationId = Long.valueOf(consultationIdObj.toString());
            sessionManager.leaveConsultation(consultationId, userId);
            log.info("用户离开问诊房间: userId={}, consultationId={}", userId, consultationId);
        }
    }

    /**
     * 心跳检测 - 每30秒检查一次连接状态
     */
    private void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            log.debug("当前在线用户数: {}", sessionManager.getOnlineCount());
        }, 30, 30, TimeUnit.SECONDS);
    }
}
