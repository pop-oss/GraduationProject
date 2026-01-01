package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病历附件实体
 * _Requirements: 5.4_
 */
@Data
@TableName("medical_attachment")
public class MedicalAttachment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long recordId;
    private Long consultationId;
    private Long patientId;
    
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String category;        // REPORT/IMAGE/OTHER
    private String description;
    private Long uploaderId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
