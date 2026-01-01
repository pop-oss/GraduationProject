package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.entity.FollowupPlan;
import com.erkang.domain.entity.FollowupRecord;
import com.erkang.mapper.FollowupPlanMapper;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.FollowupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 随访控制器
 * _Requirements: 8.1, 8.4, 8.5_
 */
@RestController
@RequiredArgsConstructor
public class FollowupController {

    private final FollowupService followupService;
    private final FollowupPlanMapper followupPlanMapper;

    // ==================== 患者端随访列表接口 ====================

    /**
     * 获取当前患者的随访计划列表
     */
    @GetMapping("/api/followups")
    @RequireRole({"PATIENT"})
    public Result<Map<String, Object>> getFollowupList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long patientId = UserContext.getUserId();
        
        Page<FollowupPlan> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<FollowupPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupPlan::getPatientId, patientId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(FollowupPlan::getStatus, status);
        }
        
        wrapper.orderByDesc(FollowupPlan::getCreatedAt);
        
        Page<FollowupPlan> result = followupPlanMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        data.put("current", result.getCurrent());
        
        return Result.success(data);
    }

    /**
     * 获取随访计划详情
     */
    @GetMapping("/api/followups/{id}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<FollowupPlan> getFollowupDetail(@PathVariable Long id) {
        FollowupPlan plan = followupService.getPlanById(id);
        return Result.success(plan);
    }

    // ==================== 随访计划 ====================

    /**
     * 创建随访计划
     */
    @PostMapping("/api/followup/plan")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "CREATE_FOLLOWUP_PLAN", module = "followup")
    public Result<FollowupPlan> createPlan(@RequestBody FollowupPlan plan) {
        Long userId = UserContext.getUserId();
        plan.setDoctorId(userId);
        FollowupPlan created = followupService.createPlan(plan);
        return Result.success(created);
    }

    /**
     * 取消随访计划
     */
    @PostMapping("/api/followup/plan/{planId}/cancel")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "CANCEL_FOLLOWUP_PLAN", module = "followup")
    public Result<FollowupPlan> cancelPlan(@PathVariable Long planId) {
        FollowupPlan plan = followupService.cancelPlan(planId);
        return Result.success(plan);
    }

    /**
     * 查询随访计划详情
     */
    @GetMapping("/api/followup/plan/{planId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<FollowupPlan> getPlan(@PathVariable Long planId) {
        FollowupPlan plan = followupService.getPlanById(planId);
        return Result.success(plan);
    }

    /**
     * 查询我的随访计划（患者）
     */
    @GetMapping("/api/followup/plan/my")
    @RequireRole({"PATIENT"})
    public Result<List<FollowupPlan>> listMyPlans() {
        Long userId = UserContext.getUserId();
        List<FollowupPlan> list = followupService.listPlansByPatientId(userId);
        return Result.success(list);
    }

    /**
     * 查询我创建的随访计划（医生）
     */
    @GetMapping("/api/followup/plan/created")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<FollowupPlan>> listCreatedPlans() {
        Long userId = UserContext.getUserId();
        List<FollowupPlan> list = followupService.listPlansByDoctorId(userId);
        return Result.success(list);
    }

    // ==================== 随访记录 ====================

    /**
     * 创建随访记录
     */
    @PostMapping("/api/followup/record")
    @RequireRole({"PATIENT"})
    @Auditable(action = "CREATE_FOLLOWUP_RECORD", module = "followup")
    public Result<FollowupRecord> createRecord(@RequestBody FollowupRecord record) {
        FollowupRecord created = followupService.createRecord(record);
        return Result.success(created);
    }

    /**
     * 提交随访记录
     */
    @PostMapping("/api/followup/record/{recordId}/submit")
    @RequireRole({"PATIENT"})
    @Auditable(action = "SUBMIT_FOLLOWUP_RECORD", module = "followup")
    public Result<FollowupRecord> submitRecord(
            @PathVariable Long recordId,
            @RequestParam(required = false) String symptoms,
            @RequestParam(required = false) String answers) {
        FollowupRecord record = followupService.submitRecord(recordId, symptoms, answers);
        return Result.success(record);
    }

    /**
     * 审阅随访记录
     */
    @PostMapping("/api/followup/record/{recordId}/review")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "REVIEW_FOLLOWUP_RECORD", module = "followup")
    public Result<FollowupRecord> reviewRecord(
            @PathVariable Long recordId,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String nextAction) {
        Long userId = UserContext.getUserId();
        FollowupRecord record = followupService.reviewRecord(recordId, userId, comment, nextAction);
        return Result.success(record);
    }

    /**
     * 查询随访记录详情
     */
    @GetMapping("/api/followup/record/{recordId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<FollowupRecord> getRecord(@PathVariable Long recordId) {
        FollowupRecord record = followupService.getRecordById(recordId);
        return Result.success(record);
    }

    /**
     * 查询计划下的随访记录
     */
    @GetMapping("/api/followup/plan/{planId}/records")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<List<FollowupRecord>> listRecordsByPlan(@PathVariable Long planId) {
        List<FollowupRecord> list = followupService.listRecordsByPlanId(planId);
        return Result.success(list);
    }

    /**
     * 查询待审阅的随访记录（医生）
     */
    @GetMapping("/api/followup/record/pending-review")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<FollowupRecord>> listPendingReview() {
        Long userId = UserContext.getUserId();
        List<FollowupRecord> list = followupService.listPendingReviewRecords(userId);
        return Result.success(list);
    }

    /**
     * 查询有红旗征象的记录（医生）
     */
    @GetMapping("/api/followup/record/red-flags")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<FollowupRecord>> listRedFlags() {
        Long userId = UserContext.getUserId();
        List<FollowupRecord> list = followupService.listRedFlagRecords(userId);
        return Result.success(list);
    }
}
