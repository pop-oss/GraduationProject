package com.erkang.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建问诊请求
 */
@Data
public class CreateConsultationRequest {
    
    /**
     * 科室ID（可选）
     */
    private Long departmentId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 症状描述
     */
    private String symptoms;
    
    /**
     * 附件ID列表
     */
    private List<Long> attachmentIds;
    
    /**
     * 预约时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledAt;
}
