package com.erkang.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MDT参会成员实体
 * _Requirements: 7.3, 7.4_
 */
@Data
@TableName("mdt_member")
public class MDTMember {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long mdtId;                 // 会诊ID
    private Long doctorId;              // 医生ID
    
    private String role;                // INITIATOR/PARTICIPANT
    private String inviteStatus;        // PENDING/ACCEPTED/REJECTED
    
    private LocalDateTime joinTime;     // 加入时间
    private LocalDateTime leaveTime;    // 离开时间
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
