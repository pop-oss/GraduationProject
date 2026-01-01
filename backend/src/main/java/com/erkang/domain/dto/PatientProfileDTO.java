package com.erkang.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * 患者档案更新DTO
 */
@Data
public class PatientProfileDTO {
    
    @NotNull(message = "性别不能为空")
    private Integer gender;
    
    private LocalDate birthDate;
    private String idCard;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String medicalHistory;
    private String allergyHistory;
    private String familyHistory;
}
