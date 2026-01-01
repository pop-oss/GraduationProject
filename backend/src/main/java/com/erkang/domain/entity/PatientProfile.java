package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者档案实体
 */
@Data
@TableName("patient_profile")
public class PatientProfile {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 关联用户ID */
    private Long userId;
    
    /** 性别: 0女 1男 2未知 */
    private Integer gender;
    
    /** 出生日期 */
    private LocalDate birthDate;
    
    /** 身份证号(加密存储) */
    private String idCard;
    
    /** 地址 */
    private String address;
    
    /** 紧急联系人 */
    private String emergencyContact;
    
    /** 紧急联系电话 */
    private String emergencyPhone;
    
    /** 既往史 */
    private String medicalHistory;
    
    /** 过敏史 */
    private String allergyHistory;
    
    /** 家族史 */
    private String familyHistory;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
