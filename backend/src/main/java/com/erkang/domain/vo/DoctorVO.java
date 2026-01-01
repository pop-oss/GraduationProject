package com.erkang.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 医生信息VO
 */
@Data
public class DoctorVO {
    
    private Long id;
    private Long userId;
    private String realName;
    private String avatar;
    private String hospitalName;
    private String departmentName;
    private String title;
    private String specialty;
    private String introduction;
    private BigDecimal consultationFee;
    private Boolean isExpert;
    private Integer status;
}
