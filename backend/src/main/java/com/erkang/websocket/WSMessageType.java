package com.erkang.websocket;

/**
 * WebSocket消息类型枚举
 * _Requirements: 11.4, 11.5, 11.6_
 */
public enum WSMessageType {
    // 系统消息
    PING,
    PONG,
    
    // 问诊相关
    CONSULTATION_STATUS,      // 问诊状态变更
    CONSULTATION_INVITE,      // 问诊邀请
    
    // 处方相关
    PRESCRIPTION_SUBMITTED,   // 处方已提交审核
    PRESCRIPTION_REVIEWED,    // 处方审核结果
    
    // 转诊/会诊相关
    REFERRAL_INVITE,          // 转诊邀请
    MDT_INVITE,               // 会诊邀请
    
    // 随访相关
    FOLLOWUP_REMINDER,        // 随访提醒
    FOLLOWUP_SUBMITTED,       // 随访记录已提交
    
    // 聊天消息
    CHAT_MESSAGE,             // 聊天消息
    
    // 系统通知
    SYSTEM_NOTIFY             // 系统通知
}
