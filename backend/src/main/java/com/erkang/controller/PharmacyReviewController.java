package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.domain.entity.PharmacyReview;
import com.erkang.security.RequireRole;
import com.erkang.security.UserContext;
import com.erkang.service.PharmacyReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审方控制器
 * _Requirements: 6.3, 6.4, 6.5, 6.7_
 */
@RestController
@RequestMapping("/api/pharmacy-reviews")
@RequiredArgsConstructor
public class PharmacyReviewController {

    private final PharmacyReviewService reviewService;

    /**
     * 审核通过
     */
    @PostMapping("/approve")
    @RequireRole({"PHARMACIST"})
    public Result<PharmacyReview> approve(
            @RequestParam Long prescriptionId,
            @RequestParam(defaultValue = "LOW") String riskLevel,
            @RequestParam(required = false) String riskDescription,
            @RequestParam(required = false) String suggestion) {
        
        Long pharmacistId = UserContext.getUserId();
        PharmacyReview review = reviewService.approve(prescriptionId, pharmacistId, 
                riskLevel, riskDescription, suggestion);
        return Result.success(review);
    }

    /**
     * 审核驳回
     */
    @PostMapping("/reject")
    @RequireRole({"PHARMACIST"})
    public Result<PharmacyReview> reject(
            @RequestParam Long prescriptionId,
            @RequestParam String rejectReason,
            @RequestParam(required = false) String suggestion) {
        
        Long pharmacistId = UserContext.getUserId();
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
