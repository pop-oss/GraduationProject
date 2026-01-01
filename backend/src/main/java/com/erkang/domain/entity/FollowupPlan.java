package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 随访计划实体
 * _Requirements: 8.1_
 */
@Data
@TableName("followup_plan")
public class FollowupPlan {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;        // 问诊ID
    private Long patientId;             // 患者ID
    private Long doctorId;              // 医生ID
    
    private String planNo;              // 计划编号
    private String diagnosis;           // 诊断
    private String followupType;        // REGULAR/CHRONIC
    
    private Integer intervalDays;       // 随访间隔(天)
    private Integer totalTimes;         // 总次数
    private Integer completedTimes;     // 已完成次数
    
    private LocalDate nextFollowupDate; // 下次随访日期
    private String questionList;        // 随访问题清单(JSON)
    private String redFlags;            // 红旗征象(JSON)
    
    private String status;              // ACTIVE/COMPLETED/CANCELED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
