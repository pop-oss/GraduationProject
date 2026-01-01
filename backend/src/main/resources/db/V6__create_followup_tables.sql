-- =============================================
-- 耳康云诊 - 随访相关表
-- =============================================

-- 随访计划表
CREATE TABLE IF NOT EXISTS followup_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    plan_no VARCHAR(32) NOT NULL UNIQUE COMMENT '计划编号',
    diagnosis VARCHAR(255) COMMENT '诊断',
    followup_type VARCHAR(20) DEFAULT 'REGULAR' COMMENT '类型: REGULAR/CHRONIC',
    interval_days INT COMMENT '随访间隔(天)',
    total_times INT COMMENT '总次数',
    completed_times INT DEFAULT 0 COMMENT '已完成次数',
    next_followup_date DATE COMMENT '下次随访日期',
    question_list TEXT COMMENT '随访问题清单(JSON)',
    red_flags TEXT COMMENT '红旗征象(JSON)',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/COMPLETED/CANCELED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_next_date (next_followup_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访计划表';

-- 随访记录表
CREATE TABLE IF NOT EXISTS followup_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    record_no VARCHAR(32) NOT NULL UNIQUE COMMENT '记录编号',
    followup_date DATE NOT NULL COMMENT '随访日期',
    symptoms TEXT COMMENT '症状描述',
    answers TEXT COMMENT '问卷答案(JSON)',
    has_red_flag TINYINT DEFAULT 0 COMMENT '是否有红旗征象',
    red_flag_detail TEXT COMMENT '红旗征象详情',
    doctor_comment TEXT COMMENT '医生评语',
    next_action VARCHAR(255) COMMENT '下一步建议',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/SUBMITTED/REVIEWED',
    submitted_at DATETIME COMMENT '提交时间',
    reviewed_at DATETIME COMMENT '审阅时间',
    reviewer_id BIGINT COMMENT '审阅医生ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan (plan_id),
    INDEX idx_patient (patient_id),
    INDEX idx_date (followup_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随访记录表';
