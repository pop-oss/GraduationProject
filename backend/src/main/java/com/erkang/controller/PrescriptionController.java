package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.Result;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.entity.PrescriptionItem;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处方控制器
 * _Requirements: 6.1, 6.2_
 */
@Tag(name = "处方管理", description = "处方开具与查询")
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionMapper prescriptionMapper;

    /**
     * 获取当前用户的处方列表（患者端）
     */
    @Operation(summary = "获取处方列表")
    @GetMapping
    @RequireRole({"PATIENT"})
    public Result<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long patientId = UserContext.getUserId();
        
        Page<Prescription> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getPatientId, patientId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Prescription::getStatus, status);
        }
        
        wrapper.orderByDesc(Prescription::getCreatedAt);
        
        Page<Prescription> result = prescriptionMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        data.put("current", result.getCurrent());
        
        return Result.success(data);
    }

    @Operation(summary = "创建处方")
    @PostMapping
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Prescription> create(@RequestParam Long consultationId,
                                       @RequestParam Long patientId) {
        Long doctorId = UserContext.getUserId();
        Prescription prescription = prescriptionService.createPrescription(consultationId, patientId, doctorId);
        return Result.success(prescription);
    }

    /**
     * 更新处方
     */
    @PutMapping("/{id}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Prescription> update(@PathVariable Long id,
                                       @RequestBody Prescription updates) {
        Prescription prescription = prescriptionService.updatePrescription(id, updates);
        return Result.success(prescription);
    }

    /**
     * 添加处方明细
     */
    @PostMapping("/{id}/items")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<PrescriptionItem> addItem(@PathVariable Long id,
                                            @RequestBody PrescriptionItem item) {
        PrescriptionItem saved = prescriptionService.addItem(id, item);
        return Result.success(saved);
    }

    /**
     * 删除处方明细
     */
    @DeleteMapping("/{id}/items/{itemId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Void> deleteItem(@PathVariable Long id,
                                   @PathVariable Long itemId) {
        prescriptionService.deleteItem(id, itemId);
        return Result.success(null);
    }

    @Operation(summary = "提交处方审核")
    @PostMapping("/{id}/submit")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Prescription> submit(@PathVariable Long id) {
        Prescription prescription = prescriptionService.submitForReview(id);
        return Result.success(prescription);
    }

    @Operation(summary = "查询处方详情")
    @GetMapping("/{id}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<Prescription> getById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        return Result.success(prescription);
    }

    /**
     * 查询处方明细
     */
    @GetMapping("/{id}/items")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<List<PrescriptionItem>> listItems(@PathVariable Long id) {
        List<PrescriptionItem> items = prescriptionService.listItems(id);
        return Result.success(items);
    }

    /**
     * 根据问诊ID查询处方
     */
    @GetMapping("/consultation/{consultationId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<Prescription> getByConsultation(@PathVariable Long consultationId) {
        Prescription prescription = prescriptionService.getByConsultationId(consultationId);
        return Result.success(prescription);
    }

    /**
     * 查询患者处方列表
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<Prescription>> listByPatient(@PathVariable Long patientId) {
        List<Prescription> prescriptions = prescriptionService.listByPatientId(patientId);
        return Result.success(prescriptions);
    }

    @Operation(summary = "查询待审核处方列表")
    @GetMapping("/pending-review")
    @RequireRole({"PHARMACIST"})
    public Result<List<Prescription>> listPendingReview() {
        List<Prescription> prescriptions = prescriptionService.listPendingReview();
        return Result.success(prescriptions);
    }
}
