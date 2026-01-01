package com.erkang.security;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 登录用户信息（存储在ThreadLocal中）
 */
@Data
@Builder
public class LoginUser {
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 角色列表 */
    private List<String> roles;
    
    /**
     * 是否拥有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 是否拥有任一角色
     */
    public boolean hasAnyRole(String... roleArray) {
        if (roles == null) return false;
        for (String role : roleArray) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
