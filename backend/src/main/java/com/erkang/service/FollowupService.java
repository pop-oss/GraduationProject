package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.entity.FollowupPlan;
import com.erkang.domain.entity.FollowupRecord;
import com.erkang.mapper.FollowupPlanMapper;
import com.erkang.mapper.FollowupRecordMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 随访服务
 * _Requirements: 8.1, 8.4, 8.5_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowupService {

    private final FollowupPlanMapper followupPlanMapper;
    private final FollowupRecordMapper followupRecordMapper;

    /**
     * 创建随访计划
     */
    @Transactional
    @Auditable(action = "CREATE_FOLLOWUP_PLAN", module = "followup")
    public FollowupPlan createPlan(FollowupPlan plan) {
        plan.setPlanNo(generatePlanNo());
        plan.setCompletedTimes(0);
        plan.setStatus("ACTIVE");
        
        // 计算下次随访日期
        if (plan.getIntervalDays() != null && plan.getIntervalDays() > 0) {
            plan.setNextFollowupDate(LocalDate.now().plusDays(plan.getIntervalDays()));
        }
        
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        followupPlanMapper.insert(plan);
        
        log.info("创建随访计划: planNo={}, patientId={}", plan.getPlanNo(), plan.getPatientId());
        return plan;
    }

    /**
     * 创建随访记录
     */
    @Transactional
    @Auditable(action = "CREATE_FOLLOWUP_RECORD", module = "followup")
    public FollowupRecord createRecord(FollowupRecord record) {
        FollowupPlan plan = followupPlanMapper.selectById(record.getPlanId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.FOLLOWUP_PLAN_NOT_FOUND);
        }
        
        record.setRecordNo(generateRecordNo());
        record.setPatientId(plan.getPatientId());
        record.setFollowupDate(LocalDate.now());
        record.setStatus("PENDING");
        record.setHasRedFlag(false);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        followupRecordMapper.insert(record);
        
        log.info("创建随访记录: recordNo={}, planId={}", record.getRecordNo(), record.getPlanId());
        return record;
    }

    /**
     * 提交随访记录
     */
    @Transactional
    @Auditable(action = "SUBMIT_FOLLOWUP_RECORD", module = "followup")
    public FollowupRecord submitRecord(Long recordId, String symptoms, String answers) {
        FollowupRecord record = followupRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FOLLOWUP_RECORD_NOT_FOUND);
        }
        
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "随访记录状态不允许此操作");
        }
        
        record.setSymptoms(symptoms);
        record.setAnswers(answers);
        record.setStatus("SUBMITTED");
        record.setSubmittedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        // 检测红旗征象
        boolean hasRedFlag = detectRedFlags(record);
        record.setHasRedFlag(hasRedFlag);
        
        followupRecordMapper.updateById(record);
        
        // 更新计划完成次数
        FollowupPlan plan = followupPlanMapper.selectById(record.getPlanId());
        if (plan != null) {
            plan.setCompletedTimes(plan.getCompletedTimes() + 1);
            
            // 更新下次随访日期
            if (plan.getIntervalDays() != null && plan.getIntervalDays() > 0) {
                plan.setNextFollowupDate(LocalDate.now().plusDays(plan.getIntervalDays()));
            }
            
            // 检查是否完成所有随访
            if (plan.getTotalTimes() != null && plan.getCompletedTimes() >= plan.getTotalTimes()) {
                plan.setStatus("COMPLETED");
            }
            
            plan.setUpdatedAt(LocalDateTime.now());
            followupPlanMapper.updateById(plan);
        }
        
        log.info("提交随访记录: recordNo={}, hasRedFlag={}", record.getRecordNo(), hasRedFlag);
        return record;
    }

    /**
     * 检测红旗征象
     * _Requirements: 8.5_
     */
    private boolean detectRedFlags(FollowupRecord record) {
        if (record.getSymptoms() == null) {
            return false;
        }
        
        String symptoms = record.getSymptoms().toLowerCase();
        
        // 耳鼻喉科常见红旗征象关键词
        String[] redFlagKeywords = {
            "剧烈疼痛", "高烧", "出血", "呼吸困难", "吞咽困难",
            "听力急剧下降", "面瘫", "眩晕", "恶心呕吐", "意识模糊",
            "severe pain", "high fever", "bleeding", "difficulty breathing"
        };
        
        for (String keyword : redFlagKeywords) {
            if (symptoms.contains(keyword.toLowerCase())) {
                record.setRedFlagDetail("检测到红旗征象: " + keyword);
                return true;
            }
        }
        
        return false;
    }

    /**
     * 医生审阅随访记录
     */
    @Transactional
    @Auditable(action = "REVIEW_FOLLOWUP_RECORD", module = "followup")
    public FollowupRecord reviewRecord(Long recordId, Long reviewerId, String comment, String nextAction) {
        FollowupRecord record = followupRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.FOLLOWUP_RECORD_NOT_FOUND);
        }
        
        if (!"SUBMITTED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "随访记录状态不允许此操作");
        }
        
        record.setReviewerId(reviewerId);
        record.setDoctorComment(comment);
        record.setNextAction(nextAction);
        record.setStatus("REVIEWED");
        record.setReviewedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        followupRecordMapper.updateById(record);
        
        // 如果有红旗征象，触发提前复诊提醒
        if (Boolean.TRUE.equals(record.getHasRedFlag())) {
            triggerEarlyRevisitReminder(record);
        }
        
        log.info("审阅随访记录: recordNo={}, reviewerId={}", record.getRecordNo(), reviewerId);
        return record;
    }

    /**
     * 触发提前复诊提醒
     * _Requirements: 8.5_
     */
    private void triggerEarlyRevisitReminder(FollowupRecord record) {
        log.warn("红旗征象提醒: recordNo={}, detail={}", record.getRecordNo(), record.getRedFlagDetail());
        // TODO: 通过WebSocket发送提醒通知
    }

    /**
     * 取消随访计划
     */
    @Transactional
    @Auditable(action = "CANCEL_FOLLOWUP_PLAN", module = "followup")
    public FollowupPlan cancelPlan(Long planId) {
        FollowupPlan plan = followupPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(ErrorCode.FOLLOWUP_PLAN_NOT_FOUND);
        }
        
        if ("COMPLETED".equals(plan.getStatus()) || "CANCELED".equals(plan.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "随访计划状态不允许此操作");
        }
        
        plan.setStatus("CANCELED");
        plan.setUpdatedAt(LocalDateTime.now());
        followupPlanMapper.updateById(plan);
        
        log.info("取消随访计划: planNo={}", plan.getPlanNo());
        return plan;
    }

    /**
     * 查询随访计划详情
     */
    public FollowupPlan getPlanById(Long planId) {
        return followupPlanMapper.selectById(planId);
    }

    /**
     * 查询随访记录详情
     */
    public FollowupRecord getRecordById(Long recordId) {
        return followupRecordMapper.selectById(recordId);
    }

    /**
     * 查询患者随访计划
     */
    public List<FollowupPlan> listPlansByPatientId(Long patientId) {
        LambdaQueryWrapper<FollowupPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupPlan::getPatientId, patientId)
               .orderByDesc(FollowupPlan::getCreatedAt);
        return followupPlanMapper.selectList(wrapper);
    }

    /**
     * 查询医生创建的随访计划
     */
    public List<FollowupPlan> listPlansByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowupPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupPlan::getDoctorId, doctorId)
               .orderByDesc(FollowupPlan::getCreatedAt);
        return followupPlanMapper.selectList(wrapper);
    }

    /**
     * 查询计划下的随访记录
     */
    public List<FollowupRecord> listRecordsByPlanId(Long planId) {
        LambdaQueryWrapper<FollowupRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupRecord::getPlanId, planId)
               .orderByDesc(FollowupRecord::getFollowupDate);
        return followupRecordMapper.selectList(wrapper);
    }

    /**
     * 查询待审阅的随访记录
     */
    public List<FollowupRecord> listPendingReviewRecords(Long doctorId) {
        // 先查询医生的计划
        LambdaQueryWrapper<FollowupPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(FollowupPlan::getDoctorId, doctorId);
        List<FollowupPlan> plans = followupPlanMapper.selectList(planWrapper);
        
        if (plans.isEmpty()) {
            return List.of();
        }
        
        List<Long> planIds = plans.stream().map(FollowupPlan::getId).toList();
        
        LambdaQueryWrapper<FollowupRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(FollowupRecord::getPlanId, planIds)
                     .eq(FollowupRecord::getStatus, "SUBMITTED")
                     .orderByDesc(FollowupRecord::getSubmittedAt);
        return followupRecordMapper.selectList(recordWrapper);
    }

    /**
     * 查询有红旗征象的记录
     */
    public List<FollowupRecord> listRedFlagRecords(Long doctorId) {
        LambdaQueryWrapper<FollowupPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(FollowupPlan::getDoctorId, doctorId);
        List<FollowupPlan> plans = followupPlanMapper.selectList(planWrapper);
        
        if (plans.isEmpty()) {
            return List.of();
        }
        
        List<Long> planIds = plans.stream().map(FollowupPlan::getId).toList();
        
        LambdaQueryWrapper<FollowupRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(FollowupRecord::getPlanId, planIds)
                     .eq(FollowupRecord::getHasRedFlag, true)
                     .orderByDesc(FollowupRecord::getSubmittedAt);
        return followupRecordMapper.selectList(recordWrapper);
    }

    private String generatePlanNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FP" + date + uuid;
    }

    private String generateRecordNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FR" + date + uuid;
    }
}
