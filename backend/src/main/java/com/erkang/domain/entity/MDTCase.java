package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MDT会诊实体
 * _Requirements: 7.3, 7.4, 7.5_
 */
@Data
@TableName("mdt_case")
public class MDTCase {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long consultationId;        // 关联问诊ID
    private Long patientId;             // 患者ID
    private Long initiatorId;           // 发起人ID
    
    private String mdtNo;               // 会诊编号
    private String title;               // 会诊主题
    private String clinicalSummary;     // 病历摘要
    private String discussionPoints;    // 讨论要点
    
    private LocalDateTime scheduledTime;    // 计划时间
    private LocalDateTime actualStartTime;  // 实际开始时间
    private LocalDateTime actualEndTime;    // 实际结束时间
    
    private String rtcRoomId;           // RTC房间ID
    
    private String status;              // PENDING/IN_PROGRESS/COMPLETED/CANCELED
    private LocalDateTime statusUpdatedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
