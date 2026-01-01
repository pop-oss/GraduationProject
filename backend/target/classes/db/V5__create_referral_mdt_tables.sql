-- =============================================
-- 耳康云诊 - 转诊与MDT会诊相关表
-- =============================================

-- 转诊表
CREATE TABLE IF NOT EXISTS referral (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '原问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    from_doctor_id BIGINT NOT NULL COMMENT '转出医生ID',
    to_doctor_id BIGINT COMMENT '转入医生ID',
    to_hospital_id BIGINT COMMENT '转入医院ID',
    to_department_id BIGINT COMMENT '转入科室ID',
    referral_no VARCHAR(32) NOT NULL UNIQUE COMMENT '转诊编号',
    referral_reason TEXT NOT NULL COMMENT '转诊原因',
    clinical_summary TEXT NOT NULL COMMENT '病历摘要',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/ACCEPTED/REJECTED/COMPLETED',
    status_updated_at DATETIME COMMENT '状态更新时间',
    accepted_at DATETIME COMMENT '接受时间',
    reject_reason VARCHAR(255) COMMENT '拒绝原因',
    new_consultation_id BIGINT COMMENT '新问诊ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id),
    INDEX idx_from_doctor (from_doctor_id),
    INDEX idx_to_doctor (to_doctor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转诊表';

-- MDT会诊表
CREATE TABLE IF NOT EXISTS mdt_case (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '关联问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    mdt_no VARCHAR(32) NOT NULL UNIQUE COMMENT '会诊编号',
    title VARCHAR(100) NOT NULL COMMENT '会诊主题',
    clinical_summary TEXT NOT NULL COMMENT '病历摘要',
    discussion_points TEXT COMMENT '讨论要点',
    scheduled_time DATETIME COMMENT '计划时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    rtc_room_id VARCHAR(64) COMMENT 'RTC房间ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/IN_PROGRESS/COMPLETED/CANCELED',
    status_updated_at DATETIME COMMENT '状态更新时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id),
    INDEX idx_initiator (initiator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDT会诊表';


-- MDT参会成员表
CREATE TABLE IF NOT EXISTS mdt_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mdt_id BIGINT NOT NULL COMMENT '会诊ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    role VARCHAR(20) DEFAULT 'PARTICIPANT' COMMENT '角色: INITIATOR/PARTICIPANT',
    invite_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '邀请状态: PENDING/ACCEPTED/REJECTED',
    join_time DATETIME COMMENT '加入时间',
    leave_time DATETIME COMMENT '离开时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mdt_doctor (mdt_id, doctor_id),
    INDEX idx_mdt (mdt_id),
    INDEX idx_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDT参会成员表';

-- MDT会诊结论表
CREATE TABLE IF NOT EXISTS mdt_conclusion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mdt_id BIGINT NOT NULL UNIQUE COMMENT '会诊ID',
    conclusion TEXT NOT NULL COMMENT '会诊结论',
    treatment_plan TEXT COMMENT '治疗方案',
    followup_plan TEXT COMMENT '随访计划',
    recorder_id BIGINT NOT NULL COMMENT '记录人ID',
    confirmed_by TEXT COMMENT '确认人列表(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_mdt (mdt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDT会诊结论表';
