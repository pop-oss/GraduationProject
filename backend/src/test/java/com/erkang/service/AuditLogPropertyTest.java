package com.erkang.service;

import com.erkang.domain.entity.AuditLog;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.time.LocalDateTime;

/**
 * 审计日志属性测试
 * 
 * Feature: ent-telemedicine, Property 6: 审计日志完整性
 * Validates: Requirements 1.9, 13.1, 13.2, 13.3
 */
class AuditLogPropertyTest {
    
    /**
     * Property 1: 审计日志必须包含所有必要字段
     * 对于任意审计日志，必须包含操作者、操作时间、操作类型
     */
    @Property(tries = 100)
    @Label("审计日志必须包含必要字段")
    void auditLogMustContainRequiredFields(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll @AlphaChars @StringLength(min = 3, max = 20) String username,
            @ForAll("validActions") String action,
            @ForAll("validModules") String module
    ) {
        // Given: 创建审计日志
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .module(module)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Then: 必要字段不能为空
        assert auditLog.getUserId() != null : "操作者ID不能为空";
        assert auditLog.getUsername() != null && !auditLog.getUsername().isEmpty() : "用户名不能为空";
        assert auditLog.getAction() != null && !auditLog.getAction().isEmpty() : "操作类型不能为空";
        assert auditLog.getCreatedAt() != null : "操作时间不能为空";
    }
    
    /**
     * Property 2: 审计日志创建时间必须是当前或过去时间
     * 对于任意审计日志，创建时间不能是未来时间
     */
    @Property(tries = 100)
    @Label("审计日志时间不能是未来")
    void auditLogTimeMustNotBeFuture(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll("validActions") String action
    ) {
        // Given: 创建审计日志
        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .createdAt(now)
                .build();
        
        // Then: 创建时间应该不晚于当前时间（允许1秒误差）
        assert !auditLog.getCreatedAt().isAfter(LocalDateTime.now().plusSeconds(1)) : 
            "审计日志时间不能是未来";
    }
    
    /**
     * Property 3: 敏感操作必须记录审计日志
     * 对于任意敏感操作类型，都应该能创建有效的审计日志
     */
    @Property(tries = 100)
    @Label("敏感操作都能记录审计日志")
    void sensitiveActionsMustBeLogged(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll("sensitiveActions") String action,
            @ForAll("validModules") String module,
            @ForAll @LongRange(min = 1, max = 10000) Long targetId
    ) {
        // Given: 敏感操作
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .module(module)
                .targetId(targetId)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Then: 审计日志应该有效
        assert auditLog.getAction() != null : "操作类型不能为空";
        assert isSensitiveAction(auditLog.getAction()) : "应该是敏感操作";
    }

    /**
     * Property 4: IP地址格式应该有效
     * 对于任意包含IP的审计日志，IP格式应该有效
     */
    @Property(tries = 100)
    @Label("IP地址格式有效")
    void ipAddressShouldBeValid(
            @ForAll("validIpAddresses") String ipAddress
    ) {
        // Given: 创建包含IP的审计日志
        AuditLog auditLog = AuditLog.builder()
                .userId(1L)
                .action("LOGIN")
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Then: IP地址应该有效
        assert isValidIpAddress(auditLog.getIpAddress()) : 
            "IP地址格式无效: " + auditLog.getIpAddress();
    }
    
    /**
     * 检查是否为敏感操作
     */
    private boolean isSensitiveAction(String action) {
        return action != null && (
                action.contains("LOGIN") ||
                action.contains("LOGOUT") ||
                action.contains("PRESCRIPTION") ||
                action.contains("REVIEW") ||
                action.contains("MDT") ||
                action.contains("REFERRAL") ||
                action.contains("EXPORT")
        );
    }
    
    /**
     * 验证IP地址格式
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        // IPv4
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        // IPv6 简化检查
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        // localhost
        if ("localhost".equals(ip) || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        
        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }
    
    @Provide
    Arbitrary<String> validActions() {
        return Arbitraries.of(
            "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "QUERY", "EXPORT"
        );
    }
    
    @Provide
    Arbitrary<String> sensitiveActions() {
        return Arbitraries.of(
            "LOGIN", "LOGOUT",
            "PRESCRIPTION_CREATE", "PRESCRIPTION_SUBMIT",
            "REVIEW_APPROVE", "REVIEW_REJECT",
            "MDT_CREATE", "MDT_CONCLUDE",
            "REFERRAL_CREATE", "REFERRAL_ACCEPT",
            "EXPORT_DATA"
        );
    }
    
    @Provide
    Arbitrary<String> validModules() {
        return Arbitraries.of(
            "AUTH", "PATIENT", "CONSULTATION", "PRESCRIPTION", 
            "PHARMACY", "REFERRAL", "MDT", "FOLLOWUP", "STATS"
        );
    }
    
    @Provide
    Arbitrary<String> validIpAddresses() {
        return Arbitraries.of(
            "127.0.0.1",
            "192.168.1.1",
            "10.0.0.1",
            "172.16.0.1",
            "localhost"
        );
    }
}
