package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.domain.entity.Referral;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 转诊控制器
 * _Requirements: 7.1, 7.2, 7.6_
 */
@Tag(name = "转诊管理", description = "转诊申请与处理")
@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;

    @Operation(summary = "发起转诊")
    @PostMapping
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Referral> create(@RequestBody Referral referral) {
        Long doctorId = UserContext.getUserId();
        referral.setFromDoctorId(doctorId);
        Referral created = referralService.createReferral(referral);
        return Result.success(created);
    }

    @Operation(summary = "接受转诊")
    @PostMapping("/{id}/accept")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Referral> accept(@PathVariable Long id) {
        Referral referral = referralService.accept(id);
        return Result.success(referral);
    }

    @Operation(summary = "拒绝转诊")
    @PostMapping("/{id}/reject")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Referral> reject(@PathVariable Long id,
                                   @RequestParam String reason) {
        Referral referral = referralService.reject(id, reason);
        return Result.success(referral);
    }

    @Operation(summary = "完成转诊")
    @PostMapping("/{id}/complete")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Referral> complete(@PathVariable Long id) {
        Referral referral = referralService.complete(id);
        return Result.success(referral);
    }

    @Operation(summary = "查询转诊详情")
    @GetMapping("/{id}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Referral> getById(@PathVariable Long id) {
        Referral referral = referralService.getById(id);
        return Result.success(referral);
    }

    @Operation(summary = "查询我发起的转诊")
    @GetMapping("/from-me")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<Referral>> listFromMe() {
        Long doctorId = UserContext.getUserId();
        List<Referral> referrals = referralService.listByFromDoctorId(doctorId);
        return Result.success(referrals);
    }

    @Operation(summary = "查询转入我的转诊")
    @GetMapping("/to-me")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<Referral>> listToMe() {
        Long doctorId = UserContext.getUserId();
        List<Referral> referrals = referralService.listByToDoctorId(doctorId);
        return Result.success(referrals);
    }

    @Operation(summary = "查询患者转诊记录")
    @GetMapping("/patient/{patientId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<Referral>> listByPatient(@PathVariable Long patientId) {
        List<Referral> referrals = referralService.listByPatientId(patientId);
        return Result.success(referrals);
    }
}
