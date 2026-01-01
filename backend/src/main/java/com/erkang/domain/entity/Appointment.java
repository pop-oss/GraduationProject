package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体
 */
@Data
@TableName("appointment")
public class Appointment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 患者ID */
    private Long patientId;
    
    /** 医生ID */
    private Long doctorId;
    
    /** 预约日期 */
    private LocalDate appointmentDate;
    
    /** 时段 */
    private String timeSlot;
    
    /** 主诉/问诊原因 */
    private String chiefComplaint;
    
    /** 状态: PENDING/CONFIRMED/CANCELED */
    private String status;
    
    /** 状态更新时间 */
    private LocalDateTime statusUpdatedAt;
    
    /** 取消原因 */
    private String cancelReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
