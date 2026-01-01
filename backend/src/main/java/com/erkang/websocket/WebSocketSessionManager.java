package com.erkang.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket会话管理器
 * _Requirements: 11.1, 11.2_
 */
@Component
public class WebSocketSessionManager {

    // userId -> session
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // consultationId -> Set<userId>
    private final Map<Long, Set<Long>> consultationUsers = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
    }

    public void removeSession(Long userId) {
        userSessions.remove(userId);
        // 从所有问诊房间移除
        consultationUsers.values().forEach(users -> users.remove(userId));
    }

    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    public void joinConsultation(Long consultationId, Long userId) {
        consultationUsers.computeIfAbsent(consultationId, k -> ConcurrentHashMap.newKeySet())
                        .add(userId);
    }

    public void leaveConsultation(Long consultationId, Long userId) {
        Set<Long> users = consultationUsers.get(consultationId);
        if (users != null) {
            users.remove(userId);
        }
    }

    public Set<WebSocketSession> getConsultationSessions(Long consultationId) {
        Set<Long> users = consultationUsers.get(consultationId);
        if (users == null) {
            return Set.of();
        }
        return users.stream()
                   .map(userSessions::get)
                   .filter(s -> s != null && s.isOpen())
                   .collect(Collectors.toSet());
    }

    public int getOnlineCount() {
        return (int) userSessions.values().stream()
                                 .filter(WebSocketSession::isOpen)
                                 .count();
    }
}
