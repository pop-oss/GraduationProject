package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 医院实体
 */
@Data
@TableName("org_hospital")
public class Hospital {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 医院名称 */
    private String name;
    
    /** 医院等级 */
    private String level;
    
    /** 地址 */
    private String address;
    
    /** 联系电话 */
    private String phone;
    
    /** 状态: 0禁用 1启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
