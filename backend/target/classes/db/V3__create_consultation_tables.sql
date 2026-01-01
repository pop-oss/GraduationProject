-- =============================================
-- 耳康云诊 - 预约、问诊与病历相关表
-- =============================================

-- 预约表
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时段: 09:00-09:30',
    chief_complaint TEXT COMMENT '主诉/问诊原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMED/CANCELED',
    status_updated_at DATETIME COMMENT '状态更新时间',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 问诊表
CREATE TABLE IF NOT EXISTS consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT COMMENT '关联预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    consultation_no VARCHAR(32) NOT NULL UNIQUE COMMENT '问诊编号',
    consultation_type VARCHAR(20) DEFAULT 'VIDEO' COMMENT '类型: VIDEO/TEXT/PHONE',
    status VARCHAR(20) DEFAULT 'WAITING' COMMENT '状态: WAITING/IN_PROGRESS/FINISHED/CANCELED',
    status_updated_at DATETIME COMMENT '状态更新时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '时长(分钟)',
    rtc_room_id VARCHAR(64) COMMENT 'RTC房间ID',
    is_recorded TINYINT DEFAULT 0 COMMENT '是否录制: 0否 1是',
    record_consent TINYINT DEFAULT 0 COMMENT '录制授权: 0未授权 1已授权',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_status (status),
    INDEX idx_consultation_no (consultation_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_type VARCHAR(20) NOT NULL COMMENT '发送者类型: PATIENT/DOCTOR',
    content_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '内容类型: TEXT/IMAGE/FILE',
    content TEXT COMMENT '消息内容',
    attachment_url VARCHAR(255) COMMENT '附件URL',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0否 1是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_sender (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';


-- 病历表
CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    record_no VARCHAR(32) NOT NULL UNIQUE COMMENT '病历编号',
    chief_complaint TEXT COMMENT '主诉',
    present_illness TEXT COMMENT '现病史',
    past_history TEXT COMMENT '既往史',
    allergy_history TEXT COMMENT '过敏史',
    physical_exam TEXT COMMENT '体格检查',
    auxiliary_exam TEXT COMMENT '辅助检查',
    diagnosis TEXT COMMENT '初步诊断',
    treatment_plan TEXT COMMENT '处理建议',
    followup_advice TEXT COMMENT '随访建议',
    ai_summary TEXT COMMENT 'AI生成摘要(需医生确认)',
    ai_confirmed TINYINT DEFAULT 0 COMMENT 'AI摘要是否已确认',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/SUBMITTED',
    submitted_at DATETIME COMMENT '提交时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_record_no (record_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历表';

-- 病历附件表
CREATE TABLE IF NOT EXISTS medical_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT COMMENT '病历ID',
    consultation_id BIGINT COMMENT '问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(50) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    category VARCHAR(50) COMMENT '分类: REPORT/IMAGE/OTHER',
    description VARCHAR(255) COMMENT '描述',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record (record_id),
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历附件表';
