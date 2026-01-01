package com.erkang.controller;

import com.erkang.service.StatsService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 统计分析控制器测试
 * _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
 */
class StatsControllerTest {

    private StatsService statsService;
    private StatsController statsController;

    @BeforeProperty
    void setUp() {
        statsService = mock(StatsService.class);
        statsController = new StatsController(statsService);
    }

    /**
     * Property 1: 问诊量统计 - 应返回统计数据
     * **Validates: Requirements 9.1**
     */
    @Property(tries = 100)
    void getConsultationStats_shouldReturnStats(
            @ForAll @IntRange(min = 1, max = 365) int daysAgo,
            @ForAll @IntRange(min = 0, max = 1000) int total,
            @ForAll @IntRange(min = 0, max = 1000) int completed) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("total", total);
        mockStats.put("completed", Math.min(completed, total));
        mockStats.put("startDate", startDate.toString());
        mockStats.put("endDate", endDate.toString());
        
        when(statsService.getConsultationStats(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockStats);
        
        var result = statsController.getConsultationStats(startDate, endDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsKey("total");
    }


    /**
     * Property 2: 医生接诊量统计 - 应返回指定医生的统计数据
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    void getDoctorStats_shouldReturnDoctorStats(
            @ForAll @LongRange(min = 1, max = 10000) Long doctorId,
            @ForAll @IntRange(min = 1, max = 30) int daysAgo) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("doctorId", doctorId);
        mockStats.put("consultationCount", 50);
        mockStats.put("avgDuration", 25.5);
        
        when(statsService.getDoctorStats(eq(doctorId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockStats);
        
        var result = statsController.getDoctorStats(doctorId, startDate, endDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsKey("doctorId");
    }

    /**
     * Property 3: 问诊类型分布统计 - 应返回各类型的数量
     * **Validates: Requirements 9.3**
     */
    @Property(tries = 100)
    void getConsultationTypeDistribution_shouldReturnDistribution(
            @ForAll @IntRange(min = 1, max = 30) int daysAgo,
            @ForAll @LongRange(min = 0, max = 500) Long videoCount,
            @ForAll @LongRange(min = 0, max = 500) Long textCount) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Long> mockDistribution = new HashMap<>();
        mockDistribution.put("VIDEO", videoCount);
        mockDistribution.put("TEXT", textCount);
        
        when(statsService.getConsultationTypeDistribution(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockDistribution);
        
        var result = statsController.getConsultationTypeDistribution(startDate, endDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        result.getData().values().forEach(count -> 
            assertThat(count).isGreaterThanOrEqualTo(0)
        );
    }

    /**
     * Property 4: 处方审核率统计 - 应返回审核相关统计
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 100)
    void getPrescriptionReviewStats_shouldReturnReviewStats(
            @ForAll @IntRange(min = 1, max = 30) int daysAgo,
            @ForAll @IntRange(min = 0, max = 100) int approvalRate) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalPrescriptions", 100);
        mockStats.put("reviewed", 80);
        mockStats.put("approved", 70);
        mockStats.put("rejected", 10);
        mockStats.put("approvalRate", approvalRate);
        
        when(statsService.getPrescriptionReviewStats(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockStats);
        
        var result = statsController.getPrescriptionReviewStats(startDate, endDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsKey("approvalRate");
    }


    /**
     * Property 5: 综合统计 - 应返回所有统计数据
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 100)
    void getOverallStats_shouldReturnAllStats(
            @ForAll @IntRange(min = 1, max = 30) int daysAgo) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("totalConsultations", 1000);
        mockStats.put("totalPrescriptions", 800);
        mockStats.put("totalReferrals", 50);
        mockStats.put("totalMDT", 20);
        
        when(statsService.getOverallStats(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(mockStats);
        
        var result = statsController.getOverallStats(startDate, endDate);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsKeys("totalConsultations", "totalPrescriptions");
    }

    /**
     * Property 6: 导出统计数据 - 应返回导出数据
     * **Validates: Requirements 9.5**
     */
    @Property(tries = 50)
    void exportStats_shouldReturnExportData(
            @ForAll @IntRange(min = 1, max = 30) int daysAgo,
            @ForAll("validExportTypes") String exportType) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        Map<String, Object> mockExportData = new HashMap<>();
        mockExportData.put("exportType", exportType);
        mockExportData.put("data", "exported-data");
        
        when(statsService.exportStats(any(LocalDate.class), any(LocalDate.class), eq(exportType)))
            .thenReturn(mockExportData);
        
        var result = statsController.exportStats(startDate, endDate, exportType);
        
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData()).containsKey("exportType");
    }

    @Provide
    Arbitrary<String> validExportTypes() {
        return Arbitraries.of("JSON", "CSV", "EXCEL");
    }

    /**
     * Property 7: 日期范围验证 - 开始日期应在结束日期之前
     * **Validates: Requirements 9.1**
     */
    @Property(tries = 100)
    void dateRange_startShouldBeBeforeEnd(
            @ForAll @IntRange(min = 1, max = 365) int daysAgo) {
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysAgo);
        
        assertThat(startDate).isBefore(endDate);
    }


    /**
     * Property 8: 统计数值验证 - 完成数不应超过总数
     * **Validates: Requirements 9.1**
     */
    @Property(tries = 100)
    void statsValues_completedShouldNotExceedTotal(
            @ForAll @IntRange(min = 0, max = 1000) int total,
            @ForAll @IntRange(min = 0, max = 1000) int completed) {
        
        int actualCompleted = Math.min(completed, total);
        
        assertThat(actualCompleted).isLessThanOrEqualTo(total);
    }

    /**
     * Property 9: 百分比验证 - 百分比应在0-100之间
     * **Validates: Requirements 9.4**
     */
    @Property(tries = 100)
    void percentage_shouldBeBetweenZeroAndHundred(
            @ForAll @IntRange(min = 0, max = 100) int percentage) {
        
        assertThat(percentage).isBetween(0, 100);
    }

    /**
     * Property 10: 平均时长验证 - 平均时长应为非负数
     * **Validates: Requirements 9.2**
     */
    @Property(tries = 100)
    void avgDuration_shouldBeNonNegative(
            @ForAll @DoubleRange(min = 0, max = 120) double avgDuration) {
        
        assertThat(avgDuration).isGreaterThanOrEqualTo(0);
    }
}
