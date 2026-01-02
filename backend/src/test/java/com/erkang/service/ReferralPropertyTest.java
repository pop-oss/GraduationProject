package com.erkang.service;

import com.erkang.common.BusinessException;
import com.erkang.domain.entity.Referral;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.ReferralMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 转诊数据完整性属性测试
 * **Property 9: 转诊数据完整性**
 * **Validates: Requirements 7.1, 7.6**
 */
class ReferralPropertyTest {

    private ReferralService createService() {
        ReferralMapper referralMapper = Mockito.mock(ReferralMapper.class);
        ConsultationMapper consultationMapper = Mockito.mock(ConsultationMapper.class);
        when(referralMapper.insert(any())).thenReturn(1);
        return new ReferralService(referralMapper, consultationMapper);
    }

    /**
     * Property 9.1: 转诊必须包含病历摘要
     * *For any* referral without medical summary, creation should fail
     */
    @Property(tries = 100)
    void referralRequiresMedicalSummary(
            @ForAll @LongRange(min = 1, max = 1000000) Long patientId,
            @ForAll @LongRange(min = 1, max = 1000000) Long fromDoctorId,
            @ForAll @LongRange(min = 1, max = 1000000) Long toDoctorId,
            @ForAll("validStrings") String reason,
            @ForAll("emptyOrNullStrings") String medicalSummary) {
        
        ReferralService service = createService();
        
        Referral referral = new Referral();
        referral.setPatientId(patientId);
        referral.setFromDoctorId(fromDoctorId);
        referral.setToDoctorId(toDoctorId);
        referral.setReason(reason);
        referral.setMedicalSummary(medicalSummary);
        
        assertThatThrownBy(() -> service.createReferral(referral))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("病历摘要为必填项");
    }

    /**
     * Property 9.2: 转诊必须包含转诊原因
     * *For any* referral without reason, creation should fail
     */
    @Property(tries = 100)
    void referralRequiresReason(
            @ForAll @LongRange(min = 1, max = 1000000) Long patientId,
            @ForAll @LongRange(min = 1, max = 1000000) Long fromDoctorId,
            @ForAll @LongRange(min = 1, max = 1000000) Long toDoctorId,
            @ForAll("validStrings") String medicalSummary,
            @ForAll("emptyOrNullStrings") String reason) {
        
        ReferralService service = createService();
        
        Referral referral = new Referral();
        referral.setPatientId(patientId);
        referral.setFromDoctorId(fromDoctorId);
        referral.setToDoctorId(toDoctorId);
        referral.setReason(reason);
        referral.setMedicalSummary(medicalSummary);
        
        assertThatThrownBy(() -> service.createReferral(referral))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("转诊原因为必填项");
    }

    /**
     * Property 9.3: 完整数据的转诊可以创建成功
     * *For any* referral with complete data, creation should succeed
     */
    @Property(tries = 100)
    void completeReferralCanBeCreated(
            @ForAll @LongRange(min = 1, max = 1000000) Long patientId,
            @ForAll @LongRange(min = 1, max = 1000000) Long fromDoctorId,
            @ForAll @LongRange(min = 1, max = 1000000) Long toDoctorId,
            @ForAll("validStrings") String reason,
            @ForAll("validStrings") String medicalSummary) {
        
        ReferralService service = createService();
        
        Referral referral = new Referral();
        referral.setPatientId(patientId);
        referral.setFromDoctorId(fromDoctorId);
        referral.setToDoctorId(toDoctorId);
        referral.setReason(reason);
        referral.setMedicalSummary(medicalSummary);
        
        Referral created = service.createReferral(referral);
        
        assertThat(created.getReferralNo()).isNotNull();
        assertThat(created.getReferralNo()).startsWith("RF");
        assertThat(created.getStatus()).isEqualTo("PENDING");
    }

    /**
     * Property 9.4: 转诊记录不可删除
     * *For any* referral, delete operation should always be rejected
     */
    @Property(tries = 100)
    void referralCannotBeDeleted(
            @ForAll @LongRange(min = 1, max = 1000000) Long referralId) {
        
        ReferralService service = createService();
        
        assertThatThrownBy(() -> service.deleteReferral(referralId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("转诊记录不允许删除");
    }

    @Provide
    Arbitrary<String> validStrings() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(5)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<String> emptyOrNullStrings() {
        return Arbitraries.of(null, "", "   ", "\t", "\n");
    }
}
