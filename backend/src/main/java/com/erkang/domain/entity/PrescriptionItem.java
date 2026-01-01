package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
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
    private String drugCode;            // 药品编码
    private String specification;       // 规格
    private String dosage;              // 单次剂量
    private String frequency;           // 用药频次
    private String route;               // 给药途径
    private Integer days;               // 用药天数
    private Integer quantity;           // 数量
    private String unit;                // 单位
    private BigDecimal price;           // 单价
    private String remark;              // 备注
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
