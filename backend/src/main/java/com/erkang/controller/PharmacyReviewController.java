package com.erkang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.common.Result;
import com.erkang.domain.dto.PrescriptionReviewDTO;
import com.erkang.domain.entity.PharmacyReview;
import com.erkang.domain.entity.Prescription;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.PharmacyReviewService;
import com.erkang.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审方控制器
 * _Requirements: 6.3, 6.4, 6.5, 6.7_
 */
@RestController
@RequestMapping("/api/pharmacy-reviews")
@RequiredArgsConstructor
public class PharmacyReviewController {

    private final PharmacyReviewService reviewService;
    private final PrescriptionService prescriptionService;

    /**
     * 获取待审处方列表
     */
    @GetMapping("/pending")
    @RequireRole({"PHARMACIST"})
    public Result<Page<PrescriptionReviewDTO>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PrescriptionReviewDTO> result = prescriptionService.listPendingReviewDTOPage(page, size);
        return Result.success(result);
    }

    /**
     * 获取审方统计
     */
    @GetMapping("/stats")
    @RequireRole({"PHARMACIST"})
    public Result<Map<String, Object>> getStats() {
        Long pharmacistId = UserContext.getUserId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("pending", prescriptionService.countPendingReview());
        stats.put("approvedToday", reviewService.countTodayApproved(pharmacistId));
        stats.put("rejectedToday", reviewService.countTodayRejected(pharmacistId));
        return Result.success(stats);
    }

    /**
     * 获取处方详情（用于审方）
     */
    @GetMapping("/{id}")
    @RequireRole({"PHARMACIST"})
    public Result<PrescriptionReviewDTO> getDetail(@PathVariable Long id) {
        PrescriptionReviewDTO detail = prescriptionService.getReviewDetailById(id);
        return Result.success(detail);
    }

    /**
     * 获取审方历史
     */
    @GetMapping("/history")
    @RequireRole({"PHARMACIST"})
    public Result<Page<PrescriptionReviewDTO>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long pharmacistId = UserContext.getUserId();
        Page<PrescriptionReviewDTO> result = prescriptionService.listReviewHistoryByPharmacist(
                pharmacistId, page, size, status, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 审核通过
     */
    @PutMapping("/{prescriptionId}/approve")
    @RequireRole({"PHARMACIST"})
    public Result<PharmacyReview> approve(
            @PathVariable Long prescriptionId,
            @RequestBody(required = false) Map<String, String> body) {
        
        Long pharmacistId = UserContext.getUserId();
        String riskLevel = body != null ? body.getOrDefault("riskLevel", "LOW") : "LOW";
        String riskDescription = body != null ? body.get("riskDescription") : null;
        String suggestion = body != null ? body.get("comment") : null;
        
        PharmacyReview review = reviewService.approve(prescriptionId, pharmacistId, 
                riskLevel, riskDescription, suggestion);
        return Result.success(review);
    }

    /**
     * 审核驳回
     */
    @PutMapping("/{prescriptionId}/reject")
    @RequireRole({"PHARMACIST"})
    public Result<PharmacyReview> reject(
            @PathVariable Long prescriptionId,
            @RequestBody Map<String, String> body) {
        
        Long pharmacistId = UserContext.getUserId();
        String rejectReason = body.get("reason");
        String suggestion = body.get("suggestion");
        
        if (rejectReason == null || rejectReason.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "驳回原因不能为空");
        }
        
        PharmacyReview review = reviewService.reject(prescriptionId, pharmacistId, 
                rejectReason, suggestion);
        return Result.success(review);
    }

    /**
     * 查询处方审核记录
     */
    @GetMapping("/prescription/{prescriptionId}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<List<PharmacyReview>> listByPrescription(@PathVariable Long prescriptionId) {
        List<PharmacyReview> reviews = reviewService.listByPrescriptionId(prescriptionId);
        return Result.success(reviews);
    }

    /**
     * 查询我的审核记录
     */
    @GetMapping("/my-reviews")
    @RequireRole({"PHARMACIST"})
    public Result<List<PharmacyReview>> listMyReviews() {
        Long pharmacistId = UserContext.getUserId();
        List<PharmacyReview> reviews = reviewService.listByPharmacistId(pharmacistId);
        return Result.success(reviews);
    }

    /**
     * 获取处方最新审核记录
     */
    @GetMapping("/prescription/{prescriptionId}/latest")
    @RequireRole({"PATIENT", "DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<PharmacyReview> getLatest(@PathVariable Long prescriptionId) {
        PharmacyReview review = reviewService.getLatestByPrescriptionId(prescriptionId);
        return Result.success(review);
    }
}
