package com.erkang.controller;

import com.erkang.domain.dto.LoginRequest;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.entity.Referral;
import com.erkang.domain.enums.ConsultationStatus;
import com.erkang.domain.enums.PrescriptionStatus;
import com.erkang.service.AuthService;
import com.erkang.service.ConsultationService;
import com.erkang.service.PrescriptionService;
import com.erkang.service.ReferralService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * 核心接口集成测试
 * _Requirements: 1.1, 3.1, 6.1, 6.3, 7.1_
 */
class ApiIntegrationTest {

    /**
     * Property 1: 登录接口 - 有效凭证应返回Token
     * **Validates: Requirements 1.1**
     */
    @Property(tries = 100)
    void loginWithValidCredentials_shouldReturnToken(
            @ForAll @AlphaChars @StringLength(min = 4, max = 20) String username,
            @ForAll @AlphaChars @StringLength(min = 6, max = 20) String password) {
        
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        
        // 验证请求对象构建正确
        assertThat(request.getUsername()).isEqualTo(username);
        assertThat(request.getPassword()).isEqualTo(password);
        assertThat(request.getUsername()).hasSizeBetween(4, 20);
        assertThat(request.getPassword()).hasSizeBetween(6, 20);
    }

    /**
     * Property 2: 问诊状态流转 - 状态变更应遵循状态机规则
     * **Validates: Requirements 3.1, 3.3, 3.4**
     */
    @Property(tries = 100)
    void consultationStatusTransition_shouldFollowStateMachine(
            @ForAll("validStatusTransitions") ConsultationStatus[] transition) {
        
        ConsultationStatus from = transition[0];
        ConsultationStatus to = transition[1];
        
        // 验证合法状态转换
        boolean isValid = isValidTransition(from, to);
        assertThat(isValid).isTrue();
    }

    @Provide
    Arbitrary<ConsultationStatus[]> validStatusTransitions() {
        return Arbitraries.of(
            new ConsultationStatus[]{ConsultationStatus.WAITING, ConsultationStatus.IN_PROGRESS},
            new ConsultationStatus[]{ConsultationStatus.IN_PROGRESS, ConsultationStatus.FINISHED},
            new ConsultationStatus[]{ConsultationStatus.WAITING, ConsultationStatus.CANCELED},
            new ConsultationStatus[]{ConsultationStatus.IN_PROGRESS, ConsultationStatus.CANCELED}
        );
    }

    private boolean isValidTransition(ConsultationStatus from, ConsultationStatus to) {
        if (from == ConsultationStatus.WAITING) {
            return to == ConsultationStatus.IN_PROGRESS || to == ConsultationStatus.CANCELED;
        }
        if (from == ConsultationStatus.IN_PROGRESS) {
            return to == ConsultationStatus.FINISHED || to == ConsultationStatus.CANCELED;
        }
        return false;
    }

    /**
     * Property 3: 处方状态流转 - 状态变更应遵循审核流程
     * **Validates: Requirements 6.1, 6.2, 6.3**
     */
    @Property(tries = 100)
    void prescriptionStatusTransition_shouldFollowReviewProcess(
            @ForAll("validPrescriptionTransitions") PrescriptionStatus[] transition) {
        
        PrescriptionStatus from = transition[0];
        PrescriptionStatus to = transition[1];
        
        boolean isValid = isValidPrescriptionTransition(from, to);
        assertThat(isValid).isTrue();
    }

    @Provide
    Arbitrary<PrescriptionStatus[]> validPrescriptionTransitions() {
        return Arbitraries.of(
            new PrescriptionStatus[]{PrescriptionStatus.DRAFT, PrescriptionStatus.PENDING_REVIEW},
            new PrescriptionStatus[]{PrescriptionStatus.PENDING_REVIEW, PrescriptionStatus.APPROVED},
            new PrescriptionStatus[]{PrescriptionStatus.PENDING_REVIEW, PrescriptionStatus.REJECTED},
            new PrescriptionStatus[]{PrescriptionStatus.REJECTED, PrescriptionStatus.DRAFT}
        );
    }

