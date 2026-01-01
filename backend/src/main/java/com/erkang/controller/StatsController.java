package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.security.Auditable;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 * _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取整体概览统计
     */
    @GetMapping("/overview")
    @RequireRole({"ADMIN"})
    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        Map<String, Object> stats = statsService.getOverviewStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取问诊趋势
     */
    @GetMapping("/consultation-trend")
    @RequireRole({"ADMIN"})
    public Result<List<Map<String, Object>>> getConsultationTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        List<Map<String, Object>> trend = statsService.getConsultationTrend(startDate, endDate);
        return Result.success(trend);
    }

    /**
     * 获取科室统计
     */
    @GetMapping("/departments")
    @RequireRole({"ADMIN"})
    public Result<List<Map<String, Object>>> getDepartmentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        List<Map<String, Object>> stats = statsService.getDepartmentStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 获取医生排行
     */
    @GetMapping("/doctor-ranking")
    @RequireRole({"ADMIN"})
    public Result<List<Map<String, Object>>> getDoctorRanking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        List<Map<String, Object>> ranking = statsService.getDoctorRanking(startDate, endDate, limit);
        return Result.success(ranking);
    }

    /**
     * 获取处方统计
     */
    @GetMapping("/prescriptions")
    @RequireRole({"ADMIN", "PHARMACIST"})
    public Result<Map<String, Object>> getPrescriptionStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();
        Map<String, Object> stats = statsService.getPrescriptionStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 问诊量统计
     */
    @GetMapping("/consultation")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<Map<String, Object>> getConsultationStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statsService.getConsultationStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 医生接诊量统计
     */
    @GetMapping("/doctor/{doctorId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<Map<String, Object>> getDoctorStats(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statsService.getDoctorStats(doctorId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 我的接诊量统计
     */
    @GetMapping("/doctor/my")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT"})
    public Result<Map<String, Object>> getMyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = UserContext.getUserId();
        Map<String, Object> stats = statsService.getDoctorStats(userId, startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 问诊类型分布统计
     */
    @GetMapping("/consultation-type")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "ADMIN"})
    public Result<Map<String, Long>> getConsultationTypeDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Long> distribution = statsService.getConsultationTypeDistribution(startDate, endDate);
        return Result.success(distribution);
    }

    /**
     * 处方审核率统计
     */
    @GetMapping("/prescription-review")
    @RequireRole({"PHARMACIST", "ADMIN"})
    public Result<Map<String, Object>> getPrescriptionReviewStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statsService.getPrescriptionReviewStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 综合统计
     */
    @GetMapping("/overall")
    @RequireRole({"ADMIN"})
    public Result<Map<String, Object>> getOverallStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = statsService.getOverallStats(startDate, endDate);
        return Result.success(stats);
    }

    /**
     * 导出统计数据
     */
    @PostMapping("/export")
    @RequireRole({"ADMIN"})
    @Auditable(action = "EXPORT_STATS", module = "stats")
    public Result<Map<String, Object>> exportStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "JSON") String exportType) {
        Map<String, Object> exportData = statsService.exportStats(startDate, endDate, exportType);
        return Result.success(exportData);
    }
}
