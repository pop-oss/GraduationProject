package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.entity.PharmacyReview;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.enums.PrescriptionStatus;
import com.erkang.mapper.PharmacyReviewMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审方服务
 * _Requirements: 6.3, 6.4, 6.5, 6.7_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyReviewService {

    private final PharmacyReviewMapper reviewMapper;
    private final PrescriptionService prescriptionService;

    /**
     * 审核通过
     */
    @Transactional
    @Auditable(action = "PHARMACY_APPROVE", module = "pharmacy_review")
    public PharmacyReview approve(Long prescriptionId, Long pharmacistId, 
                                   String riskLevel, String riskDescription, String suggestion) {
        Prescription prescription = prescriptionService.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        if (!PrescriptionStatus.PENDING_REVIEW.getCode().equals(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, "处方不在待审核状态");
        }
        
        // 创建审核记录
        PharmacyReview review = new PharmacyReview();
        review.setPrescriptionId(prescriptionId);
        review.setPharmacistId(pharmacistId);
        review.setResult("APPROVED");
        review.setRiskLevel(riskLevel);
        review.setSuggestion(suggestion);
        review.setReviewedAt(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        // 更新处方状态
        prescriptionService.approve(prescriptionId);
        
        log.info("处方审核通过: prescriptionId={}, pharmacistId={}", prescriptionId, pharmacistId);
        return review;
    }

    /**
     * 审核驳回
     */
    @Transactional
    @Auditable(action = "PHARMACY_REJECT", module = "pharmacy_review")
    public PharmacyReview reject(Long prescriptionId, Long pharmacistId, 
                                  String rejectReason, String suggestion) {
        Prescription prescription = prescriptionService.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        
        if (!PrescriptionStatus.PENDING_REVIEW.getCode().equals(prescription.getStatus())) {
            throw new BusinessException(ErrorCode.PRESCRIPTION_STATUS_INVALID, "处方不在待审核状态");
        }
        
        // 创建审核记录
        PharmacyReview review = new PharmacyReview();
        review.setPrescriptionId(prescriptionId);
        review.setPharmacistId(pharmacistId);
        review.setResult("REJECTED");
        review.setRejectReason(rejectReason);
        review.setSuggestion(suggestion);
        review.setReviewedAt(LocalDateTime.now());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        
        // 更新处方状态
        prescriptionService.reject(prescriptionId, rejectReason);
        
        log.info("处方审核驳回: prescriptionId={}, pharmacistId={}, reason={}", 
                prescriptionId, pharmacistId, rejectReason);
        return review;
    }

    /**
     * 查询处方审核记录
     */
    public List<PharmacyReview> listByPrescriptionId(Long prescriptionId) {
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPrescriptionId, prescriptionId)
               .orderByDesc(PharmacyReview::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    /**
     * 查询药师审核记录
     */
    public List<PharmacyReview> listByPharmacistId(Long pharmacistId) {
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPharmacistId, pharmacistId)
               .orderByDesc(PharmacyReview::getCreatedAt);
        return reviewMapper.selectList(wrapper);
    }

    /**
     * 获取最新审核记录
     */
    public PharmacyReview getLatestByPrescriptionId(Long prescriptionId) {
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPrescriptionId, prescriptionId)
               .orderByDesc(PharmacyReview::getCreatedAt)
               .last("LIMIT 1");
        return reviewMapper.selectOne(wrapper);
    }

    /**
     * 分页查询药师审核记录
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<PharmacyReview> listByPharmacistIdPage(
            Long pharmacistId, int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<PharmacyReview> pageParam = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPharmacistId, pharmacistId)
               .orderByDesc(PharmacyReview::getCreatedAt);
        return reviewMapper.selectPage(pageParam, wrapper);
    }

    /**
     * 统计今日通过数量
     */
    public long countTodayApproved(Long pharmacistId) {
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPharmacistId, pharmacistId)
               .eq(PharmacyReview::getResult, "APPROVED")
               .ge(PharmacyReview::getCreatedAt, java.time.LocalDate.now().atStartOfDay());
        return reviewMapper.selectCount(wrapper);
    }

    /**
     * 统计今日驳回数量
     */
    public long countTodayRejected(Long pharmacistId) {
        LambdaQueryWrapper<PharmacyReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PharmacyReview::getPharmacistId, pharmacistId)
               .eq(PharmacyReview::getResult, "REJECTED")
               .ge(PharmacyReview::getCreatedAt, java.time.LocalDate.now().atStartOfDay());
        return reviewMapper.selectCount(wrapper);
    }
}
