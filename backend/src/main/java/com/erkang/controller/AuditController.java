package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.entity.AuditLog;
import com.erkang.mapper.AuditLogMapper;
import com.erkang.security.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计日志控制器
 * _Requirements: 9.3, 9.4_
 */
@Slf4j
@Tag(name = "审计日志", description = "审计日志查询")
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogMapper auditLogMapper;

    /**
     * 获取审计日志列表
     */
    @GetMapping
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取审计日志列表")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Page<AuditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(AuditLog::getUsername, username);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(AuditLog::getModule, module);
        }
        if (action != null && !action.isEmpty()) {
            wrapper.eq(AuditLog::getAction, action);
        }
        if (startDate != null) {
            wrapper.ge(AuditLog::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(AuditLog::getCreatedAt, endDate.atTime(LocalTime.MAX));
        }
        
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        
        Page<AuditLog> result = auditLogMapper.selectPage(pageParam, wrapper);
        
        List<Map<String, Object>> records = result.getRecords().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("username", log.getUsername());
            map.put("action", log.getAction());
            map.put("module", log.getModule());
            map.put("targetId", log.getTargetId() != null ? String.valueOf(log.getTargetId()) : null);
            map.put("detail", log.getRemark());
            map.put("ip", log.getIpAddress());
            map.put("userAgent", log.getUserAgent());
            map.put("createdAt", log.getCreatedAt());
            return map;
        }).toList();
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        data.put("current", result.getCurrent());
        
        return Result.success(data);
    }
}
