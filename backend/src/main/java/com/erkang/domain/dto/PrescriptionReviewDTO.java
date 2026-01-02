package com.erkang.domain.dto;

import com.erkang.domain.entity.PrescriptionItem;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方审核列表DTO
 * 包含患者姓名、医生姓名、药品数量等前端需要的字段
 */
@Data
public class PrescriptionReviewDTO {
    
    private Long id;
    private String prescriptionNo;
    private Long consultationId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String diagnosis;
    private String status;
    private String riskLevel;       // LOW/MEDIUM/HIGH
    private Integer drugCount;      // 药品数量
    private LocalDateTime createdAt;
    private LocalDateTime submittedAt;
    
    // 详情页面需要的额外字段
    private Integer patientAge;     // 患者年龄
    private String patientGender;   // 患者性别
    private List<String> allergies; // 过敏史
    private List<PrescriptionItem> items; // 药品明细
    private String rejectReason;    // 驳回原因
    private LocalDateTime reviewedAt; // 审核时间
    private String reviewedBy;      // 审核人
    private String reviewResult;    // 审核结果 APPROVED/REJECTED
}
