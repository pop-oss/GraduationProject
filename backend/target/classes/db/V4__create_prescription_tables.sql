-- =============================================
-- 耳康云诊 - 处方与审方相关表
-- =============================================

-- 处方表
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '开方医生ID',
    prescription_no VARCHAR(32) NOT NULL UNIQUE COMMENT '处方编号',
    diagnosis TEXT COMMENT '诊断',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_REVIEW/APPROVED/REJECTED',
    status_updated_at DATETIME COMMENT '状态更新时间',
    submitted_at DATETIME COMMENT '提交审核时间',
    approved_at DATETIME COMMENT '审核通过时间',
    notes TEXT COMMENT '医嘱/备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation (consultation_id),
    INDEX idx_patient (patient_id),
    INDEX idx_doctor (doctor_id),
    INDEX idx_status (status),
    INDEX idx_prescription_no (prescription_no),
    INDEX idx_submitted_at (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- 处方明细表
CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    drug_name VARCHAR(100) NOT NULL COMMENT '药品名称',
    drug_spec VARCHAR(100) COMMENT '规格',
    dosage VARCHAR(100) COMMENT '用法用量',
    frequency VARCHAR(50) COMMENT '频次',
    duration VARCHAR(50) COMMENT '疗程',
    quantity INT COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    notes VARCHAR(255) COMMENT '备注',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prescription (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- 审方记录表
CREATE TABLE IF NOT EXISTS pharmacy_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    reviewer_id BIGINT NOT NULL COMMENT '审方药师ID',
    review_status VARCHAR(20) NOT NULL COMMENT '审核结果: APPROVED/REJECTED/NEED_INFO',
    risk_level VARCHAR(20) COMMENT '风险等级: HIGH/MEDIUM/LOW',
    high_risk_items TEXT COMMENT '高风险项',
    medium_risk_items TEXT COMMENT '中风险项',
    low_risk_items TEXT COMMENT '低风险项',
    reject_reason TEXT COMMENT '驳回原因',
    suggestion TEXT COMMENT '建议',
    ai_risk_hint TEXT COMMENT 'AI风险提示',
    reviewed_at DATETIME COMMENT '审核时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_prescription (prescription_id),
    INDEX idx_reviewer (reviewer_id),
    INDEX idx_status (review_status),
    INDEX idx_reviewed_at (reviewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审方记录表';