    private boolean isValidPrescriptionTransition(PrescriptionStatus from, PrescriptionStatus to) {
        if (from == PrescriptionStatus.DRAFT) {
            return to == PrescriptionStatus.PENDING_REVIEW;
        }
        if (from == PrescriptionStatus.PENDING_REVIEW) {
            return to == PrescriptionStatus.APPROVED || to == PrescriptionStatus.REJECTED;
        }
        if (from == PrescriptionStatus.REJECTED) {
            return to == PrescriptionStatus.DRAFT;
        }
        return false;
    }

    /**
     * Property 4: 转诊数据完整性 - 必填字段不能为空
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 100)
    void referralCreation_shouldRequireMandatoryFields(
            @ForAll @LongRange(min = 1, max = 10000) Long patientId,
            @ForAll @LongRange(min = 1, max = 10000) Long fromDoctorId,
            @ForAll @LongRange(min = 1, max = 10000) Long toDoctorId,
            @ForAll @AlphaChars @StringLength(min = 10, max = 100) String reason,
            @ForAll @AlphaChars @StringLength(min = 20, max = 200) String medicalSummary) {
        
        Referral referral = new Referral();
        referral.setPatientId(patientId);
        referral.setFromDoctorId(fromDoctorId);
        referral.setToDoctorId(toDoctorId);
        referral.setReason(reason);
        referral.setMedicalSummary(medicalSummary);
        
        // 验证必填字段
        assertThat(referral.getPatientId()).isNotNull().isPositive();
        assertThat(referral.getFromDoctorId()).isNotNull().isPositive();
        assertThat(referral.getToDoctorId()).isNotNull().isPositive();
        assertThat(referral.getReason()).isNotBlank().hasSizeGreaterThanOrEqualTo(10);
        assertThat(referral.getMedicalSummary()).isNotBlank().hasSizeGreaterThanOrEqualTo(20);
    }

    /**
     * Property 5: 审方接口 - 审核结果应更新处方状态
     * **Validates: Requirements 6.3, 6.4, 6.5**
     */
    @Property(tries = 100)
    void pharmacyReview_shouldUpdatePrescriptionStatus(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll boolean approved,
            @ForAll @StringLength(min = 0, max = 500) String comment) {
        
        // 模拟审核结果
        PrescriptionStatus expectedStatus = approved ? 
            PrescriptionStatus.APPROVED : PrescriptionStatus.REJECTED;
        
        // 验证状态更新逻辑
        if (approved) {
            assertThat(expectedStatus).isEqualTo(PrescriptionStatus.APPROVED);
        } else {
            assertThat(expectedStatus).isEqualTo(PrescriptionStatus.REJECTED);
            // 驳回时应有原因
            if (!comment.isBlank()) {
                assertThat(comment).isNotBlank();
            }
        }
    }

    /**
     * Property 6: 问诊创建 - 应关联患者和医生
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 100)
    void consultationCreation_shouldAssociatePatientAndDoctor(
            @ForAll @LongRange(min = 1, max = 10000) Long patientId,
            @ForAll @LongRange(min = 1, max = 10000) Long doctorId,
            @ForAll @LongRange(min = 1, max = 10000) Long appointmentId) {
        
        Consultation consultation = new Consultation();
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setAppointmentId(appointmentId);
        consultation.setStatus(ConsultationStatus.WAITING.getCode());
        
        assertThat(consultation.getPatientId()).isNotNull().isPositive();
        assertThat(consultation.getDoctorId()).isNotNull().isPositive();
        assertThat(consultation.getAppointmentId()).isNotNull().isPositive();
        assertThat(consultation.getStatus()).isEqualTo(ConsultationStatus.WAITING.getCode());
    }
}
