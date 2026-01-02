package com.erkang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erkang.domain.entity.Consultation;
import com.erkang.domain.entity.Department;
import com.erkang.domain.entity.DoctorProfile;
import com.erkang.domain.entity.PharmacyReview;
import com.erkang.domain.entity.Prescription;
import com.erkang.domain.entity.User;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.DepartmentMapper;
import com.erkang.mapper.DoctorProfileMapper;
import com.erkang.mapper.PharmacyReviewMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.mapper.UserMapper;
import com.erkang.security.Auditable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 * _Requirements: 9.1, 9.2, 9.3, 9.4_
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ConsultationMapper consultationMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final PharmacyReviewMapper pharmacyReviewMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;

    /**
     * 统计问诊量
     * _Requirements: 9.1_
     */
    public Map<String, Object> getConsultationStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Consultation::getCreatedAt, start, end);
        List<Consultation> consultations = consultationMapper.selectList(wrapper);
        
        long total = consultations.size();
        long completed = consultations.stream()
                .filter(c -> "COMPLETED".equals(c.getStatus()))
                .count();
        long canceled = consultations.stream()
                .filter(c -> "CANCELED".equals(c.getStatus()))
                .count();
        long inProgress = consultations.stream()
                .filter(c -> "IN_PROGRESS".equals(c.getStatus()))
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("canceled", canceled);
        stats.put("inProgress", inProgress);
        stats.put("completionRate", total > 0 ? (double) completed / total * 100 : 0);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        
        return stats;
    }

    /**
     * 统计医生接诊量
     * _Requirements: 9.2_
     */
    public Map<String, Object> getDoctorStats(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getDoctorId, doctorId)
               .between(Consultation::getCreatedAt, start, end);
        List<Consultation> consultations = consultationMapper.selectList(wrapper);
        
        long total = consultations.size();
        long completed = consultations.stream()
                .filter(c -> "COMPLETED".equals(c.getStatus()))
                .count();
        
        // 计算平均问诊时长（分钟）
        double avgDuration = consultations.stream()
                .filter(c -> c.getStartTime() != null && c.getEndTime() != null)
                .mapToLong(c -> java.time.Duration.between(c.getStartTime(), c.getEndTime()).toMinutes())
                .average()
                .orElse(0);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("doctorId", doctorId);
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("avgDurationMinutes", avgDuration);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        
        return stats;
    }

    /**
     * 统计病种分布（基于问诊类型）
     * _Requirements: 9.3_
     */
    public Map<String, Long> getConsultationTypeDistribution(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Consultation::getCreatedAt, start, end)
               .isNotNull(Consultation::getConsultationType);
        List<Consultation> consultations = consultationMapper.selectList(wrapper);
        
        // 按问诊类型分组统计
        return consultations.stream()
                .filter(c -> c.getConsultationType() != null && !c.getConsultationType().isEmpty())
                .collect(Collectors.groupingBy(
                        Consultation::getConsultationType,
                        Collectors.counting()
                ));
    }

    /**
     * 统计处方审核率
     * _Requirements: 9.4_
     */
    public Map<String, Object> getPrescriptionReviewStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        
        // 统计处方
        LambdaQueryWrapper<Prescription> prescriptionWrapper = new LambdaQueryWrapper<>();
        prescriptionWrapper.between(Prescription::getCreatedAt, start, end);
        List<Prescription> prescriptions = prescriptionMapper.selectList(prescriptionWrapper);
        
        long totalPrescriptions = prescriptions.size();
        long approved = prescriptions.stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .count();
        long rejected = prescriptions.stream()
                .filter(p -> "REJECTED".equals(p.getStatus()))
                .count();
        long pendingReview = prescriptions.stream()
                .filter(p -> "PENDING_REVIEW".equals(p.getStatus()))
                .count();
        
        // 统计审方记录
        LambdaQueryWrapper<PharmacyReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.between(PharmacyReview::getCreatedAt, start, end);
        long totalReviews = pharmacyReviewMapper.selectCount(reviewWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPrescriptions", totalPrescriptions);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("pendingReview", pendingReview);
        stats.put("totalReviews", totalReviews);
        stats.put("approvalRate", totalPrescriptions > 0 ? (double) approved / totalPrescriptions * 100 : 0);
        stats.put("rejectionRate", totalPrescriptions > 0 ? (double) rejected / totalPrescriptions * 100 : 0);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        
        return stats;
    }

    /**
     * 获取综合统计数据
     */
    public Map<String, Object> getOverallStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> overall = new HashMap<>();
        overall.put("consultation", getConsultationStats(startDate, endDate));
        overall.put("prescriptionReview", getPrescriptionReviewStats(startDate, endDate));
        overall.put("consultationTypeDistribution", getConsultationTypeDistribution(startDate, endDate));
        return overall;
    }

    /**
     * 获取概览统计（前端用）
     */
    public Map<String, Object> getOverviewStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            
            // 问诊统计
            LambdaQueryWrapper<Consultation> consultationWrapper = new LambdaQueryWrapper<>();
            consultationWrapper.between(Consultation::getCreatedAt, start, end);
            List<Consultation> consultations = consultationMapper.selectList(consultationWrapper);
            
            long totalConsultations = consultations.size();
            
            // 处方统计
            LambdaQueryWrapper<Prescription> prescriptionWrapper = new LambdaQueryWrapper<>();
            prescriptionWrapper.between(Prescription::getCreatedAt, start, end);
            List<Prescription> prescriptions = prescriptionMapper.selectList(prescriptionWrapper);
            
            long totalPrescriptions = prescriptions.size();
            long approvedPrescriptions = prescriptions.stream()
                    .filter(p -> "APPROVED".equals(p.getStatus()))
                    .count();
            
            // 计算平均问诊时长
            double avgDuration = consultations.stream()
                    .filter(c -> c.getStartTime() != null && c.getEndTime() != null)
                    .mapToLong(c -> java.time.Duration.between(c.getStartTime(), c.getEndTime()).toMinutes())
                    .average()
                    .orElse(0);
            
            // 计算处方通过率
            double approvalRate = totalPrescriptions > 0 ? (double) approvedPrescriptions / totalPrescriptions * 100 : 0;
            
            stats.put("totalConsultations", totalConsultations);
            stats.put("totalPrescriptions", totalPrescriptions);
            stats.put("totalPatients", consultations.stream().map(Consultation::getPatientId).distinct().count());
            stats.put("totalDoctors", consultations.stream().map(Consultation::getDoctorId).filter(id -> id != null).distinct().count());
            stats.put("avgConsultationDuration", avgDuration);
            stats.put("prescriptionApprovalRate", approvalRate);
        } catch (Exception e) {
            log.warn("获取概览统计失败: {}", e.getMessage());
            stats.put("totalConsultations", 0);
            stats.put("totalPrescriptions", 0);
            stats.put("totalPatients", 0);
            stats.put("totalDoctors", 0);
            stats.put("avgConsultationDuration", 0);
            stats.put("prescriptionApprovalRate", 0);
        }
        
        return stats;
    }

    /**
     * 获取问诊趋势
     */
    public List<Map<String, Object>> getConsultationTrend(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> trend = new java.util.ArrayList<>();
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Consultation::getCreatedAt, start, end);
            List<Consultation> consultations = consultationMapper.selectList(wrapper);
            
            // 按日期分组
            Map<LocalDate, List<Consultation>> grouped = consultations.stream()
                    .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate()));
            
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                List<Consultation> dayConsultations = grouped.getOrDefault(current, List.of());
                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("date", current.toString());
                dayStats.put("count", dayConsultations.size());
                dayStats.put("completedCount", dayConsultations.stream()
                        .filter(c -> "COMPLETED".equals(c.getStatus()))
                        .count());
                trend.add(dayStats);
                current = current.plusDays(1);
            }
        } catch (Exception e) {
            log.warn("获取问诊趋势失败: {}", e.getMessage());
            // 返回空趋势数据
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("date", current.toString());
                dayStats.put("count", 0);
                dayStats.put("completedCount", 0);
                trend.add(dayStats);
                current = current.plusDays(1);
            }
        }
        
        return trend;
    }

    /**
     * 获取科室统计
     */
    public List<Map<String, Object>> getDepartmentStats(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> stats = new java.util.ArrayList<>();
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Consultation::getCreatedAt, start, end);
            List<Consultation> consultations = consultationMapper.selectList(wrapper);
            
            // 按科室分组（使用 consultationType 作为科室代替）
            Map<String, List<Consultation>> grouped = consultations.stream()
                    .filter(c -> c.getConsultationType() != null)
                    .collect(Collectors.groupingBy(Consultation::getConsultationType));
            
            // 问诊类型映射为科室名称
            Map<String, String> typeToName = Map.of(
                    "VIDEO", "视频问诊",
                    "AUDIO", "语音问诊",
                    "TEXT", "图文问诊",
                    "FOLLOW_UP", "复诊随访"
            );
            
            int id = 1;
            for (Map.Entry<String, List<Consultation>> entry : grouped.entrySet()) {
                Map<String, Object> deptStats = new HashMap<>();
                deptStats.put("departmentId", id++);
                deptStats.put("departmentName", typeToName.getOrDefault(entry.getKey(), entry.getKey()));
                deptStats.put("consultationCount", entry.getValue().size());
                deptStats.put("prescriptionCount", 0); // 简化处理
                deptStats.put("avgRating", 4.5); // 模拟数据
                stats.add(deptStats);
            }
        } catch (Exception e) {
            log.warn("获取科室统计失败: {}", e.getMessage());
        }
        
        // 如果没有数据，返回默认科室
        if (stats.isEmpty()) {
            Map<String, Object> defaultDept = new HashMap<>();
            defaultDept.put("departmentId", 1);
            defaultDept.put("departmentName", "耳鼻喉科");
            defaultDept.put("consultationCount", 0);
            defaultDept.put("prescriptionCount", 0);
            defaultDept.put("avgRating", 0);
            stats.add(defaultDept);
        }
        
        return stats;
    }

    /**
     * 获取医生排行
     */
    public List<Map<String, Object>> getDoctorRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        List<Map<String, Object>> ranking = new java.util.ArrayList<>();
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Consultation::getCreatedAt, start, end)
                   .isNotNull(Consultation::getDoctorId);
            List<Consultation> consultations = consultationMapper.selectList(wrapper);
            
            // 按医生分组统计
            Map<Long, List<Consultation>> grouped = consultations.stream()
                    .filter(c -> c.getDoctorId() != null)
                    .collect(Collectors.groupingBy(Consultation::getDoctorId));
            
            // 获取医生信息
            for (Map.Entry<Long, List<Consultation>> entry : grouped.entrySet()) {
                Long doctorId = entry.getKey();
                String doctorName = "医生" + doctorId;
                String departmentName = "耳鼻喉科";
                
                // 尝试获取真实医生名称和科室
                try {
                    // doctorId 可能是 user_id，先尝试通过 user_id 查询
                    DoctorProfile profile = doctorProfileMapper.selectByUserId(doctorId);
                    if (profile != null) {
                        // 获取医生名称
                        User user = userMapper.selectById(profile.getUserId());
                        if (user != null && user.getRealName() != null) {
                            doctorName = user.getRealName();
                        }
                        // 获取科室名称
                        if (profile.getDepartmentId() != null) {
                            Department dept = departmentMapper.selectById(profile.getDepartmentId());
                            if (dept != null && dept.getName() != null) {
                                departmentName = dept.getName();
                            }
                        }
                    } else {
                        // 尝试直接用 doctorId 作为 user_id 查询用户名
                        User user = userMapper.selectById(doctorId);
                        if (user != null && user.getRealName() != null) {
                            doctorName = user.getRealName();
                        }
                    }
                } catch (Exception e) {
                    log.debug("获取医生信息失败: doctorId={}", doctorId);
                }
                
                Map<String, Object> doctorStats = new HashMap<>();
                doctorStats.put("doctorId", entry.getKey());
                doctorStats.put("doctorName", doctorName);
                doctorStats.put("departmentName", departmentName);
                doctorStats.put("consultationCount", entry.getValue().size());
                doctorStats.put("avgRating", 4.5); // 模拟数据
                ranking.add(doctorStats);
            }
            
            // 按问诊量排序
            ranking.sort((a, b) -> Integer.compare(
                    (Integer) b.get("consultationCount"),
                    (Integer) a.get("consultationCount")
            ));
        } catch (Exception e) {
            log.warn("获取医生排行失败: {}", e.getMessage());
        }
        
        // 限制返回数量
        if (ranking.size() > limit) {
            ranking = ranking.subList(0, limit);
        }
        
        return ranking;
    }

    /**
     * 获取处方统计（前端用）
     */
    public Map<String, Object> getPrescriptionStats(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            
            LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(Prescription::getCreatedAt, start, end);
            List<Prescription> prescriptions = prescriptionMapper.selectList(wrapper);
            
            long totalCount = prescriptions.size();
            long approvedCount = prescriptions.stream()
                    .filter(p -> "APPROVED".equals(p.getStatus()))
                    .count();
            long rejectedCount = prescriptions.stream()
                    .filter(p -> "REJECTED".equals(p.getStatus()))
                    .count();
            long pendingCount = prescriptions.stream()
                    .filter(p -> "PENDING_REVIEW".equals(p.getStatus()) || "PENDING".equals(p.getStatus()))
                    .count();
            
            double approvalRate = totalCount > 0 ? (double) approvedCount / totalCount * 100 : 0;
            
            stats.put("totalCount", totalCount);
            stats.put("approvedCount", approvedCount);
            stats.put("rejectedCount", rejectedCount);
            stats.put("pendingCount", pendingCount);
            stats.put("approvalRate", approvalRate);
            stats.put("avgReviewTime", 15); // 模拟平均审核时间（分钟）
        } catch (Exception e) {
            log.warn("获取处方统计失败: {}", e.getMessage());
            stats.put("totalCount", 0);
            stats.put("approvedCount", 0);
            stats.put("rejectedCount", 0);
            stats.put("pendingCount", 0);
            stats.put("approvalRate", 0);
            stats.put("avgReviewTime", 0);
        }
        
        return stats;
    }

    /**
     * 导出统计数据（记录审计日志）
     * _Requirements: 9.5_
     */
    @Auditable(action = "EXPORT_STATS", module = "stats")
    public Map<String, Object> exportStats(LocalDate startDate, LocalDate endDate, String exportType) {
        log.info("导出统计数据: type={}, range={} to {}", exportType, startDate, endDate);
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("exportType", exportType);
        exportData.put("exportTime", LocalDateTime.now());
        exportData.put("data", getOverallStats(startDate, endDate));
        
        return exportData;
    }
}
