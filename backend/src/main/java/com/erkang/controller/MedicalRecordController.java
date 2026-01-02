package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.domain.dto.CreateMedicalRecordRequest;
import com.erkang.domain.entity.MedicalAttachment;
import com.erkang.domain.entity.MedicalRecord;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病历控制器
 * _Requirements: 5.1, 5.3, 5.5_
 */
@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    /**
     * 创建或更新病历
     */
    @PostMapping
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalRecord> createOrUpdate(@RequestBody CreateMedicalRecordRequest request) {
        Long doctorId = UserContext.getUserId();
        MedicalRecord record = recordService.createOrUpdateRecord(request, doctorId);
        return Result.success(record);
    }

    /**
     * 更新病历
     */
    @PutMapping("/{id}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalRecord> update(@PathVariable Long id,
                                        @RequestBody MedicalRecord updates) {
        MedicalRecord record = recordService.updateRecord(id, updates);
        return Result.success(record);
    }

    /**
     * 提交病历
     */
    @PostMapping("/{id}/submit")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalRecord> submit(@PathVariable Long id) {
        MedicalRecord record = recordService.submitRecord(id);
        return Result.success(record);
    }

    /**
     * 查询病历详情
     */
    @GetMapping("/{id}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalRecord> getById(@PathVariable Long id) {
        MedicalRecord record = recordService.getRecord(id);
        return Result.success(record);
    }

    /**
     * 根据问诊ID查询病历
     */
    @GetMapping("/consultation/{consultationId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalRecord> getByConsultation(@PathVariable Long consultationId) {
        MedicalRecord record = recordService.getByConsultationId(consultationId);
        return Result.success(record);
    }

    /**
     * 查询患者病历列表
     */
    @GetMapping("/patient/{patientId}")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<MedicalRecord>> listByPatient(@PathVariable Long patientId) {
        List<MedicalRecord> records = recordService.listByPatientId(patientId);
        return Result.success(records);
    }

    /**
     * 添加附件
     */
    @PostMapping("/{id}/attachments")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<MedicalAttachment> addAttachment(@PathVariable Long id,
                                                   @RequestBody MedicalAttachment attachment) {
        attachment.setRecordId(id);
        attachment.setUploaderId(UserContext.getUserId());
        MedicalAttachment saved = recordService.addAttachment(attachment);
        return Result.success(saved);
    }

    /**
     * 查询病历附件
     */
    @GetMapping("/{id}/attachments")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<List<MedicalAttachment>> listAttachments(@PathVariable Long id) {
        List<MedicalAttachment> attachments = recordService.listAttachments(id);
        return Result.success(attachments);
    }
}
