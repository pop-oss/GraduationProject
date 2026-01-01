package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话消息实体
 * _Requirements: 10.1, 10.5_
 */
@Data
@TableName("ai_chat_message")
public class AIChatMessage {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sessionId;             // 会话ID
    private String role;                // USER/ASSISTANT/SYSTEM
    private String content;             // 消息内容
    private Integer tokens;             // Token数
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
