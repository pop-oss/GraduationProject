package com.erkang.security;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 标注在Controller方法上，表示需要指定角色才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 允许访问的角色列表（满足任一即可）
     */
    String[] value();
}
