package com.erkang.domain.vo;

import lombok.Data;
import java.time.LocalDate;

/**
 * 患者档案VO（脱敏后）
 */
@Data
public class PatientProfileVO {
    
    private Long id;
    private Long userId;
    private String realName;
    private Integer gender;
    private LocalDate birthDate;
    
    /** 身份证号（脱敏） */
    private String idCard;
    
    /** 手机号（脱敏） */
    private String phone;
    
    private String address;
    private String emergencyContact;
    
    /** 紧急联系电话（脱敏） */
    private String emergencyPhone;
    
    private String medicalHistory;
    private String allergyHistory;
    private String familyHistory;
}
