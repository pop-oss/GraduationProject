package com.erkang.security;

import java.lang.annotation.*;

/**
 * 数据范围控制注解
 * 用于Service方法，自动过滤数据范围
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    
    /**
     * 数据范围类型
     */
    DataScopeType value();
    
    /**
     * 患者ID字段名（用于SQL拼接）
     */
    String patientIdField() default "patient_id";
    
    /**
     * 医生ID字段名（用于SQL拼接）
     */
    String doctorIdField() default "doctor_id";
}
