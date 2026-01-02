package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方实体
 * _Requirements: 6.1, 6.2_
 */
@Data
@TableName("prescription")
public class Prescription {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;
    private Long patientId;
    private Long doctorId;
    private String prescriptionNo;
    
    private String diagnosis;           // 诊断
    private String notes;               // 备注
    
    private String status;              // DRAFT/PENDING_REVIEW/APPROVED/REJECTED/DISPENSED
    private LocalDateTime statusUpdatedAt;  // 状态更新时间
    private LocalDateTime submittedAt;      // 提交时间
    private LocalDateTime approvedAt;       // 审核通过时间
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
