package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话会话实体
 * _Requirements: 10.1, 10.5_
 */
@Data
@TableName("ai_chat_session")
public class AIChatSession {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;                // 用户ID
    private String sessionType;         // HEALTH_QA
    private String title;               // 会话标题
    private String status;              // ACTIVE/CLOSED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
