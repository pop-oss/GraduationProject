package com.erkang.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * 消息分发服务
 * _Requirements: 11.4, 11.5, 11.6_
 */
@Slf4j
@Service
public class MessageDispatcher {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public MessageDispatcher(WebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(Long userId, WSMessageType type, Object data) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, WSMessage.of(type, data));
        }
    }

    /**
     * 发送消息给问诊房间内所有用户
     */
    public void sendToConsultation(Long consultationId, WSMessageType type, Object data) {
        Set<WebSocketSession> sessions = sessionManager.getConsultationSessions(consultationId);
        WSMessage message = WSMessage.of(type, data);
        sessions.forEach(session -> sendMessage(session, message));
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(WSMessageType type, Object data) {
        // 实现广播逻辑（如需要）
    }

    /**
     * 推送问诊状态变更通知
     */
    public void pushConsultationStatus(Long consultationId, Long patientId, Long doctorId, 
                                       String status, String message) {
        Map<String, Object> data = Map.of(
            "consultationId", consultationId,
            "status", status,
            "message", message
        );
        
        // 通知患者
        sendToUser(patientId, WSMessageType.CONSULTATION_STATUS, data);
        // 通知医生
        sendToUser(doctorId, WSMessageType.CONSULTATION_STATUS, data);
    }

    /**
     * 推送处方提交通知（通知药师）
     */
    public void pushPrescriptionSubmitted(Long pharmacistId, Long prescriptionId, 
                                          String prescriptionNo) {
        Map<String, Object> data = Map.of(
            "prescriptionId", prescriptionId,
            "prescriptionNo", prescriptionNo,
            "message", "有新处方待审核"
        );
        sendToUser(pharmacistId, WSMessageType.PRESCRIPTION_SUBMITTED, data);
    }

    /**
     * 推送处方审核结果通知
     */
    public void pushPrescriptionReviewed(Long doctorId, Long patientId, Long prescriptionId,
                                         String status, String message) {
        Map<String, Object> data = Map.of(
            "prescriptionId", prescriptionId,
            "status", status,
            "message", message
        );
        sendToUser(doctorId, WSMessageType.PRESCRIPTION_REVIEWED, data);
        sendToUser(patientId, WSMessageType.PRESCRIPTION_REVIEWED, data);
    }

    /**
     * 推送转诊邀请通知
     */
    public void pushReferralInvite(Long toDoctorId, Long referralId, String patientName,
                                   String fromDoctorName, String reason) {
        Map<String, Object> data = Map.of(
            "referralId", referralId,
            "patientName", patientName,
            "fromDoctorName", fromDoctorName,
            "reason", reason,
            "message", "您收到一个转诊请求"
        );
        sendToUser(toDoctorId, WSMessageType.REFERRAL_INVITE, data);
    }

    /**
     * 推送MDT会诊邀请通知
     */
    public void pushMDTInvite(Long doctorId, Long mdtCaseId, String patientName,
                              String initiatorName, String topic) {
        Map<String, Object> data = Map.of(
            "mdtCaseId", mdtCaseId,
            "patientName", patientName,
            "initiatorName", initiatorName,
            "topic", topic,
            "message", "您被邀请参加MDT会诊"
        );
        sendToUser(doctorId, WSMessageType.MDT_INVITE, data);
    }

    /**
     * 推送随访提醒
     */
    public void pushFollowupReminder(Long patientId, Long followupPlanId, String doctorName,
                                     String content) {
        Map<String, Object> data = Map.of(
            "followupPlanId", followupPlanId,
            "doctorName", doctorName,
            "content", content,
            "message", "您有一个随访任务待完成"
        );
        sendToUser(patientId, WSMessageType.FOLLOWUP_REMINDER, data);
    }

    /**
     * 推送随访记录提交通知（通知医生）
     */
    public void pushFollowupSubmitted(Long doctorId, Long followupRecordId, String patientName) {
        Map<String, Object> data = Map.of(
            "followupRecordId", followupRecordId,
            "patientName", patientName,
            "message", "患者已提交随访记录"
        );
        sendToUser(doctorId, WSMessageType.FOLLOWUP_SUBMITTED, data);
    }

    /**
     * 推送聊天消息
     */
    public void pushChatMessage(Long consultationId, Long senderId, String senderName,
                                String content, String contentType) {
        Map<String, Object> data = Map.of(
            "consultationId", consultationId,
            "senderId", senderId,
            "senderName", senderName,
            "content", content,
            "contentType", contentType
        );
        sendToConsultation(consultationId, WSMessageType.CHAT_MESSAGE, data);
    }

    private void sendMessage(WebSocketSession session, WSMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage());
        }
    }
}
