package com.erkang.domain.dto;

import lombok.Data;

/**
 * 创建转诊请求DTO
 * _Requirements: 7.1_
 */
@Data
public class CreateReferralRequest {
    private String consultationId;  // 问诊编号或ID
    private Long toDoctorId;        // 转入医生ID
    private Long toDepartmentId;    // 转入科室ID
    private String summary;         // 病历摘要
    private String description;     // 转诊原因
}
