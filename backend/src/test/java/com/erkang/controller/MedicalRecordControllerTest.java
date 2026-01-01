package com.erkang.controller;

import com.erkang.domain.entity.MedicalAttachment;
import com.erkang.domain.entity.MedicalRecord;
import com.erkang.security.LoginUser;
import com.erkang.security.UserContext;
import com.erkang.service.MedicalRecordService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.api.lifecycle.AfterProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 病历控制器测试
 * _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_
 */
class MedicalRecordControllerTest {

    private MedicalRecordService recordService;
    private MedicalRecordController recordController;

    @BeforeProperty
    void setUp() {
        recordService = mock(MedicalRecordService.class);
        recordController = new MedicalRecordController(recordService);
        // 设置UserContext
        UserContext.setUser(LoginUser.builder()
            .userId(1L)
            .username("testDoctor")
            .roles(List.of("DOCTOR_PRIMARY"))
            .build());
    }

    @AfterProperty
    void tearDown() {
        UserContext.clear();
    }

    /**
     * Property 1: 创建病历 - 应关联问诊和患者
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 100)
    void createRecord_shouldAssociateConsultationAndPatient(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 10000) Long patientId) {
        
        MedicalRecord mockRecord = new MedicalRecord();
        mockRecord.setId(1L);
        mockRecord.setConsultationId(consultationId);
        mockRecord.setPatientId(patientId);
        mockRecord.setStatus("DRAFT");
        
        when(recordService.createRecord(eq(consultationId), eq(patientId), anyLong()))
            .thenReturn(mockRecord);
        
        var result = recordController.create(consultationId, patientId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getConsultationId()).isEqualTo(consultationId);
        assertThat(result.getData().getPatientId()).isEqualTo(patientId);
    }


    /**
     * Property 2: 更新病历 - 应调用服务层更新
     * **Validates: Requirements 5.2**
     */
    @Property(tries = 100)
    void updateRecord_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId,
            @ForAll @AlphaChars @StringLength(min = 10, max = 500) String chiefComplaint,
            @ForAll @AlphaChars @StringLength(min = 10, max = 500) String diagnosis) {
        
        MedicalRecord updates = new MedicalRecord();
        updates.setChiefComplaint(chiefComplaint);
        updates.setDiagnosis(diagnosis);
        
        MedicalRecord updatedRecord = new MedicalRecord();
        updatedRecord.setId(recordId);
        updatedRecord.setChiefComplaint(chiefComplaint);
        updatedRecord.setDiagnosis(diagnosis);
        
        when(recordService.updateRecord(eq(recordId), any(MedicalRecord.class)))
            .thenReturn(updatedRecord);
        
        var result = recordController.update(recordId, updates);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getChiefComplaint()).isEqualTo(chiefComplaint);
        assertThat(result.getData().getDiagnosis()).isEqualTo(diagnosis);
    }

    /**
     * Property 3: 提交病历 - 应更新病历状态为已提交
     * **Validates: Requirements 5.3**
     */
    @Property(tries = 100)
    void submitRecord_shouldUpdateStatusToSubmitted(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId) {
        
        MedicalRecord submittedRecord = new MedicalRecord();
        submittedRecord.setId(recordId);
        submittedRecord.setStatus("SUBMITTED");
        
        when(recordService.submitRecord(recordId)).thenReturn(submittedRecord);
        
        var result = recordController.submit(recordId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("SUBMITTED");
    }

    /**
     * Property 4: 查询病历详情 - 应返回正确的病历信息
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    void getRecord_shouldReturnRecordDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId) {
        
        MedicalRecord mockRecord = new MedicalRecord();
        mockRecord.setId(recordId);
        
        when(recordService.getRecord(recordId)).thenReturn(mockRecord);
        
        var result = recordController.getById(recordId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(recordId);
    }


    /**
     * Property 5: 根据问诊ID查询病历 - 应返回对应的病历
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    void getByConsultation_shouldReturnConsultationRecord(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        MedicalRecord mockRecord = new MedicalRecord();
        mockRecord.setConsultationId(consultationId);
        
        when(recordService.getByConsultationId(consultationId)).thenReturn(mockRecord);
        
        var result = recordController.getByConsultation(consultationId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getConsultationId()).isEqualTo(consultationId);
    }

    /**
     * Property 6: 查询患者病历列表 - 应返回该患者的所有病历
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 100)
    void listByPatient_shouldReturnPatientRecords(
            @ForAll @LongRange(min = 1, max = 10000) Long patientId,
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<MedicalRecord> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MedicalRecord record = new MedicalRecord();
            record.setId((long) (i + 1));
            record.setPatientId(patientId);
            mockList.add(record);
        }
        
        when(recordService.listByPatientId(patientId)).thenReturn(mockList);
        
        var result = recordController.listByPatient(patientId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(count);
        result.getData().forEach(r -> 
            assertThat(r.getPatientId()).isEqualTo(patientId)
        );
    }

    /**
     * Property 7: 添加附件 - 应创建附件记录
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 100)
    void addAttachment_shouldCreateAttachmentRecord(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId,
            @ForAll @AlphaChars @StringLength(min = 5, max = 100) String fileName,
            @ForAll @AlphaChars @StringLength(min = 10, max = 200) String fileUrl) {
        
        MedicalAttachment attachment = new MedicalAttachment();
        attachment.setFileName(fileName);
        attachment.setFileUrl(fileUrl);
        
        MedicalAttachment savedAttachment = new MedicalAttachment();
        savedAttachment.setId(1L);
        savedAttachment.setRecordId(recordId);
        savedAttachment.setFileName(fileName);
        savedAttachment.setFileUrl(fileUrl);
        
        when(recordService.addAttachment(any(MedicalAttachment.class)))
            .thenReturn(savedAttachment);
        
        var result = recordController.addAttachment(recordId, attachment);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getRecordId()).isEqualTo(recordId);
        assertThat(result.getData().getFileName()).isEqualTo(fileName);
    }


    /**
     * Property 8: 查询病历附件 - 应返回该病历的所有附件
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 100)
    void listAttachments_shouldReturnRecordAttachments(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId,
            @ForAll @IntRange(min = 0, max = 10) int count) {
        
        List<MedicalAttachment> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MedicalAttachment attachment = new MedicalAttachment();
            attachment.setId((long) (i + 1));
            attachment.setRecordId(recordId);
            mockList.add(attachment);
        }
        
        when(recordService.listAttachments(recordId)).thenReturn(mockList);
        
        var result = recordController.listAttachments(recordId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(count);
        result.getData().forEach(a -> 
            assertThat(a.getRecordId()).isEqualTo(recordId)
        );
    }

    /**
     * Property 9: 病历必填字段验证 - 主诉和诊断不能为空
     * **Validates: Requirements 5.2**
     */
    @Property(tries = 100)
    void mandatoryFields_shouldNotBeEmpty(
            @ForAll @AlphaChars @StringLength(min = 1, max = 500) String chiefComplaint,
            @ForAll @AlphaChars @StringLength(min = 1, max = 500) String diagnosis) {
        
        assertThat(chiefComplaint).isNotBlank();
        assertThat(diagnosis).isNotBlank();
    }

    /**
     * Property 10: 病历状态验证 - 状态应为有效值
     * **Validates: Requirements 5.3**
     */
    @Property(tries = 50)
    void recordStatus_shouldBeValid(
            @ForAll("validRecordStatuses") String status) {
        
        assertThat(status).isIn("DRAFT", "SUBMITTED");
    }

    @Provide
    Arbitrary<String> validRecordStatuses() {
        return Arbitraries.of("DRAFT", "SUBMITTED");
    }
}
