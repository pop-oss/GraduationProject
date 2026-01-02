package com.erkang.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 问诊列表DTO（包含患者信息）
 */
@Data
public class ConsultationListDTO {
    
    private Long id;
    
    /** 问诊编号 */
    private String consultationNo;
    
    /** 患者ID */
    private Long patientId;
    
    /** 患者姓名 */
    private String patientName;
    
    /** 患者电话 */
    private String patientPhone;
    
    /** 医生ID */
    private Long doctorId;
    
    /** 类型: VIDEO/TEXT/PHONE */
    private String consultationType;
    
    /** 状态: WAITING/IN_PROGRESS/FINISHED/CANCELED */
    private String status;
    
    /** 症状描述 */
    private String symptoms;
    
    /** 预约时间 */
    private LocalDateTime scheduledAt;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 时长(分钟) */
    private Integer duration;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
