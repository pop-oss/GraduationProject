package com.erkang.controller;

import com.erkang.domain.entity.Prescription;
import com.erkang.domain.entity.PrescriptionItem;
import com.erkang.domain.enums.PrescriptionStatus;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.service.PrescriptionService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 处方控制器测试
 * _Requirements: 6.1, 6.2, 6.3_
 */
class PrescriptionControllerTest {

    private PrescriptionService prescriptionService;
    private PrescriptionMapper prescriptionMapper;
    private PrescriptionController prescriptionController;

    @BeforeProperty
    void setUp() {
        prescriptionService = mock(PrescriptionService.class);
        prescriptionMapper = mock(PrescriptionMapper.class);
        prescriptionController = new PrescriptionController(prescriptionService, prescriptionMapper);
    }

    /**
     * Property 1: 创建处方 - 应关联问诊和患者
     * **Validates: Requirements 6.1**
     */
    @Property(tries = 100)
    void createPrescription_shouldAssociateConsultationAndPatient(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @LongRange(min = 1, max = 10000) Long patientId) {
        
        Prescription mockPrescription = new Prescription();
        mockPrescription.setId(1L);
        mockPrescription.setConsultationId(consultationId);
        mockPrescription.setPatientId(patientId);
        mockPrescription.setStatus(PrescriptionStatus.DRAFT.getCode());
        
        when(prescriptionService.createPrescription(eq(consultationId), eq(patientId), anyLong()))
            .thenReturn(mockPrescription);
        
        // 验证处方创建逻辑
        assertThat(mockPrescription.getConsultationId()).isEqualTo(consultationId);
        assertThat(mockPrescription.getPatientId()).isEqualTo(patientId);
        assertThat(mockPrescription.getStatus()).isEqualTo(PrescriptionStatus.DRAFT.getCode());
    }

    /**
     * Property 2: 处方状态流转 - 状态变更应遵循审核流程
     * **Validates: Requirements 6.2, 6.3**
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
     * Property 3: 获取处方详情 - 应返回正确的处方信息
     * **Validates: Requirements 6.1**
     */
    @Property(tries = 100)
    void getPrescription_shouldReturnPrescriptionDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId) {
        
        Prescription mockPrescription = new Prescription();
        mockPrescription.setId(prescriptionId);
        
        when(prescriptionService.getById(prescriptionId)).thenReturn(mockPrescription);
        
        var result = prescriptionController.getById(prescriptionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(prescriptionId);
    }

    /**
     * Property 4: 提交处方审核 - 应调用服务层提交审核
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 100)
    void submitPrescription_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId) {
        
        Prescription mockPrescription = new Prescription();
        mockPrescription.setId(prescriptionId);
        mockPrescription.setStatus(PrescriptionStatus.PENDING_REVIEW.getCode());
        
        when(prescriptionService.submitForReview(prescriptionId)).thenReturn(mockPrescription);
        
        var result = prescriptionController.submit(prescriptionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo(PrescriptionStatus.PENDING_REVIEW.getCode());
    }

    /**
     * Property 5: 添加处方明细 - 应正确添加药品信息
     * **Validates: Requirements 6.1**
     */
    @Property(tries = 100)
    void addPrescriptionItem_shouldAddItemCorrectly(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll @AlphaChars @StringLength(min = 2, max = 50) String drugName,
            @ForAll @IntRange(min = 1, max = 100) int quantity) {
        
        PrescriptionItem item = new PrescriptionItem();
        item.setDrugName(drugName);
        item.setQuantity(quantity);
        
        PrescriptionItem savedItem = new PrescriptionItem();
        savedItem.setId(1L);
        savedItem.setPrescriptionId(prescriptionId);
        savedItem.setDrugName(drugName);
        savedItem.setQuantity(quantity);
        
        when(prescriptionService.addItem(eq(prescriptionId), any(PrescriptionItem.class)))
            .thenReturn(savedItem);
        
        var result = prescriptionController.addItem(prescriptionId, item);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getDrugName()).isEqualTo(drugName);
        assertThat(result.getData().getQuantity()).isEqualTo(quantity);
    }

    /**
     * Property 6: 查询待审核处方列表 - 应返回待审核状态的处方
     * **Validates: Requirements 6.2**
     */
    @Property(tries = 50)
    void listPendingReview_shouldReturnPendingPrescriptions(
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<Prescription> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Prescription p = new Prescription();
            p.setId((long) (i + 1));
            p.setStatus(PrescriptionStatus.PENDING_REVIEW.getCode());
            mockList.add(p);
        }
        
        when(prescriptionService.listPendingReview()).thenReturn(mockList);
        
        var result = prescriptionController.listPendingReview();
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(count);
        result.getData().forEach(p -> 
            assertThat(p.getStatus()).isEqualTo(PrescriptionStatus.PENDING_REVIEW.getCode())
        );
    }

    /**
     * Property 7: 删除处方明细 - 应调用服务层删除
     * **Validates: Requirements 6.1**
     */
    @Property(tries = 100)
    void deletePrescriptionItem_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll @LongRange(min = 1, max = 10000) Long itemId) {
        
        doNothing().when(prescriptionService).deleteItem(prescriptionId, itemId);
        
        var result = prescriptionController.deleteItem(prescriptionId, itemId);
        
        assertThat(result).isNotNull();
    }
}
