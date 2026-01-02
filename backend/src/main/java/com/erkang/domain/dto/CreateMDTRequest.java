package com.erkang.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建MDT会诊请求DTO
 */
@Data
public class CreateMDTRequest {
    private String consultationId;      // 关联问诊ID（可能是编号或ID）
    private String title;               // 会诊主题
    private String description;         // 病情描述（映射到clinicalSummary）
    private List<Long> memberIds;       // 邀请的专家ID列表
    private String scheduledAt;         // 计划时间
}
