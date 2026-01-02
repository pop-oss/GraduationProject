package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审方记录实体
 * _Requirements: 6.3, 6.4, 6.5_
 */
@Data
@TableName("pharmacy_review")
public class PharmacyReview {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long prescriptionId;
    
    @TableField("reviewer_id")
    private Long pharmacistId;
    
    @TableField("review_status")
    private String result;              // APPROVED/REJECTED
    private String riskLevel;           // LOW/MEDIUM/HIGH
    private String highRiskItems;       // 高风险项
    private String mediumRiskItems;     // 中风险项
    private String lowRiskItems;        // 低风险项
    private String rejectReason;        // 驳回原因
    private String suggestion;          // 建议
    private String aiRiskHint;          // AI风险提示
    private LocalDateTime reviewedAt;   // 审核时间
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
