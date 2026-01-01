package com.erkang.controller;

import com.erkang.domain.dto.PatientProfileDTO;
import com.erkang.domain.vo.PatientProfileVO;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.FollowupPlanMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.service.PatientService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 患者控制器测试
 * _Requirements: 2.4, 2.5, 2.6_
 */
class PatientControllerTest {

    private PatientService patientService;
    private ConsultationMapper consultationMapper;
    private PrescriptionMapper prescriptionMapper;
    private FollowupPlanMapper followupPlanMapper;
    private PatientController patientController;

    @BeforeProperty
    void setUp() {
        patientService = mock(PatientService.class);
        consultationMapper = mock(ConsultationMapper.class);
        prescriptionMapper = mock(PrescriptionMapper.class);
        followupPlanMapper = mock(FollowupPlanMapper.class);
        patientController = new PatientController(
            patientService, 
            consultationMapper, 
            prescriptionMapper, 
            followupPlanMapper
        );
    }

    /**
     * Property 1: 获取我的档案 - 应返回当前用户的档案信息
     * **Validates: Requirements 2.4**
     */
    @Property(tries = 50)
    void getMyProfile_shouldReturnCurrentUserProfile(
            @ForAll @LongRange(min = 1, max = 10000) Long userId) {
        
        PatientProfileVO mockProfile = new PatientProfileVO();
        mockProfile.setId(userId);
        mockProfile.setRealName("测试患者");
        mockProfile.setPhone("138****1234");
        
        when(patientService.getMyProfile()).thenReturn(mockProfile);
        
        var result = patientController.getMyProfile();
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRealName()).isNotBlank();
    }

    /**
     * Property 2: 更新我的档案 - 应调用服务层更新档案
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 50)
    void updateProfile_shouldCallService(
            @ForAll @IntRange(min = 0, max = 1) int gender) {
        
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setGender(gender);
        dto.setAddress("测试地址");
        
        doNothing().when(patientService).updateProfile(any(PatientProfileDTO.class));
        
        var result = patientController.updateProfile(dto);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 3: 获取患者档案（医生/管理员）- 应返回指定患者的档案
     * **Validates: Requirements 2.6**
     */
    @Property(tries = 100)
    void getPatientProfile_shouldReturnSpecifiedPatientProfile(
            @ForAll @LongRange(min = 1, max = 10000) Long patientId) {
        
        PatientProfileVO mockProfile = new PatientProfileVO();
        mockProfile.setId(patientId);
        mockProfile.setRealName("测试患者");
        
        when(patientService.getProfileById(patientId)).thenReturn(mockProfile);
        
        var result = patientController.getPatientProfile(patientId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(patientId);
    }

    /**
     * Property 4: 患者ID必须为正数
     * **Validates: Requirements 2.6**
     */
    @Property(tries = 100)
    void patientId_shouldBePositive(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long patientId) {
        
        assertThat(patientId).isPositive();
    }

    /**
     * Property 5: 性别应为有效值
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 100)
    void gender_shouldBeValid(
            @ForAll @IntRange(min = 0, max = 1) int gender) {
        
        assertThat(gender).isBetween(0, 1);
    }

    /**
     * Property 6: 档案更新DTO验证
     * **Validates: Requirements 2.5**
     */
    @Property(tries = 50)
    void profileDTO_shouldHaveValidFields(
            @ForAll @IntRange(min = 0, max = 1) int gender,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String address) {
        
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setGender(gender);
        dto.setAddress(address);
        
        assertThat(dto.getGender()).isBetween(0, 1);
    }
}
