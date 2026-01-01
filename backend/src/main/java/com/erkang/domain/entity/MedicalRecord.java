package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病历实体
 * _Requirements: 5.1, 5.3_
 */
@Data
@TableName("medical_record")
public class MedicalRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;
    private Long patientId;
    private Long doctorId;
    private String recordNo;
    
    private String chiefComplaint;      // 主诉
    private String presentIllness;      // 现病史
    private String pastHistory;         // 既往史
    private String allergyHistory;      // 过敏史
    private String physicalExam;        // 体格检查
    private String auxiliaryExam;       // 辅助检查
    private String diagnosis;           // 初步诊断
    private String treatmentPlan;       // 处理建议
    private String followupAdvice;      // 随访建议
    
    private String aiSummary;           // AI生成摘要
    private Integer aiConfirmed;        // AI摘要是否已确认
    
    private String status;              // DRAFT/SUBMITTED
    private LocalDateTime submittedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
