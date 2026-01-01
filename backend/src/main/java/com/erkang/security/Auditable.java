package com.erkang.security;

import java.lang.annotation.*;

/**
 * 审计注解
 * 标注在Controller方法上，自动记录审计日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    
    /**
     * 操作类型
     */
    String action();
    
    /**
     * 模块名称
     */
    String module();
    
    /**
     * 目标类型（可选）
     */
    String targetType() default "";
    
    /**
     * 备注（可选）
     */
    String remark() default "";
}
