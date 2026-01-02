package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.domain.dto.PrescriptionReviewDTO;
import com.erkang.domain.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 处方Mapper
 */
@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
    
    /**
     * 分页查询待审核处方（包含患者姓名、医生姓名、药品数量）
     * 如果有多条审核记录，只返回最新的一条
     */
    @Select("SELECT p.id, p.prescription_no AS prescriptionNo, p.consultation_id AS consultationId, p.patient_id AS patientId, " +
            "patient.real_name AS patientName, p.doctor_id AS doctorId, doctor.real_name AS doctorName, " +
            "p.diagnosis, p.status, p.created_at AS createdAt, p.submitted_at AS submittedAt, " +
            "COALESCE(pr.risk_level, 'LOW') AS riskLevel, " +
            "(SELECT COUNT(*) FROM prescription_item pi WHERE pi.prescription_id = p.id) AS drugCount " +
            "FROM prescription p " +
            "LEFT JOIN sys_user patient ON p.patient_id = patient.id " +
            "LEFT JOIN sys_user doctor ON p.doctor_id = doctor.id " +
            "LEFT JOIN pharmacy_review pr ON pr.prescription_id = p.id " +
            "AND pr.id = (SELECT MAX(pr2.id) FROM pharmacy_review pr2 WHERE pr2.prescription_id = p.id) " +
            "WHERE p.status = #{status} " +
            "ORDER BY p.submitted_at ASC")
    Page<PrescriptionReviewDTO> selectPendingReviewPage(Page<?> page, @Param("status") String status);
    
    /**
     * 查询处方详情（包含患者姓名、医生姓名、药品数量）
     * 如果有多条审核记录，只返回最新的一条
     */
    @Select("SELECT p.id, p.prescription_no AS prescriptionNo, p.consultation_id AS consultationId, p.patient_id AS patientId, " +
            "patient.real_name AS patientName, p.doctor_id AS doctorId, doctor.real_name AS doctorName, " +
            "p.diagnosis, p.status, p.created_at AS createdAt, p.submitted_at AS submittedAt, " +
            "COALESCE(pr.risk_level, 'LOW') AS riskLevel, pr.review_status AS reviewResult, " +
            "pr.reject_reason AS rejectReason, pr.reviewed_at AS reviewedAt, " +
            "(SELECT COUNT(*) FROM prescription_item pi WHERE pi.prescription_id = p.id) AS drugCount " +
            "FROM prescription p " +
            "LEFT JOIN sys_user patient ON p.patient_id = patient.id " +
            "LEFT JOIN sys_user doctor ON p.doctor_id = doctor.id " +
            "LEFT JOIN pharmacy_review pr ON pr.prescription_id = p.id " +
            "AND pr.id = (SELECT MAX(pr2.id) FROM pharmacy_review pr2 WHERE pr2.prescription_id = p.id) " +
            "WHERE p.id = #{id}")
    PrescriptionReviewDTO selectReviewDetailById(@Param("id") Long id);
    
    /**
     * 分页查询药师审方历史（包含处方详情，支持筛选）
     * 查询当前药师审核过的处方记录，每个处方只返回最新的审核记录
     */
    @Select("<script>" +
            "SELECT p.id, p.prescription_no AS prescriptionNo, p.consultation_id AS consultationId, p.patient_id AS patientId, " +
            "patient.real_name AS patientName, p.doctor_id AS doctorId, doctor.real_name AS doctorName, " +
            "p.diagnosis, p.status, p.created_at AS createdAt, pr.reviewed_at AS reviewedAt, " +
            "pr.risk_level AS riskLevel, pr.review_status AS reviewResult, " +
            "(SELECT COUNT(*) FROM prescription_item pi WHERE pi.prescription_id = p.id) AS drugCount " +
            "FROM pharmacy_review pr " +
            "INNER JOIN prescription p ON pr.prescription_id = p.id " +
            "LEFT JOIN sys_user patient ON p.patient_id = patient.id " +
            "LEFT JOIN sys_user doctor ON p.doctor_id = doctor.id " +
            "WHERE pr.reviewer_id = #{pharmacistId} " +
            "AND pr.id = (SELECT MAX(pr2.id) FROM pharmacy_review pr2 WHERE pr2.prescription_id = pr.prescription_id) " +
            "<if test=\"status != null and status != ''\">" +
            "AND pr.review_status = #{status} " +
            "</if>" +
            "<if test=\"startDate != null and startDate != ''\">" +
            "AND DATE(pr.reviewed_at) &gt;= #{startDate} " +
            "</if>" +
            "<if test=\"endDate != null and endDate != ''\">" +
            "AND DATE(pr.reviewed_at) &lt;= #{endDate} " +
            "</if>" +
            "ORDER BY pr.reviewed_at DESC" +
            "</script>")
    Page<PrescriptionReviewDTO> selectReviewHistoryByPharmacist(Page<?> page, 
            @Param("pharmacistId") Long pharmacistId,
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    /**
     * 分页查询所有审方历史（支持筛选，不限制药师）
     */
    @Select("<script>" +
            "SELECT p.id, p.prescription_no AS prescriptionNo, p.consultation_id AS consultationId, p.patient_id AS patientId, " +
            "patient.real_name AS patientName, p.doctor_id AS doctorId, doctor.real_name AS doctorName, " +
            "p.diagnosis, p.status, p.created_at AS createdAt, pr.reviewed_at AS reviewedAt, " +
            "pr.risk_level AS riskLevel, pr.review_status AS reviewResult, " +
            "(SELECT COUNT(*) FROM prescription_item pi WHERE pi.prescription_id = p.id) AS drugCount " +
            "FROM pharmacy_review pr " +
            "INNER JOIN prescription p ON pr.prescription_id = p.id " +
            "LEFT JOIN sys_user patient ON p.patient_id = patient.id " +
            "LEFT JOIN sys_user doctor ON p.doctor_id = doctor.id " +
            "WHERE pr.id = (SELECT MAX(pr2.id) FROM pharmacy_review pr2 WHERE pr2.prescription_id = pr.prescription_id) " +
            "<if test=\"status != null and status != ''\">" +
            "AND pr.review_status = #{status} " +
            "</if>" +
            "<if test=\"startDate != null and startDate != ''\">" +
            "AND DATE(pr.reviewed_at) &gt;= #{startDate} " +
            "</if>" +
            "<if test=\"endDate != null and endDate != ''\">" +
            "AND DATE(pr.reviewed_at) &lt;= #{endDate} " +
            "</if>" +
            "ORDER BY pr.reviewed_at DESC" +
            "</script>")
    Page<PrescriptionReviewDTO> selectAllReviewHistory(Page<?> page, 
            @Param("status") String status,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
}
