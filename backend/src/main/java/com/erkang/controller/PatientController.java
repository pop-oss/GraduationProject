package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.Result;
import com.erkang.domain.dto.PatientProfileDTO;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.entity.FollowupPlan;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.vo.PatientProfileVO;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.FollowupPlanMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 患者控制器
 */
@Slf4j
@Tag(name = "患者管理", description = "患者档案管理")
@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {
    
    private final PatientService patientService;
    private final ConsultationMapper consultationMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final FollowupPlanMapper followupPlanMapper;
    
    @Operation(summary = "获取患者仪表盘数据")
    @GetMapping("/dashboard")
    @RequireRole("PATIENT")
    public Result<Map<String, Object>> getDashboard() {
        try {
            Long userId = UserContext.getUserId();
            
            // 获取患者档案
            PatientProfileVO profile = patientService.getMyProfile();
            
            // 统计待进行问诊数量
            LambdaQueryWrapper<Consultation> upcomingWrapper = new LambdaQueryWrapper<>();
            upcomingWrapper.eq(Consultation::getPatientId, userId);
            upcomingWrapper.in(Consultation::getStatus, "WAITING", "IN_PROGRESS");
            long upcomingConsultations = consultationMapper.selectCount(upcomingWrapper);
            
            // 统计历史问诊总数
            LambdaQueryWrapper<Consultation> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(Consultation::getPatientId, userId);
            long totalConsultations = consultationMapper.selectCount(totalWrapper);
            
            // 统计有效处方数量
            LambdaQueryWrapper<Prescription> prescriptionWrapper = new LambdaQueryWrapper<>();
            prescriptionWrapper.eq(Prescription::getPatientId, userId);
            prescriptionWrapper.eq(Prescription::getStatus, "APPROVED");
            long activePrescriptions = prescriptionMapper.selectCount(prescriptionWrapper);
            
            // 统计待完成随访数量
            LambdaQueryWrapper<FollowupPlan> followupWrapper = new LambdaQueryWrapper<>();
            followupWrapper.eq(FollowupPlan::getPatientId, userId);
            followupWrapper.eq(FollowupPlan::getStatus, "PENDING");
            long pendingFollowups = followupPlanMapper.selectCount(followupWrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("profile", profile);
            data.put("upcomingConsultations", upcomingConsultations);
            data.put("totalConsultations", totalConsultations);
            data.put("activePrescriptions", activePrescriptions);
            data.put("pendingFollowups", pendingFollowups);
            
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取患者仪表盘数据失败", e);
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("profile", null);
            emptyData.put("upcomingConsultations", 0);
            emptyData.put("totalConsultations", 0);
            emptyData.put("activePrescriptions", 0);
            emptyData.put("pendingFollowups", 0);
            return Result.success(emptyData);
        }
    }
    
    @Operation(summary = "获取我的档案")
    @GetMapping("/profile")
    @RequireRole("PATIENT")
    public Result<PatientProfileVO> getMyProfile() {
        return Result.success(patientService.getMyProfile());
    }
    
    @Operation(summary = "更新我的档案")
    @PutMapping("/profile")
    @RequireRole("PATIENT")
    @Auditable(action = "UPDATE_PROFILE", module = "PATIENT")
    public Result<Void> updateProfile(@Valid @RequestBody PatientProfileDTO dto) {
        patientService.updateProfile(dto);
        return Result.success();
    }
    
    @Operation(summary = "获取患者档案（医生/管理员）")
    @GetMapping("/{patientId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST", "ADMIN"})
    public Result<PatientProfileVO> getPatientProfile(@PathVariable Long patientId) {
        return Result.success(patientService.getProfileById(patientId));
    }
}
