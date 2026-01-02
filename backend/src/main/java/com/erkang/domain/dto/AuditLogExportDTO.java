package com.erkang.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 审计日志导出DTO
 */
@Data
public class AuditLogExportDTO {
    
    @ExcelProperty("时间")
    @ColumnWidth(20)
    private String createdAt;
    
    @ExcelProperty("用户名")
    @ColumnWidth(15)
    private String username;
    
    @ExcelProperty("真实姓名")
    @ColumnWidth(15)
    private String realName;
    
    @ExcelProperty("手机号")
    @ColumnWidth(15)
    private String phone;
    
    @ExcelProperty("模块")
    @ColumnWidth(12)
    private String module;
    
    @ExcelProperty("操作")
    @ColumnWidth(15)
    private String action;
    
    @ExcelProperty("目标ID")
    @ColumnWidth(12)
    private String targetId;
    
    @ExcelProperty("详情")
    @ColumnWidth(30)
    private String detail;
    
    @ExcelProperty("IP")
    @ColumnWidth(15)
    private String ip;
}
