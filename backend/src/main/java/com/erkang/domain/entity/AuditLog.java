package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@Builder
@TableName("audit_log")
public class AuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 操作用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 用户角色 */
    private String userRole;
    
    /** 操作类型 */
    private String action;
    
    /** 模块 */
    private String module;
    
    /** 目标类型 */
    private String targetType;
    
    /** 目标ID */
    private Long targetId;
    
    /** 目标描述 */
    private String targetDesc;
    
    /** 请求方法 */
    private String requestMethod;
    
    /** 请求URL */
    private String requestUrl;
    
    /** 请求参数 */
    private String requestParams;
    
    /** 响应码 */
    private Integer responseCode;
    
    /** IP地址 */
    private String ipAddress;
    
    /** User-Agent */
    private String userAgent;
    
    /** 耗时(毫秒) */
    private Integer durationMs;
    
    /** 备注 */
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
