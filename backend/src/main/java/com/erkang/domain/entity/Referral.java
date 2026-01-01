package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 转诊实体
 * _Requirements: 7.1, 7.2_
 */
@Data
@TableName("referral")
public class Referral {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;
    private Long patientId;
    private Long fromDoctorId;          // 转出医生
    private Long toDoctorId;            // 转入医生
    private Long toHospitalId;          // 转入医院
    private Long toDepartmentId;        // 转入科室
    
    private String referralNo;          // 转诊编号
    private String reason;              // 转诊原因
    private String medicalSummary;      // 病历摘要（必填）
    private String examResults;         // 检查资料（必填）
    private String urgencyLevel;        // 紧急程度: NORMAL/URGENT/EMERGENCY
    
    private String status;              // PENDING/ACCEPTED/REJECTED/COMPLETED/CANCELED
    private String rejectReason;        // 拒绝原因
    
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
