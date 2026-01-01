package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI任务实体
 * _Requirements: 10.5_
 */
@Data
@TableName("ai_task")
public class AITask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskType;            // HEALTH_QA/RECORD_SUMMARY/RISK_CHECK/FOLLOWUP_GEN
    private Long relatedId;             // 关联业务ID
    private String relatedType;         // CONSULTATION/PRESCRIPTION/FOLLOWUP
    private Long userId;                // 请求用户ID
    
    private String requestData;         // 请求数据(脱敏后)
    private String responseData;        // 响应数据
    
    private String status;              // PENDING/PROCESSING/COMPLETED/FAILED
    private String errorMessage;        // 错误信息
    
    private Integer tokensUsed;         // 消耗Token数
    private Integer latencyMs;          // 响应延迟(毫秒)
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
}
