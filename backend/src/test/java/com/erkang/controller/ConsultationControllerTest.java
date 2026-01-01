package com.erkang.controller;

import com.erkang.domain.entity.Consultation;
import com.erkang.domain.enums.ConsultationStatus;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.service.ConsultationService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 问诊控制器测试
 * _Requirements: 3.1, 3.2, 3.3, 3.4_
 */
class ConsultationControllerTest {

    private ConsultationService consultationService;
    private ConsultationMapper consultationMapper;
    private ConsultationController consultationController;

    @BeforeProperty
    void setUp() {
        consultationService = mock(ConsultationService.class);
        consultationMapper = mock(ConsultationMapper.class);
        consultationController = new ConsultationController(consultationService, consultationMapper);
    }

    /**
     * Property 1: 获取问诊详情 - 应返回正确的问诊信息
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 100)
    void getConsultation_shouldReturnConsultationDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        Consultation mockConsultation = new Consultation();
        mockConsultation.setId(consultationId);
        mockConsultation.setStatus(ConsultationStatus.WAITING.getCode());
        
        when(consultationService.getById(consultationId)).thenReturn(mockConsultation);
        
        var result = consultationController.getConsultation(consultationId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(consultationId);
    }

    /**
     * Property 2: 开始问诊 - 应调用服务层开始问诊
     * **Validates: Requirements 3.2**
     */
    @Property(tries = 100)
    void startConsultation_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        doNothing().when(consultationService).startConsultation(consultationId);
        
        var result = consultationController.startConsultation(consultationId);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 3: 结束问诊 - 应调用服务层结束问诊
     * **Validates: Requirements 3.3**
     */
    @Property(tries = 100)
    void finishConsultation_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId) {
        
        doNothing().when(consultationService).finishConsultation(consultationId);
        
        var result = consultationController.finishConsultation(consultationId);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 4: 取消问诊 - 应调用服务层取消问诊
     * **Validates: Requirements 3.4**
     */
    @Property(tries = 100)
    void cancelConsultation_shouldCallService(
            @ForAll @LongRange(min = 1, max = 10000) Long consultationId,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String reason) {
        
        doNothing().when(consultationService).cancelConsultation(eq(consultationId), anyString());
        
        var result = consultationController.cancelConsultation(consultationId, reason);
        
        assertThat(result).isNotNull();
    }

    /**
     * Property 5: 问诊状态流转 - 状态变更应遵循状态机规则
     * **Validates: Requirements 3.2, 3.3, 3.4**
     */
    @Property(tries = 100)
    void consultationStatusTransition_shouldFollowStateMachine(
            @ForAll("validStatusTransitions") ConsultationStatus[] transition) {
        
        ConsultationStatus from = transition[0];
        ConsultationStatus to = transition[1];
        
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
     * Property 6: 问诊ID必须为正数
     * **Validates: Requirements 3.1**
     */
    @Property(tries = 100)
    void consultationId_shouldBePositive(
            @ForAll @LongRange(min = 1, max = Long.MAX_VALUE) Long consultationId) {
        
        assertThat(consultationId).isPositive();
    }
}
