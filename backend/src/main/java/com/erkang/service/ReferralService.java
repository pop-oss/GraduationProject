package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.dto.CreateReferralRequest;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.entity.Referral;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.ReferralMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 转诊服务
 * _Requirements: 7.1, 7.2, 7.6_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralMapper referralMapper;
    private final ConsultationMapper consultationMapper;

    /**
     * 从请求创建转诊
     */
    @Transactional
    @Auditable(action = "CREATE_REFERRAL", module = "referral")
    public Referral createReferralFromRequest(Long fromDoctorId, CreateReferralRequest request) {
        // 查找问诊记录
        Consultation consultation = findConsultation(request.getConsultationId());
        if (consultation == null) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_FOUND, "问诊记录不存在");
        }
        
        Referral referral = new Referral();
        referral.setConsultationId(consultation.getId());
        referral.setPatientId(consultation.getPatientId());
        referral.setFromDoctorId(fromDoctorId);
        referral.setToDoctorId(request.getToDoctorId());
        referral.setToDepartmentId(request.getToDepartmentId());
        referral.setMedicalSummary(request.getSummary());
        referral.setReason(request.getDescription());
        
        return createReferral(referral);
    }
    
    /**
     * 根据问诊编号或ID查找问诊
     */
    private Consultation findConsultation(String consultationIdOrNo) {
        if (consultationIdOrNo == null || consultationIdOrNo.trim().isEmpty()) {
            return null;
        }
        
        // 尝试按ID查找
        try {
            Long id = Long.parseLong(consultationIdOrNo);
            return consultationMapper.selectById(id);
        } catch (NumberFormatException e) {
            // 按问诊编号查找
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Consultation::getConsultationNo, consultationIdOrNo);
            return consultationMapper.selectOne(wrapper);
        }
    }

    /**
     * 发起转诊
     */
    @Transactional
    @Auditable(action = "CREATE_REFERRAL", module = "referral")
    public Referral createReferral(Referral referral) {
        // 校验必填字段
        validateReferralData(referral);
        
        referral.setReferralNo(generateReferralNo());
        referral.setStatus("PENDING");
        referral.setCreatedAt(LocalDateTime.now());
        referral.setUpdatedAt(LocalDateTime.now());
        
        referralMapper.insert(referral);
        log.info("发起转诊: referralNo={}, fromDoctor={}, toDoctor={}", 
                referral.getReferralNo(), referral.getFromDoctorId(), referral.getToDoctorId());
        return referral;
    }

    /**
     * 校验转诊数据完整性
     * _Requirements: 7.1_
     */
    private void validateReferralData(Referral referral) {
        if (referral.getMedicalSummary() == null || referral.getMedicalSummary().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.REFERRAL_INFO_INCOMPLETE, "病历摘要为必填项");
        }
        if (referral.getReason() == null || referral.getReason().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.REFERRAL_INFO_INCOMPLETE, "转诊原因为必填项");
        }
    }

    /**
     * 接受转诊
     */
    @Transactional
    @Auditable(action = "ACCEPT_REFERRAL", module = "referral")
    public Referral accept(Long referralId) {
        Referral referral = referralMapper.selectById(referralId);
        if (referral == null) {
            throw new BusinessException(ErrorCode.REFERRAL_NOT_FOUND);
        }
        
        if (!"PENDING".equals(referral.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "转诊状态不允许此操作");
        }
        
        referral.setStatus("ACCEPTED");
        referral.setAcceptedAt(LocalDateTime.now());
        referral.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(referral);
        
        log.info("接受转诊: referralNo={}", referral.getReferralNo());
        return referral;
    }

    /**
     * 拒绝转诊
     */
    @Transactional
    @Auditable(action = "REJECT_REFERRAL", module = "referral")
    public Referral reject(Long referralId, String reason) {
        Referral referral = referralMapper.selectById(referralId);
        if (referral == null) {
            throw new BusinessException(ErrorCode.REFERRAL_NOT_FOUND);
        }
        
        if (!"PENDING".equals(referral.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "转诊状态不允许此操作");
        }
        
        referral.setStatus("REJECTED");
        referral.setRejectReason(reason);
        referral.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(referral);
        
        log.info("拒绝转诊: referralNo={}, reason={}", referral.getReferralNo(), reason);
        return referral;
    }

    /**
     * 完成转诊
     */
    @Transactional
    @Auditable(action = "COMPLETE_REFERRAL", module = "referral")
    public Referral complete(Long referralId) {
        Referral referral = referralMapper.selectById(referralId);
        if (referral == null) {
            throw new BusinessException(ErrorCode.REFERRAL_NOT_FOUND);
        }
        
        if (!"ACCEPTED".equals(referral.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "转诊状态不允许此操作");
        }
        
        referral.setStatus("COMPLETED");
        referral.setStatusUpdatedAt(LocalDateTime.now());
        referral.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(referral);
        
        log.info("完成转诊: referralNo={}", referral.getReferralNo());
        return referral;
    }

    /**
     * 转诊不可删除
     * _Requirements: 7.6_
     */
    public void deleteReferral(Long referralId) {
        throw new BusinessException(ErrorCode.REFERRAL_CANNOT_DELETE, "转诊记录不允许删除");
    }

    /**
     * 查询转诊详情
     */
    public Referral getById(Long referralId) {
        return referralMapper.selectById(referralId);
    }

    /**
     * 查询转出转诊列表
     */
    public List<Referral> listByFromDoctorId(Long doctorId) {
        LambdaQueryWrapper<Referral> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Referral::getFromDoctorId, doctorId)
               .orderByDesc(Referral::getCreatedAt);
        return referralMapper.selectList(wrapper);
    }

    /**
     * 查询转入转诊列表
     */
    public List<Referral> listByToDoctorId(Long doctorId) {
        LambdaQueryWrapper<Referral> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Referral::getToDoctorId, doctorId)
               .orderByDesc(Referral::getCreatedAt);
        return referralMapper.selectList(wrapper);
    }

    /**
     * 查询患者转诊列表
     */
    public List<Referral> listByPatientId(Long patientId) {
        LambdaQueryWrapper<Referral> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Referral::getPatientId, patientId)
               .orderByDesc(Referral::getCreatedAt);
        return referralMapper.selectList(wrapper);
    }

    private String generateReferralNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "RF" + date + uuid;
    }
}
