package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MDT会诊结论实体
 * _Requirements: 7.5_
 */
@Data
@TableName("mdt_conclusion")
public class MDTConclusion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long mdtId;                 // 会诊ID
    
    private String conclusion;          // 会诊结论
    private String treatmentPlan;       // 治疗方案
    private String followupPlan;        // 随访计划
    
    private Long recorderId;            // 记录人ID
    private String confirmedBy;         // 确认人列表(JSON)
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
