package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生档案实体
 */
@Data
@TableName("doctor_profile")
public class DoctorProfile {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联用户ID */
    private Long userId;
    
    /** 所属医院ID */
    private Long hospitalId;
    
    /** 所属科室ID */
    private Long departmentId;
    
    /** 职称 */
    private String title;
    
    /** 专长 */
    private String specialty;
    
    /** 简介 */
    private String introduction;
    
    /** 执业证号 */
    private String licenseNo;
    
    /** 问诊费用 */
    private BigDecimal consultationFee;
    
    /** 是否专家: 0否 1是 */
    private Integer isExpert;
    
    /** 状态: 0停诊 1接诊中 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
