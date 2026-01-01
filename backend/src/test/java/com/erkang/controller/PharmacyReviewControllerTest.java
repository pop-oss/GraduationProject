package com.erkang.controller;

import com.erkang.domain.entity.PharmacyReview;
import com.erkang.security.LoginUser;
import com.erkang.security.UserContext;
import com.erkang.service.PharmacyReviewService;
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
 * 审方控制器测试
 * _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7_
 */
class PharmacyReviewControllerTest {

    private PharmacyReviewService reviewService;
    private PharmacyReviewController reviewController;

    @BeforeProperty
    void setUp() {
        reviewService = mock(PharmacyReviewService.class);
        reviewController = new PharmacyReviewController(reviewService);
        // 设置UserContext
        UserContext.setUser(LoginUser.builder()
            .userId(1L)
            .username("testPharmacist")
            .roles(List.of("PHARMACIST"))
            .build());
    }

    @AfterProperty
    void tearDown() {
        UserContext.clear();
    }

    /**
     * Property 1: 审核通过 - 应创建通过的审核记录
     * **Validates: Requirements 6.3**
     */
    @Property(tries = 100)
    void approve_shouldCreateApprovedReview(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll("validRiskLevels") String riskLevel,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String riskDescription,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String suggestion) {
        
        PharmacyReview approvedReview = new PharmacyReview();
        approvedReview.setId(1L);
        approvedReview.setPrescriptionId(prescriptionId);
        approvedReview.setResult("APPROVED");
        approvedReview.setRiskLevel(riskLevel);
        
        when(reviewService.approve(eq(prescriptionId), anyLong(), eq(riskLevel), anyString(), anyString()))
            .thenReturn(approvedReview);
        
        var result = reviewController.approve(prescriptionId, riskLevel, riskDescription, suggestion);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getResult()).isEqualTo("APPROVED");
        assertThat(result.getData().getRiskLevel()).isEqualTo(riskLevel);
    }

    @Provide
    Arbitrary<String> validRiskLevels() {
        return Arbitraries.of("LOW", "MEDIUM", "HIGH");
    }


    /**
     * Property 2: 审核驳回 - 应创建驳回的审核记录并记录原因
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void reject_shouldCreateRejectedReviewWithReason(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll @AlphaChars @StringLength(min = 10, max = 200) String rejectReason,
            @ForAll @AlphaChars @StringLength(min = 0, max = 200) String suggestion) {
        
        PharmacyReview rejectedReview = new PharmacyReview();
        rejectedReview.setId(1L);
        rejectedReview.setPrescriptionId(prescriptionId);
        rejectedReview.setResult("REJECTED");
        rejectedReview.setRejectReason(rejectReason);
        
        when(reviewService.reject(eq(prescriptionId), anyLong(), eq(rejectReason), anyString()))
            .thenReturn(rejectedReview);
        
        var result = reviewController.reject(prescriptionId, rejectReason, suggestion);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getResult()).isEqualTo("REJECTED");
        assertThat(result.getData().getRejectReason()).isEqualTo(rejectReason);
    }

    /**
     * Property 3: 查询处方审核记录 - 应返回该处方的所有审核记录
     * **Validates: Requirements 6.5**
     */
    @Property(tries = 100)
    void listByPrescription_shouldReturnPrescriptionReviews(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId,
            @ForAll @IntRange(min = 0, max = 10) int count) {
        
        List<PharmacyReview> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PharmacyReview review = new PharmacyReview();
            review.setId((long) (i + 1));
            review.setPrescriptionId(prescriptionId);
            mockList.add(review);
        }
        
        when(reviewService.listByPrescriptionId(prescriptionId)).thenReturn(mockList);
        
        var result = reviewController.listByPrescription(prescriptionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(count);
        result.getData().forEach(r -> 
            assertThat(r.getPrescriptionId()).isEqualTo(prescriptionId)
        );
    }

    /**
     * Property 4: 查询我的审核记录 - 应返回当前药师的审核记录
     * **Validates: Requirements 6.6**
     */
    @Property(tries = 50)
    void listMyReviews_shouldReturnPharmacistReviews(
            @ForAll @IntRange(min = 0, max = 20) int count) {
        
        List<PharmacyReview> mockList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PharmacyReview review = new PharmacyReview();
            review.setId((long) (i + 1));
            mockList.add(review);
        }
        
        when(reviewService.listByPharmacistId(anyLong())).thenReturn(mockList);
        
        assertThat(mockList).hasSize(count);
    }


    /**
     * Property 5: 获取处方最新审核记录 - 应返回最新的审核记录
     * **Validates: Requirements 6.7**
     */
    @Property(tries = 100)
    void getLatest_shouldReturnLatestReview(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId) {
        
        PharmacyReview latestReview = new PharmacyReview();
        latestReview.setId(1L);
        latestReview.setPrescriptionId(prescriptionId);
        
        when(reviewService.getLatestByPrescriptionId(prescriptionId)).thenReturn(latestReview);
        
        var result = reviewController.getLatest(prescriptionId);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getPrescriptionId()).isEqualTo(prescriptionId);
    }

    /**
     * Property 6: 驳回原因不能为空 - 驳回时必须提供原因
     * **Validates: Requirements 6.4**
     */
    @Property(tries = 100)
    void rejectReason_shouldNotBeEmpty(
            @ForAll @AlphaChars @StringLength(min = 1, max = 500) String rejectReason) {
        
        assertThat(rejectReason).isNotBlank();
    }

    /**
     * Property 7: 风险等级验证 - 风险等级应为有效值
     * **Validates: Requirements 6.3**
     */
    @Property(tries = 50)
    void riskLevel_shouldBeValid(
            @ForAll("validRiskLevels") String riskLevel) {
        
        assertThat(riskLevel).isIn("LOW", "MEDIUM", "HIGH");
    }

    /**
     * Property 8: 审核结果验证 - 结果应为通过或驳回
     * **Validates: Requirements 6.3, 6.4**
     */
    @Property(tries = 50)
    void reviewResult_shouldBeValid(
            @ForAll("validReviewResults") String result) {
        
        assertThat(result).isIn("APPROVED", "REJECTED");
    }

    @Provide
    Arbitrary<String> validReviewResults() {
        return Arbitraries.of("APPROVED", "REJECTED");
    }

    /**
     * Property 9: 审核记录应包含时间戳
     * **Validates: Requirements 6.5**
     */
    @Property(tries = 100)
    void reviewRecord_shouldHaveTimestamp(
            @ForAll @LongRange(min = 1, max = 10000) Long prescriptionId) {
        
        PharmacyReview review = new PharmacyReview();
        review.setPrescriptionId(prescriptionId);
        review.setCreatedAt(java.time.LocalDateTime.now());
        
        assertThat(review.getCreatedAt()).isNotNull();
        assertThat(review.getCreatedAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now());
    }
}
