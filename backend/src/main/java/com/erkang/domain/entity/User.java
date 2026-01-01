package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码(BCrypt) */
    private String password;
    
    /** 手机号 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 真实姓名 */
    private String realName;
    
    /** 头像URL */
    private String avatar;
    
    /** 状态: 0禁用 1启用 */
    private Integer status;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /** 软删除时间 */
    @TableLogic
    private LocalDateTime deletedAt;
}
