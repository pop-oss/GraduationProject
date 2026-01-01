package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.domain.entity.MDTCase;
import com.erkang.domain.entity.MDTConclusion;
import com.erkang.domain.entity.MDTMember;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.MDTService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MDT会诊控制器
 * _Requirements: 7.3, 7.4, 7.5, 7.6_
 */
@RestController
@RequestMapping("/api/mdt")
@RequiredArgsConstructor
public class MDTController {

    private final MDTService mdtService;

    /**
     * 发起会诊
     */
    @PostMapping
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "CREATE_MDT", module = "mdt")
    public Result<MDTCase> createMDT(@RequestBody MDTCase mdtCase) {
        Long userId = UserContext.getUserId();
        mdtCase.setInitiatorId(userId);
        MDTCase created = mdtService.createMDT(mdtCase);
        return Result.success(created);
    }

    /**
     * 邀请专家参会
     */
    @PostMapping("/{mdtId}/invite/{doctorId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "INVITE_MDT_MEMBER", module = "mdt")
    public Result<MDTMember> inviteMember(@PathVariable Long mdtId, @PathVariable Long doctorId) {
        MDTMember member = mdtService.inviteMember(mdtId, doctorId);
        return Result.success(member);
    }

    /**
     * 接受邀请
     */
    @PostMapping("/{mdtId}/accept")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "ACCEPT_MDT_INVITE", module = "mdt")
    public Result<MDTMember> acceptInvite(@PathVariable Long mdtId) {
        Long userId = UserContext.getUserId();
        MDTMember member = mdtService.acceptInvite(mdtId, userId);
        return Result.success(member);
    }

    /**
     * 拒绝邀请
     */
    @PostMapping("/{mdtId}/reject")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "REJECT_MDT_INVITE", module = "mdt")
    public Result<MDTMember> rejectInvite(@PathVariable Long mdtId) {
        Long userId = UserContext.getUserId();
        MDTMember member = mdtService.rejectInvite(mdtId, userId);
        return Result.success(member);
    }

    /**
     * 开始会诊
     */
    @PostMapping("/{mdtId}/start")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "START_MDT", module = "mdt")
    public Result<MDTCase> startMDT(@PathVariable Long mdtId) {
        MDTCase mdtCase = mdtService.startMDT(mdtId);
        return Result.success(mdtCase);
    }

    /**
     * 结束会诊
     */
    @PostMapping("/{mdtId}/end")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "END_MDT", module = "mdt")
    public Result<MDTCase> endMDT(@PathVariable Long mdtId) {
        MDTCase mdtCase = mdtService.endMDT(mdtId);
        return Result.success(mdtCase);
    }

    /**
     * 取消会诊
     */
    @PostMapping("/{mdtId}/cancel")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "CANCEL_MDT", module = "mdt")
    public Result<MDTCase> cancelMDT(@PathVariable Long mdtId) {
        MDTCase mdtCase = mdtService.cancelMDT(mdtId);
        return Result.success(mdtCase);
    }

    /**
     * 归档会诊结论
     */
    @PostMapping("/{mdtId}/conclusion")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    @Auditable(action = "ARCHIVE_MDT_CONCLUSION", module = "mdt")
    public Result<MDTConclusion> archiveConclusion(@PathVariable Long mdtId, @RequestBody MDTConclusion conclusion) {
        Long userId = UserContext.getUserId();
        conclusion.setMdtId(mdtId);
        conclusion.setRecorderId(userId);
        MDTConclusion archived = mdtService.archiveConclusion(conclusion);
        return Result.success(archived);
    }

    /**
     * 查询会诊详情
     */
    @GetMapping("/{mdtId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<MDTCase> getById(@PathVariable Long mdtId) {
        MDTCase mdtCase = mdtService.getById(mdtId);
        return Result.success(mdtCase);
    }

    /**
     * 查询会诊成员
     */
    @GetMapping("/{mdtId}/members")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<List<MDTMember>> getMembers(@PathVariable Long mdtId) {
        List<MDTMember> members = mdtService.getMembers(mdtId);
        return Result.success(members);
    }

    /**
     * 查询会诊结论
     */
    @GetMapping("/{mdtId}/conclusion")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<MDTConclusion> getConclusion(@PathVariable Long mdtId) {
        MDTConclusion conclusion = mdtService.getConclusion(mdtId);
        return Result.success(conclusion);
    }

    /**
     * 查询我发起的会诊
     */
    @GetMapping("/initiated")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<MDTCase>> listInitiated() {
        Long userId = UserContext.getUserId();
        List<MDTCase> list = mdtService.listByInitiatorId(userId);
        return Result.success(list);
    }

    /**
     * 查询我参与的会诊
     */
    @GetMapping("/participated")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<MDTCase>> listParticipated() {
        Long userId = UserContext.getUserId();
        List<MDTCase> list = mdtService.listByDoctorId(userId);
        return Result.success(list);
    }

    /**
     * 查询患者相关会诊
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<List<MDTCase>> listByPatient(@PathVariable Long patientId) {
        List<MDTCase> list = mdtService.listByPatientId(patientId);
        return Result.success(list);
    }

    /**
     * 删除会诊（禁止）
     * _Requirements: 7.6_
     */
    @DeleteMapping("/{mdtId}")
    @RequireRole({"ADMIN"})
    public Result<Void> deleteMDT(@PathVariable Long mdtId) {
        mdtService.deleteMDT(mdtId);
        return Result.success(null);
    }
}
