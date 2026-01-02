package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方明细实体
 * _Requirements: 6.1_
 */
@Data
@TableName("prescription_item")
public class PrescriptionItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long prescriptionId;
    
    private String drugName;            // 药品名称
    private String drugSpec;            // 规格
    private String dosage;              // 用法用量
    private String frequency;           // 频次
    private String duration;            // 疗程
    private Integer quantity;           // 数量
    private String unit;                // 单位
    private String notes;               // 备注
    private Integer sortOrder;          // 排序
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
