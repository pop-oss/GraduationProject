package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问诊实体
 */
@Data
@TableName("consultation")
public class Consultation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联预约ID */
    private Long appointmentId;
    
    /** 患者ID */
    private Long patientId;
    
    /** 医生ID */
    private Long doctorId;
    
    /** 问诊编号 */
    private String consultationNo;
    
    /** 类型: VIDEO/TEXT/PHONE */
    private String consultationType;
    
    /** 状态: WAITING/IN_PROGRESS/FINISHED/CANCELED */
    private String status;
    
    /** 状态更新时间 */
    private LocalDateTime statusUpdatedAt;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 时长(分钟) */
    private Integer duration;
    
    /** RTC房间ID */
    private String rtcRoomId;
    
    /** 症状描述 */
    private String symptoms;
    
    /** 预约时间 */
    private LocalDateTime scheduledAt;
    
    /** 是否录制 */
    private Integer isRecorded;
    
    /** 录制授权 */
    private Integer recordConsent;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
