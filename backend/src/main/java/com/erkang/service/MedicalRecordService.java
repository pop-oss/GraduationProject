package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.entity.MedicalAttachment;
import com.erkang.domain.entity.MedicalRecord;
import com.erkang.mapper.MedicalAttachmentMapper;
import com.erkang.mapper.MedicalRecordMapper;
import com.erkang.security.Auditable;
import com.erkang.security.DataScope;
import com.erkang.security.DataScopeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 病历服务
 * _Requirements: 5.1, 5.3, 5.5, 5.6_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordMapper recordMapper;
    private final MedicalAttachmentMapper attachmentMapper;

    /**
     * 创建病历
     */
    @Transactional
    @Auditable(action = "CREATE_MEDICAL_RECORD", module = "medical_record")
    public MedicalRecord createRecord(Long consultationId, Long patientId, Long doctorId) {
        // 检查是否已存在病历
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getConsultationId, consultationId);
        if (recordMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该问诊已存在病历");
        }

        MedicalRecord record = new MedicalRecord();
        record.setConsultationId(consultationId);
        record.setPatientId(patientId);
        record.setDoctorId(doctorId);
        record.setRecordNo(generateRecordNo());
        record.setStatus("DRAFT");
        record.setAiConfirmed(0);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        recordMapper.insert(record);
        log.info("创建病历: recordNo={}, consultationId={}", record.getRecordNo(), consultationId);
        return record;
    }

    /**
     * 更新病历内容
     */
    @Transactional
    @Auditable(action = "UPDATE_MEDICAL_RECORD", module = "medical_record")
    public MedicalRecord updateRecord(Long recordId, MedicalRecord updates) {
        MedicalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "病历不存在");
        }
        
        // 已提交的病历不允许修改核心内容，仅允许补充
        if ("SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "已提交的病历不允许修改");
        }
        
        // 更新字段
        if (updates.getChiefComplaint() != null) record.setChiefComplaint(updates.getChiefComplaint());
        if (updates.getPresentIllness() != null) record.setPresentIllness(updates.getPresentIllness());
        if (updates.getPastHistory() != null) record.setPastHistory(updates.getPastHistory());
        if (updates.getAllergyHistory() != null) record.setAllergyHistory(updates.getAllergyHistory());
        if (updates.getPhysicalExam() != null) record.setPhysicalExam(updates.getPhysicalExam());
        if (updates.getAuxiliaryExam() != null) record.setAuxiliaryExam(updates.getAuxiliaryExam());
        if (updates.getDiagnosis() != null) record.setDiagnosis(updates.getDiagnosis());
        if (updates.getTreatmentPlan() != null) record.setTreatmentPlan(updates.getTreatmentPlan());
        if (updates.getFollowupAdvice() != null) record.setFollowupAdvice(updates.getFollowupAdvice());
        
        record.setUpdatedAt(LocalDateTime.now());
        recordMapper.updateById(record);
        
        return record;
    }

    /**
     * 提交病历
     */
    @Transactional
    @Auditable(action = "SUBMIT_MEDICAL_RECORD", module = "medical_record")
    public MedicalRecord submitRecord(Long recordId) {
        MedicalRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "病历不存在");
        }
        
        if ("SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "病历已提交");
        }
        
        // 校验必填字段
        if (record.getChiefComplaint() == null || record.getDiagnosis() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "主诉和诊断为必填项");
        }
        
        record.setStatus("SUBMITTED");
        record.setSubmittedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        recordMapper.updateById(record);
        
        log.info("病历已提交: recordNo={}", record.getRecordNo());
        return record;
    }

    /**
     * 查询病历详情
     */
    @DataScope(value = DataScopeType.PATIENT_SELF)
    public MedicalRecord getRecord(Long recordId) {
        return recordMapper.selectById(recordId);
    }

    /**
     * 根据问诊ID查询病历
     */
    public MedicalRecord getByConsultationId(Long consultationId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getConsultationId, consultationId);
        return recordMapper.selectOne(wrapper);
    }

    /**
     * 查询患者病历列表
     */
    @DataScope(value = DataScopeType.PATIENT_SELF)
    public List<MedicalRecord> listByPatientId(Long patientId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getPatientId, patientId)
               .eq(MedicalRecord::getStatus, "SUBMITTED")
               .orderByDesc(MedicalRecord::getCreatedAt);
        return recordMapper.selectList(wrapper);
    }

    /**
     * 添加附件
     */
    @Transactional
    @Auditable(action = "ADD_ATTACHMENT", module = "medical_attachment")
    public MedicalAttachment addAttachment(MedicalAttachment attachment) {
        attachment.setCreatedAt(LocalDateTime.now());
        attachmentMapper.insert(attachment);
        return attachment;
    }

    /**
     * 查询病历附件
     */
    public List<MedicalAttachment> listAttachments(Long recordId) {
        LambdaQueryWrapper<MedicalAttachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalAttachment::getRecordId, recordId)
               .orderByDesc(MedicalAttachment::getCreatedAt);
        return attachmentMapper.selectList(wrapper);
    }

    /**
     * 病历不可删除 - 此方法禁止调用
     * _Requirements: 5.6_
     */
    public void deleteRecord(Long recordId) {
        throw new BusinessException(ErrorCode.FORBIDDEN, "病历不允许删除");
    }

    private String generateRecordNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "MR" + date + uuid;
    }
}
