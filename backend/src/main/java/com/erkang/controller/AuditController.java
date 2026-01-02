package com.erkang.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.dto.AuditLogExportDTO;
import com.erkang.domain.entity.AuditLog;
import com.erkang.domain.entity.User;
import com.erkang.mapper.AuditLogMapper;
import com.erkang.mapper.UserMapper;
import com.erkang.security.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审计日志控制器
 * _Requirements: 9.3, 9.4_
 */

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
    private final UserMapper userMapper;
    
    // 模块中文映射
    private static final Map<String, String> MODULE_LABELS = Map.ofEntries(
            Map.entry("AUTH", "认证"),
            Map.entry("USER", "用户"),
            Map.entry("CONSULTATION", "问诊"),
            Map.entry("PRESCRIPTION", "处方"),
            Map.entry("REFERRAL", "转诊"),
            Map.entry("MDT", "会诊"),
            Map.entry("REVIEW", "审方"),
            Map.entry("auth", "认证"),
            Map.entry("user", "用户"),
            Map.entry("consultation", "问诊"),
            Map.entry("prescription", "处方"),
            Map.entry("referral", "转诊"),
            Map.entry("mdt", "会诊"),
            Map.entry("review", "审方"),
            Map.entry("stats", "统计"),
            Map.entry("medical_record", "病历"),
            Map.entry("followup", "随访")
    );
    
    // 操作中文映射
    private static final Map<String, String> ACTION_LABELS = Map.ofEntries(
            Map.entry("CREATE", "创建"),
            Map.entry("UPDATE", "更新"),
            Map.entry("DELETE", "删除"),
            Map.entry("VIEW", "查看"),
            Map.entry("LOGIN", "登录"),
            Map.entry("LOGOUT", "登出"),
            Map.entry("EXPORT", "导出"),
            Map.entry("VIEW_STATS", "查看统计"),
            Map.entry("VIEW_RECORD", "查看病历"),
            Map.entry("REVIEW_PRESCRIPTION", "审核处方"),
            Map.entry("CREATE_PRESCRIPTION", "创建处方"),
            Map.entry("EXPORT_STATS", "导出统计"),
            Map.entry("CREATE_MDT", "创建会诊"),
            Map.entry("CREATE_REFERRAL", "创建转诊"),
            Map.entry("UPDATE_REFERRAL", "更新转诊"),
            Map.entry("CREATE_CONSULTATION", "创建问诊"),
            Map.entry("UPDATE_CONSULTATION", "更新问诊"),
            Map.entry("CREATE_FOLLOWUP", "创建随访"),
            Map.entry("START_CONSULTATION", "开始问诊"),
            Map.entry("ACCEPT_CONSULTATION", "接受问诊"),
            Map.entry("REJECT_PRESCRIPTION", "驳回处方")
    );

    /**
     * 获取审计日志列表
     */
    @GetMapping
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取审计日志列表")
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Page<AuditLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        
        // 按真实姓名模糊筛选：先查询匹配的用户ID
        Set<Long> matchedUserIds = null;
        if (realName != null && !realName.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getRealName, realName);
            List<User> matchedUsers = userMapper.selectList(userWrapper);
            matchedUserIds = matchedUsers.stream().map(User::getId).collect(Collectors.toSet());
            if (matchedUserIds.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("records", List.of());
                data.put("total", 0);
                data.put("pages", 0);
                data.put("current", page);
                return Result.success(data);
            }
            wrapper.in(AuditLog::getUserId, matchedUserIds);
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
        
        Set<Long> userIds = result.getRecords().stream()
                .map(AuditLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        
        final Map<Long, User> finalUserMap = userMap;
        List<Map<String, Object>> records = result.getRecords().stream().map(log -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", log.getId());
            map.put("userId", log.getUserId());
            map.put("username", log.getUsername());
            
            User user = log.getUserId() != null ? finalUserMap.get(log.getUserId()) : null;
            map.put("realName", user != null ? user.getRealName() : null);
            map.put("phone", user != null ? user.getPhone() : null);
            
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

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    @RequireRole({"ADMIN"})
    @Operation(summary = "导出审计日志")
    public void exportLogs(
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpServletResponse response) throws IOException {
        
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        
        if (realName != null && !realName.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getRealName, realName);
            List<User> matchedUsers = userMapper.selectList(userWrapper);
            Set<Long> matchedUserIds = matchedUsers.stream().map(User::getId).collect(Collectors.toSet());
            if (!matchedUserIds.isEmpty()) {
                wrapper.in(AuditLog::getUserId, matchedUserIds);
            } else {
                // 没有匹配的用户，返回空Excel
                setExcelResponse(response);
                EasyExcel.write(response.getOutputStream(), AuditLogExportDTO.class)
                        .sheet("审计日志")
                        .doWrite(Collections.emptyList());
                return;
            }
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
        List<AuditLog> logs = auditLogMapper.selectList(wrapper);
        
        Set<Long> userIds = logs.stream()
                .map(AuditLog::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final Map<Long, User> finalUserMap = userMap;
        
        List<AuditLogExportDTO> exportData = logs.stream().map(log -> {
            AuditLogExportDTO dto = new AuditLogExportDTO();
            User user = log.getUserId() != null ? finalUserMap.get(log.getUserId()) : null;
            
            dto.setCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
            dto.setUsername(log.getUsername());
            dto.setRealName(user != null ? user.getRealName() : "");
            dto.setPhone(user != null ? user.getPhone() : "");
            dto.setModule(MODULE_LABELS.getOrDefault(log.getModule(), log.getModule()));
            dto.setAction(ACTION_LABELS.getOrDefault(log.getAction(), log.getAction()));
            dto.setTargetId(log.getTargetId() != null ? String.valueOf(log.getTargetId()) : "");
            dto.setDetail(log.getRemark());
            dto.setIp(log.getIpAddress());
            return dto;
        }).toList();
        
        setExcelResponse(response);
        EasyExcel.write(response.getOutputStream(), AuditLogExportDTO.class)
                .sheet("审计日志")
                .doWrite(exportData);
    }
    
    private void setExcelResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("审计日志", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
    }
}
