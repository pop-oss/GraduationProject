package com.erkang.service;

import com.erkang.domain.entity.AuditLog;
import com.erkang.mapper.AuditLogMapper;
import com.erkang.security.LoginUser;
import com.erkang.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogMapper auditLogMapper;
    
    /**
     * 记录审计日志（异步）
     */
    @Async
    public void log(String action, String module, String targetType, Long targetId, 
                    String targetDesc, String requestMethod, String requestUrl,
                    String requestParams, Integer responseCode, String ipAddress,
                    String userAgent, Integer durationMs, String remark) {
        try {
            LoginUser user = UserContext.getUser();
            
            AuditLog auditLog = AuditLog.builder()
                    .userId(user != null ? user.getUserId() : null)
                    .username(user != null ? user.getUsername() : null)
                    .userRole(user != null && user.getRoles() != null ? 
                            String.join(",", user.getRoles()) : null)
                    .action(action)
                    .module(module)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetDesc(targetDesc)
                    .requestMethod(requestMethod)
                    .requestUrl(requestUrl)
                    .requestParams(truncate(requestParams, 2000))
                    .responseCode(responseCode)
                    .ipAddress(ipAddress)
                    .userAgent(truncate(userAgent, 255))
                    .durationMs(durationMs)
                    .remark(remark)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 简化版记录审计日志
     */
    public void log(String action, String module, String targetType, Long targetId, String remark) {
        log(action, module, targetType, targetId, null, null, null, null, null, null, null, null, remark);
    }
    
    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
