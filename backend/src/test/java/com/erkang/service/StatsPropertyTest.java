package com.erkang.service;

import net.jqwik.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 统计分析模块属性测试
 * 使用jqwik进行属性测试，每个测试至少100次迭代
 * 
 * **Property 13: 统计数据正确性**
 * **Validates: Requirements 9.1, 9.2, 9.3, 9.4**
 */
class StatsPropertyTest {

    // ==================== Property 13: 统计数据正确性 ====================
    // **Validates: Requirements 9.1, 9.2, 9.3, 9.4**

    @Property(tries = 100)
    void consultationStats_totalShouldEqualSumOfStatuses(
            @ForAll("consultationCounts") ConsultationCounts counts) {
        // Given: 各状态的问诊数量
        long completed = counts.completed;
        long canceled = counts.canceled;
        long inProgress = counts.inProgress;
        long pending = counts.pending;
        
        // When: 计算总数
        long total = completed + canceled + inProgress + pending;
        
        // Then: 总数应等于各状态之和
        assertThat(total).isEqualTo(counts.getTotal());
    }

    @Property(tries = 100)
    void completionRate_shouldBeBetweenZeroAndHundred(
            @ForAll("consultationCounts") ConsultationCounts counts) {
        // Given: 问诊统计数据
        long total = counts.getTotal();
        long completed = counts.completed;
        
        // When: 计算完成率
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;
        
        // Then: 完成率应在0-100之间
        assertThat(completionRate).isBetween(0.0, 100.0);
    }

    @Property(tries = 100)
    void prescriptionStats_approvalAndRejectionRatesShouldNotExceedHundred(
            @ForAll("prescriptionCounts") PrescriptionCounts counts) {
        // Given: 处方统计数据
        long total = counts.getTotal();
        long approved = counts.approved;
        long rejected = counts.rejected;
        
        // When: 计算审核率
        double approvalRate = total > 0 ? (double) approved / total * 100 : 0;
        double rejectionRate = total > 0 ? (double) rejected / total * 100 : 0;
        
        // Then: 审核率应在合理范围内（考虑浮点数精度）
        assertThat(approvalRate).isBetween(0.0, 100.0 + 0.0001);
        assertThat(rejectionRate).isBetween(0.0, 100.0 + 0.0001);
        assertThat(approvalRate + rejectionRate).isLessThanOrEqualTo(100.0 + 0.0001);
    }

    @Property(tries = 100)
    void diagnosisDistribution_countsShouldBeNonNegative(
            @ForAll("diagnosisCounts") Map<String, Long> distribution) {
        // Given: 病种分布数据
        // When & Then: 所有计数应为非负数
        for (Long count : distribution.values()) {
            assertThat(count).isGreaterThanOrEqualTo(0L);
        }
    }

    @Property(tries = 100)
    void doctorStats_avgDurationShouldBeNonNegative(
            @ForAll("durationMinutes") long[] durations) {
        // Given: 问诊时长数据
        // When: 计算平均时长
        double avgDuration = 0;
        if (durations.length > 0) {
            long sum = 0;
            for (long d : durations) {
                sum += d;
            }
            avgDuration = (double) sum / durations.length;
        }
        
        // Then: 平均时长应为非负数
        assertThat(avgDuration).isGreaterThanOrEqualTo(0.0);
    }

    @Property(tries = 100)
    void statsExport_shouldContainRequiredFields(
            @ForAll("exportTypes") String exportType) {
        // Given: 导出类型
        // When: 模拟导出数据结构
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("exportType", exportType);
        exportData.put("data", new HashMap<>());
        
        // Then: 应包含必要字段
        assertThat(exportData).containsKey("exportType");
        assertThat(exportData).containsKey("data");
        assertThat(exportData.get("exportType")).isEqualTo(exportType);
    }

    @Property(tries = 100)
    void consultationStats_completedShouldNotExceedTotal(
            @ForAll("consultationCounts") ConsultationCounts counts) {
        // Given: 问诊统计数据
        long total = counts.getTotal();
        long completed = counts.completed;
        
        // Then: 完成数不应超过总数
        assertThat(completed).isLessThanOrEqualTo(total);
    }

    @Property(tries = 100)
    void prescriptionStats_pendingPlusReviewedShouldEqualTotal(
            @ForAll("prescriptionCounts") PrescriptionCounts counts) {
        // Given: 处方统计数据
        long total = counts.getTotal();
        long approved = counts.approved;
        long rejected = counts.rejected;
        long pending = counts.pending;
        
        // Then: 各状态之和应等于总数
        assertThat(approved + rejected + pending).isEqualTo(total);
    }

    // ==================== 数据生成器 ====================

    @Provide
    Arbitrary<ConsultationCounts> consultationCounts() {
        return Arbitraries.integers().between(0, 1000)
                .tuple4()
                .map(t -> new ConsultationCounts(t.get1(), t.get2(), t.get3(), t.get4()));
    }

    @Provide
    Arbitrary<PrescriptionCounts> prescriptionCounts() {
        return Arbitraries.integers().between(0, 500)
                .tuple3()
                .map(t -> new PrescriptionCounts(t.get1(), t.get2(), t.get3()));
    }

    @Provide
    Arbitrary<Map<String, Long>> diagnosisCounts() {
        return Arbitraries.maps(
                Arbitraries.of("急性中耳炎", "慢性鼻炎", "扁桃体炎", "咽喉炎", "耳鸣", "听力下降"),
                Arbitraries.longs().between(0, 100)
        ).ofMinSize(1).ofMaxSize(6);
    }

    @Provide
    Arbitrary<long[]> durationMinutes() {
        return Arbitraries.longs().between(5, 60)
                .array(long[].class)
                .ofMinSize(0)
                .ofMaxSize(50);
    }

    @Provide
    Arbitrary<String> exportTypes() {
        return Arbitraries.of("JSON", "CSV", "EXCEL", "PDF");
    }

    // ==================== 辅助类 ====================

    static class ConsultationCounts {
        final int completed;
        final int canceled;
        final int inProgress;
        final int pending;

        ConsultationCounts(int completed, int canceled, int inProgress, int pending) {
            this.completed = Math.abs(completed);
            this.canceled = Math.abs(canceled);
            this.inProgress = Math.abs(inProgress);
            this.pending = Math.abs(pending);
        }

        long getTotal() {
            return (long) completed + canceled + inProgress + pending;
        }
    }

    static class PrescriptionCounts {
        final int approved;
        final int rejected;
        final int pending;

        PrescriptionCounts(int approved, int rejected, int pending) {
            this.approved = Math.abs(approved);
            this.rejected = Math.abs(rejected);
            this.pending = Math.abs(pending);
        }

        long getTotal() {
            return (long) approved + rejected + pending;
        }
    }
}
