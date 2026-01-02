package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.dto.CreateConsultationRequest;
import com.erkang.domain.entity.Appointment;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.entity.DoctorProfile;
import com.erkang.domain.enums.ConsultationStatus;
import com.erkang.mapper.AppointmentMapper;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.DoctorProfileMapper;
import com.erkang.security.DataScopeHelper;
import com.erkang.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 问诊服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationService {
    
    private final ConsultationMapper consultationMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final AuditService auditService;
    
    /**
     * 创建问诊
     */
    @Transactional
    public Consultation createConsultation(CreateConsultationRequest request) {
        Long patientId = UserContext.getUserId();
        
        // 根据 doctor_profile.id 获取医生的 user_id
        Long doctorUserId = request.getDoctorId();
        DoctorProfile doctorProfile = doctorProfileMapper.selectById(request.getDoctorId());
        if (doctorProfile != null && doctorProfile.getUserId() != null) {
            doctorUserId = doctorProfile.getUserId();
            log.info("转换医生ID: doctorProfileId={} -> userId={}", request.getDoctorId(), doctorUserId);
        }
        
        Consultation consultation = new Consultation();
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorUserId);
        consultation.setConsultationNo(generateConsultationNo());
        consultation.setConsultationType("VIDEO");
        consultation.setStatus(ConsultationStatus.WAITING.getCode());
        consultation.setStatusUpdatedAt(LocalDateTime.now());
        consultation.setRtcRoomId(UUID.randomUUID().toString().replace("-", ""));
        consultation.setSymptoms(request.getSymptoms());
        consultation.setScheduledAt(request.getScheduledAt());
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        
        consultationMapper.insert(consultation);
        
        auditService.log("CREATE_CONSULTATION", "CONSULTATION", "Consultation",
                consultation.getId(), "创建问诊预约");
        
        log.info("创建问诊: consultationNo={}, patientId={}, doctorId={}", 
                consultation.getConsultationNo(), patientId, request.getDoctorId());
        
        return consultation;
    }
    
    /**
     * 创建问诊（从预约创建）
     */
    @Transactional
    public Consultation createFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.APPOINTMENT_NOT_FOUND);
        }
        
        Consultation consultation = new Consultation();
        consultation.setAppointmentId(appointmentId);
        consultation.setPatientId(appointment.getPatientId());
        consultation.setDoctorId(appointment.getDoctorId());
        consultation.setConsultationNo(generateConsultationNo());
        consultation.setConsultationType("VIDEO");
        consultation.setStatus(ConsultationStatus.WAITING.getCode());
        consultation.setStatusUpdatedAt(LocalDateTime.now());
        consultation.setRtcRoomId(UUID.randomUUID().toString().replace("-", ""));
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setUpdatedAt(LocalDateTime.now());
        
        consultationMapper.insert(consultation);
        
        log.info("创建问诊: consultationNo={}, patientId={}, doctorId={}", 
                consultation.getConsultationNo(), consultation.getPatientId(), consultation.getDoctorId());
        
        return consultation;
    }
    
    /**
     * 医生接诊
     */
    @Transactional
    public void startConsultation(Long consultationId) {
        Consultation consultation = getAndCheckOwnership(consultationId);
        
        ConsultationStatus currentStatus = ConsultationStatus.fromCode(consultation.getStatus());
        if (!currentStatus.canTransitionTo(ConsultationStatus.IN_PROGRESS)) {
            throw new BusinessException(ErrorCode.CONSULT_STATUS_INVALID, 
                    "当前状态不允许接诊: " + currentStatus.getName());
        }
        
        consultation.setStatus(ConsultationStatus.IN_PROGRESS.getCode());
        consultation.setStatusUpdatedAt(LocalDateTime.now());
        consultation.setStartTime(LocalDateTime.now());
        consultationMapper.updateById(consultation);
        
        auditService.log("START_CONSULTATION", "CONSULTATION", "Consultation", 
                consultationId, "医生接诊");
        
        log.info("医生接诊: consultationId={}", consultationId);
    }

    /**
     * 结束问诊
     */
    @Transactional
    public void finishConsultation(Long consultationId) {
        Consultation consultation = getAndCheckOwnership(consultationId);
        
        ConsultationStatus currentStatus = ConsultationStatus.fromCode(consultation.getStatus());
        if (!currentStatus.canTransitionTo(ConsultationStatus.FINISHED)) {
            throw new BusinessException(ErrorCode.CONSULT_STATUS_INVALID,
                    "当前状态不允许结束: " + currentStatus.getName());
        }
        
        consultation.setStatus(ConsultationStatus.FINISHED.getCode());
        consultation.setStatusUpdatedAt(LocalDateTime.now());
        consultation.setEndTime(LocalDateTime.now());
        
        // 计算时长
        if (consultation.getStartTime() != null) {
            long minutes = java.time.Duration.between(
                    consultation.getStartTime(), consultation.getEndTime()).toMinutes();
            consultation.setDuration((int) minutes);
        }
        
        consultationMapper.updateById(consultation);
        
        auditService.log("FINISH_CONSULTATION", "CONSULTATION", "Consultation",
                consultationId, "问诊结束");
        
        log.info("问诊结束: consultationId={}, duration={}分钟", consultationId, consultation.getDuration());
    }
    
    /**
     * 取消问诊
     */
    @Transactional
    public void cancelConsultation(Long consultationId, String reason) {
        Consultation consultation = getAndCheckOwnership(consultationId);
        
        ConsultationStatus currentStatus = ConsultationStatus.fromCode(consultation.getStatus());
        if (!currentStatus.canTransitionTo(ConsultationStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.CONSULT_STATUS_INVALID,
                    "当前状态不允许取消: " + currentStatus.getName());
        }
        
        consultation.setStatus(ConsultationStatus.CANCELED.getCode());
        consultation.setStatusUpdatedAt(LocalDateTime.now());
        consultationMapper.updateById(consultation);
        
        auditService.log("CANCEL_CONSULTATION", "CONSULTATION", "Consultation",
                consultationId, "取消问诊: " + reason);
        
        log.info("取消问诊: consultationId={}, reason={}", consultationId, reason);
    }
    
    /**
     * 获取问诊详情并校验归属
     */
    private Consultation getAndCheckOwnership(Long consultationId) {
        Consultation consultation = consultationMapper.selectById(consultationId);
        if (consultation == null) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_FOUND);
        }
        
        Long currentUserId = UserContext.getUserId();
        
        // 检查是否为患者本人或接诊医生
        if (!consultation.getPatientId().equals(currentUserId) && 
            !consultation.getDoctorId().equals(currentUserId) &&
            !DataScopeHelper.isAdmin()) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_BELONG);
        }
        
        return consultation;
    }
    
    /**
     * 生成问诊编号
     */
    private String generateConsultationNo() {
        return "C" + System.currentTimeMillis() + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * 根据ID获取问诊
     */
    public Consultation getById(Long consultationId) {
        return getAndCheckOwnership(consultationId);
    }
    
    /**
     * 获取问诊详情（包含患者信息）
     */
    public Map<String, Object> getDetailById(Long consultationId) {
        // 先校验权限
        getAndCheckOwnership(consultationId);
        
        Map<String, Object> detail = consultationMapper.selectDetailById(consultationId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.CONSULT_NOT_FOUND);
        }
        
        // 构建患者信息
        Map<String, Object> patient = new java.util.HashMap<>();
        patient.put("id", detail.get("patient_id"));
        patient.put("name", detail.get("patient_name"));
        patient.put("phone", detail.get("patient_phone"));
        patient.put("gender", detail.get("patient_gender"));
        
        // 计算年龄
        Object birthDate = detail.get("patient_birth_date");
        if (birthDate != null) {
            try {
                java.time.LocalDate birth = null;
                if (birthDate instanceof java.time.LocalDate) {
                    birth = (java.time.LocalDate) birthDate;
                } else if (birthDate instanceof java.sql.Date) {
                    birth = ((java.sql.Date) birthDate).toLocalDate();
                }
                if (birth != null) {
                    int age = java.time.Period.between(birth, java.time.LocalDate.now()).getYears();
                    patient.put("age", age);
                }
            } catch (Exception e) {
                log.warn("计算年龄失败", e);
            }
        }
        
        // 手机号脱敏
        String phone = (String) detail.get("patient_phone");
        if (phone != null && phone.length() >= 7) {
            patient.put("phoneMasked", phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4));
        }
        
        detail.put("patient", patient);
        
        // 移除原始字段
        detail.remove("patient_user_id");
        detail.remove("patient_name");
        detail.remove("patient_phone");
        detail.remove("patient_gender");
        detail.remove("patient_birth_date");
        
        return detail;
    }
}
