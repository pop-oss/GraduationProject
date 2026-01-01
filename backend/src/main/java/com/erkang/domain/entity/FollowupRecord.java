package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访记录实体
 * _Requirements: 8.1, 8.4, 8.5_
 */
@Data
@TableName("followup_record")
public class FollowupRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long planId;                // 计划ID
    private Long patientId;             // 患者ID
    
    private String recordNo;            // 记录编号
    private LocalDate followupDate;     // 随访日期
    
    private String symptoms;            // 症状描述
    private String answers;             // 问卷答案(JSON)
    
    private Boolean hasRedFlag;         // 是否有红旗征象
    private String redFlagDetail;       // 红旗征象详情
    
    private String doctorComment;       // 医生评语
    private String nextAction;          // 下一步建议
    
    private String status;              // PENDING/SUBMITTED/REVIEWED
    private LocalDateTime submittedAt;  // 提交时间
    private LocalDateTime reviewedAt;   // 审阅时间
    private Long reviewerId;            // 审阅医生ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
