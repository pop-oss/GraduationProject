package com.erkang.domain.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 登录响应VO
 */
@Data
@Builder
public class LoginVO {
    
    /** 访问Token */
    private String accessToken;
    
    /** 刷新Token */
    private String refreshToken;
    
    /** Token类型 */
    private String tokenType;
    
    /** 过期时间(秒) */
    private Long expiresIn;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 头像 */
    private String avatar;
    
    /** 角色列表 */
    private List<String> roles;
}
