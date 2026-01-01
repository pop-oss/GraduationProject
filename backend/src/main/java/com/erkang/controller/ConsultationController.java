package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.dto.CreateConsultationRequest;
import com.erkang.domain.entity.Consultation;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 问诊控制器
 */
@Slf4j
@Tag(name = "问诊管理", description = "问诊流程管理")
@RestController
@RequiredArgsConstructor
public class ConsultationController {
    
    private final ConsultationService consultationService;
    private final ConsultationMapper consultationMapper;
    
    /**
     * 获取问诊列表（患者端）
     */
    @Operation(summary = "获取问诊列表")
    @GetMapping("/api/consultations")
    public Result<Map<String, Object>> getConsultationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String status) {
        try {
            Long userId = UserContext.getUserId();
            Page<Consultation> pageParam = new Page<>(page, pageSize);
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            
            // 患者只能看自己的问诊
            wrapper.eq(Consultation::getPatientId, userId);
            
            if (status != null && !status.isEmpty()) {
                wrapper.eq(Consultation::getStatus, status);
            }
            
            wrapper.orderByDesc(Consultation::getCreatedAt);
            
            Page<Consultation> result = consultationMapper.selectPage(pageParam, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取问诊列表失败", e);
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("records", List.of());
            emptyData.put("total", 0);
            emptyData.put("pages", 0);
            emptyData.put("current", 1);
            return Result.success(emptyData);
        }
    }
    
    /**
     * 获取待接诊列表（医生端）
     */
    @Operation(summary = "获取待接诊列表")
    @GetMapping("/api/consultations/waiting")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Map<String, Object>> getWaitingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Long doctorId = UserContext.getUserId();
            Page<Consultation> pageParam = new Page<>(page, pageSize);
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            
            wrapper.eq(Consultation::getDoctorId, doctorId);
            wrapper.eq(Consultation::getStatus, "WAITING");
            wrapper.orderByAsc(Consultation::getCreatedAt);
            
            Page<Consultation> result = consultationMapper.selectPage(pageParam, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pages", result.getPages());
            data.put("current", result.getCurrent());
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取待接诊列表失败", e);
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("records", List.of());
            emptyData.put("total", 0);
            emptyData.put("pages", 0);
            emptyData.put("current", 1);
            return Result.success(emptyData);
        }
    }
    
    /**
     * 获取今日统计（医生端）
     */
    @Operation(summary = "获取今日统计")
    @GetMapping("/api/consultations/today-stats")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Map<String, Object>> getTodayStats() {
        try {
            Long doctorId = UserContext.getUserId();
            
            // 统计待接诊数量
            LambdaQueryWrapper<Consultation> waitingWrapper = new LambdaQueryWrapper<>();
            waitingWrapper.eq(Consultation::getStatus, "WAITING");
            long waiting = consultationMapper.selectCount(waitingWrapper);
            
            // 统计进行中数量
            LambdaQueryWrapper<Consultation> inProgressWrapper = new LambdaQueryWrapper<>();
            inProgressWrapper.eq(Consultation::getDoctorId, doctorId);
            inProgressWrapper.eq(Consultation::getStatus, "IN_PROGRESS");
            long inProgress = consultationMapper.selectCount(inProgressWrapper);
            
            // 统计已完成数量
            LambdaQueryWrapper<Consultation> finishedWrapper = new LambdaQueryWrapper<>();
            finishedWrapper.eq(Consultation::getDoctorId, doctorId);
            finishedWrapper.eq(Consultation::getStatus, "FINISHED");
            long finished = consultationMapper.selectCount(finishedWrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("waiting", waiting);
            data.put("inProgress", inProgress);
            data.put("finished", finished);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取今日统计失败", e);
            Map<String, Object> data = new HashMap<>();
            data.put("waiting", 0);
            data.put("inProgress", 0);
            data.put("finished", 0);
            return Result.success(data);
        }
    }
    
    /**
     * 创建问诊预约
     */
    @Operation(summary = "创建问诊预约")
    @PostMapping("/api/consultations")
    @RequireRole({"PATIENT"})
    @Auditable(action = "CREATE_CONSULTATION", module = "CONSULTATION")
    public Result<Consultation> createConsultation(@RequestBody CreateConsultationRequest request) {
        Consultation consultation = consultationService.createConsultation(request);
        return Result.success(consultation);
    }
    
    @Operation(summary = "获取问诊详情")
    @GetMapping("/api/consultations/{consultationId}")
    public Result<Consultation> getConsultationDetail(@PathVariable Long consultationId) {
        return Result.success(consultationService.getById(consultationId));
    }
    
    @Operation(summary = "获取问诊详情（旧接口）")
    @GetMapping("/api/consultation/{consultationId}")
    public Result<Consultation> getConsultation(@PathVariable Long consultationId) {
        return Result.success(consultationService.getById(consultationId));
    }
    
    @Operation(summary = "医生接诊")
    @PostMapping("/api/consultation/{consultationId}/start")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "START_CONSULTATION", module = "CONSULTATION")
    public Result<Void> startConsultation(@PathVariable Long consultationId) {
        consultationService.startConsultation(consultationId);
        return Result.success();
    }
    
    @Operation(summary = "结束问诊")
    @PostMapping("/api/consultation/{consultationId}/finish")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "FINISH_CONSULTATION", module = "CONSULTATION")
    public Result<Void> finishConsultation(@PathVariable Long consultationId) {
        consultationService.finishConsultation(consultationId);
        return Result.success();
    }
    
    /**
     * 医生接诊（新接口）
     */
    @Operation(summary = "医生接诊")
    @PutMapping("/api/consultations/{consultationId}/accept")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "ACCEPT_CONSULTATION", module = "CONSULTATION")
    public Result<Void> acceptConsultation(@PathVariable Long consultationId) {
        consultationService.startConsultation(consultationId);
        return Result.success();
    }
    
    @Operation(summary = "取消问诊")
    @PostMapping("/api/consultation/{consultationId}/cancel")
    @Auditable(action = "CANCEL_CONSULTATION", module = "CONSULTATION")
    public Result<Void> cancelConsultation(
            @PathVariable Long consultationId,
            @RequestParam(required = false) String reason) {
        consultationService.cancelConsultation(consultationId, reason);
        return Result.success();
    }
}
