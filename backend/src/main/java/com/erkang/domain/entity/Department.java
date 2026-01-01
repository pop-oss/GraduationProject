package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 科室实体
 */
@Data
@TableName("org_department")
public class Department {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 所属医院ID */
    private Long hospitalId;
    
    /** 科室名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 排序 */
    private Integer sortOrder;
    
    /** 状态: 0禁用 1启用 */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
