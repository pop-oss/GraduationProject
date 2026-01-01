package com.erkang.domain.enums;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum RoleEnum {
    
    PATIENT("PATIENT", "患者"),
    DOCTOR_PRIMARY("DOCTOR_PRIMARY", "基层医生"),
    DOCTOR_EXPERT("DOCTOR_EXPERT", "专家医生"),
    PHARMACIST("PHARMACIST", "药师"),
    ADMIN("ADMIN", "管理员");
    
    private final String code;
    private final String name;
    
    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
    
    /** 是否为医生角色 */
    public boolean isDoctor() {
        return this == DOCTOR_PRIMARY || this == DOCTOR_EXPERT;
    }
}
