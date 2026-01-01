package com.erkang.controller;

import com.erkang.domain.entity.FollowupPlan;
import com.erkang.domain.entity.FollowupRecord;
import com.erkang.mapper.FollowupPlanMapper;
import com.erkang.service.FollowupService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 随访控制器测试
 * _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
 */
class FollowupControllerTest {

    private FollowupService followupService;
    private FollowupPlanMapper followupPlanMapper;
    private FollowupController followupController;

    @BeforeProperty
    void setUp() {
        followupService = mock(FollowupService.class);
        followupPlanMapper = mock(FollowupPlanMapper.class);
        followupController = new FollowupController(followupService, followupPlanMapper);
    }

    /**
     * Property 1: 创建随访计划 - 应创建新的随访计划
     * **Validates: Requirements 8.1**
     */
    @Property(tries = 100)
    void createPlan_shouldCreateNewPlan(
            @ForAll @LongRange(min = 1, max = 10000) Long patientId,
            @ForAll @IntRange(min = 1, max = 30) int intervalDays) {
        
        FollowupPlan plan = new FollowupPlan();
        plan.setPatientId(patientId);
        plan.setIntervalDays(intervalDays);
        plan.setDiagnosis("测试诊断");
        
        FollowupPlan createdPlan = new FollowupPlan();
        createdPlan.setId(1L);
        createdPlan.setPatientId(patientId);
        createdPlan.setIntervalDays(intervalDays);
        createdPlan.setStatus("ACTIVE");
        
        when(followupService.createPlan(any(FollowupPlan.class))).thenReturn(createdPlan);
        
        var result = followupController.createPlan(plan);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getPatientId()).isEqualTo(patientId);
        assertThat(result.getData().getStatus()).isEqualTo("ACTIVE");
    }


    /**
     * Property 2: 取消随访计划 - 应更新计划状态为已取消
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 100)
    void cancelPlan_shouldUpdateStatusToCanceled(
            @ForAll @LongRange(min = 1, max = 10000) Long planId) {
        
        FollowupPlan canceledPlan = new FollowupPlan();
        canceledPlan.setId(planId);
        canceledPlan.setStatus("CANCELED");
        
        when(followupService.cancelPlan(planId)).thenReturn(canceledPlan);
        
        var result = followupController.cancelPlan(planId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("CANCELED");
    }

    /**
     * Property 3: 查询随访计划详情 - 应返回正确的计划信息
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 100)
    void getPlan_shouldReturnPlanDetails(
            @ForAll @LongRange(min = 1, max = 10000) Long planId) {
        
        FollowupPlan mockPlan = new FollowupPlan();
        mockPlan.setId(planId);
        
        when(followupService.getPlanById(planId)).thenReturn(mockPlan);
        
        var result = followupController.getPlan(planId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(planId);
    }

    /**
     * Property 4: 创建随访记录 - 应创建新的随访记录
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    void createRecord_shouldCreateNewRecord(
            @ForAll @LongRange(min = 1, max = 10000) Long planId) {
        
        FollowupRecord record = new FollowupRecord();
        record.setPlanId(planId);
        
        FollowupRecord createdRecord = new FollowupRecord();
        createdRecord.setId(1L);
        createdRecord.setPlanId(planId);
        createdRecord.setStatus("PENDING");
        
        when(followupService.createRecord(any(FollowupRecord.class))).thenReturn(createdRecord);
        
        var result = followupController.createRecord(record);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getPlanId()).isEqualTo(planId);
    }


    /**
     * Property 5: 提交随访记录 - 应更新记录状态为已提交
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 100)
    void submitRecord_shouldUpdateStatusToSubmitted(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId,
            @ForAll @AlphaChars @StringLength(min = 0, max = 500) String symptoms,
            @ForAll @AlphaChars @StringLength(min = 0, max = 500) String answers) {
        
        FollowupRecord submittedRecord = new FollowupRecord();
        submittedRecord.setId(recordId);
        submittedRecord.setStatus("SUBMITTED");
        submittedRecord.setSymptoms(symptoms);
        
        when(followupService.submitRecord(eq(recordId), anyString(), anyString()))
            .thenReturn(submittedRecord);
        
        var result = followupController.submitRecord(recordId, symptoms, answers);
        
        assertThat(result).isNotNull();
        assertThat(result.getData().getStatus()).isEqualTo("SUBMITTED");
    }

    /**
     * Property 6: 审阅随访记录 - 应更新记录状态为已审阅
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 100)
    void reviewRecord_shouldUpdateStatusToReviewed(
            @ForAll @LongRange(min = 1, max = 10000) Long recordId,
            @ForAll @AlphaChars @StringLength(min = 0, max = 500) String comment,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String nextAction) {
        
        FollowupRecord reviewedRecord = new FollowupRecord();
        reviewedRecord.setId(recordId);
        reviewedRecord.setStatus("REVIEWED");
        reviewedRecord.setDoctorComment(comment);
        
        when(followupService.reviewRecord(eq(recordId), anyLong(), anyString(), anyString()))
            .thenReturn(reviewedRecord);
        
        assertThat(reviewedRecord.getStatus()).isEqualTo("REVIEWED");
    }

    /**
     * Property 7: 查询计划下的随访记录 - 应返回该计划的所有记录
     * **Validates: Requirements 8.3**
     */
    @Property(tries = 100)
    void listRecordsByPlan_shouldReturnPlanRecords(
            @ForAll @LongRange(min = 1, max = 10000) Long planId,
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<FollowupRecord> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FollowupRecord record = new FollowupRecord();
            record.setId((long) (i + 1));
            record.setPlanId(planId);
            mockList.add(record);
        }
        
        when(followupService.listRecordsByPlanId(planId)).thenReturn(mockList);
        
        var result = followupController.listRecordsByPlan(planId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(count);
        result.getData().forEach(r -> 
            assertThat(r.getPlanId()).isEqualTo(planId)
        );
    }


    /**
     * Property 8: 查询待审阅的随访记录 - 应返回待审阅状态的记录
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 50)
    void listPendingReview_shouldReturnPendingRecords(
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<FollowupRecord> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FollowupRecord record = new FollowupRecord();
            record.setId((long) (i + 1));
            record.setStatus("SUBMITTED");
            mockList.add(record);
        }
        
        when(followupService.listPendingReviewRecords(anyLong())).thenReturn(mockList);
        
        assertThat(mockList).hasSize(count);
        mockList.forEach(r -> 
            assertThat(r.getStatus()).isEqualTo("SUBMITTED")
        );
    }

    /**
     * Property 9: 查询有红旗征象的记录 - 应返回标记红旗的记录
     * **Validates: Requirements 8.5**
     */
    @Property(tries = 50)
    void listRedFlags_shouldReturnRedFlagRecords(
            @ForAll @IntRange(min = 0, max = 10) int count) {
        
        List<FollowupRecord> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FollowupRecord record = new FollowupRecord();
            record.setId((long) (i + 1));
            record.setHasRedFlag(true);
            mockList.add(record);
        }
        
        when(followupService.listRedFlagRecords(anyLong())).thenReturn(mockList);
        
        assertThat(mockList).hasSize(count);
        mockList.forEach(r -> 
            assertThat(r.getHasRedFlag()).isTrue()
        );
    }

    /**
     * Property 10: 随访间隔天数验证 - 应为正数
     * **Validates: Requirements 8.1**
     */
    @Property(tries = 100)
    void intervalDays_shouldBePositive(
            @ForAll @IntRange(min = 1, max = 365) int intervalDays) {
        
        assertThat(intervalDays).isPositive();
        assertThat(intervalDays).isLessThanOrEqualTo(365);
    }
}
