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
    private Long pharmacistId;
    
    private String result;              // APPROVED/REJECTED
    private String riskLevel;           // LOW/MEDIUM/HIGH
    private String riskDescription;     // 风险描述
    private String rejectReason;        // 驳回原因
    private String suggestion;          // 建议
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
