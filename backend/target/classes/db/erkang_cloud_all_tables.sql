-- =============================================
-- 耳康云诊 - 完整数据库建表脚本
-- 生成时间: 2024
-- 说明: 此文件整合了所有数据库表的创建语句，可一次性执行
-- =============================================

-- 创建数据库（如需要）
CREATE DATABASE IF NOT EXISTS erkang_cloud DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erkang_cloud;

-- =============================================
-- 第一部分: 用户权限相关表 (V1)
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '软删除时间',
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_type VARCHAR(20) COMMENT '类型: menu/button/api',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';


-- =============================================
-- 第二部分: 患者与医生档案表 (V2)
-- =============================================

-- 患者档案表
CREATE TABLE IF NOT EXISTS patient_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
    gender TINYINT COMMENT '性别: 0女 1男 2未知',
    birth_date DATE COMMENT '出生日期',
    id_card VARCHAR(18) COMMENT '身份证号(加密存储)',
    address VARCHAR(255) COMMENT '地址',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    medical_history TEXT COMMENT '既往史',
    allergy_history TEXT COMMENT '过敏史',
    family_history TEXT COMMENT '家族史',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者档案表';

-- 医院表
CREATE TABLE IF NOT EXISTS org_hospital (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '医院名称',
    level VARCHAR(20) COMMENT '医院等级',
    address VARCHAR(255) COMMENT '地址',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医院表';

-- 科室表
CREATE TABLE IF NOT EXISTS org_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hospital_id BIGINT NOT NULL COMMENT '所属医院ID',
    name VARCHAR(50) NOT NULL COMMENT '科室名称',
    description VARCHAR(255) COMMENT '描述',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hospital (hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生档案表
CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
    hospital_id BIGINT COMMENT '所属医院ID',
    department_id BIGINT COMMENT '所属科室ID',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(255) COMMENT '专长',
    introduction TEXT COMMENT '简介',
    license_no VARCHAR(50) COMMENT '执业证号',
    consultation_fee DECIMAL(10,2) COMMENT '问诊费用',
    is_expert TINYINT DEFAULT 0 COMMENT '是否专家: 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态: 0停诊 1接诊中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_hospital (hospital_id),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生档案表';

-- =============================================
-- 第三部分: 预约、问诊与病历相关表 (V3)
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


-- =============================================
-- 第四部分: 处方与审方相关表 (V4)
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

-- =============================================
-- 第五部分: 转诊与MDT会诊相关表 (V5)
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


-- =============================================
-- 第六部分: 随访相关表 (V6)
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

-- =============================================
-- 第七部分: AI与审计相关表 (V7)
-- =============================================

-- AI任务表
CREATE TABLE IF NOT EXISTS ai_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型: HEALTH_QA/RECORD_SUMMARY/RISK_CHECK/FOLLOWUP_GEN',
    related_id BIGINT COMMENT '关联业务ID',
    related_type VARCHAR(50) COMMENT '关联类型: CONSULTATION/PRESCRIPTION/FOLLOWUP',
    user_id BIGINT NOT NULL COMMENT '请求用户ID',
    request_data TEXT COMMENT '请求数据(脱敏后)',
    response_data TEXT COMMENT '响应数据',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/COMPLETED/FAILED',
    error_message TEXT COMMENT '错误信息',
    tokens_used INT COMMENT '消耗Token数',
    latency_ms INT COMMENT '响应延迟(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME COMMENT '完成时间',
    INDEX idx_type (task_type),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

-- AI对话会话表
CREATE TABLE IF NOT EXISTS ai_chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_type VARCHAR(50) DEFAULT 'HEALTH_QA' COMMENT '会话类型',
    title VARCHAR(100) COMMENT '会话标题',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CLOSED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS ai_chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: USER/ASSISTANT/SYSTEM',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens INT COMMENT 'Token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    user_role VARCHAR(50) COMMENT '用户角色',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    module VARCHAR(50) COMMENT '模块',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_desc VARCHAR(255) COMMENT '目标描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_code INT COMMENT '响应码',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT 'User-Agent',
    duration_ms INT COMMENT '耗时(毫秒)',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_module (module),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- =============================================
-- 第八部分: 初始化数据
-- =============================================

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('PATIENT', '患者', '患者用户，可预约问诊、查看病历、进行健康咨询'),
('DOCTOR_PRIMARY', '基层医生', '基层医生，可接诊患者、开具处方、发起转诊'),
('DOCTOR_EXPERT', '专家医生', '专家医生，可接受转诊、参与MDT会诊'),
('PHARMACIST', '药师', '药师，负责处方审核与用药风险提示'),
('ADMIN', '管理员', '系统管理员，负责用户管理、权限配置、系统监控');

-- 初始化管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 1);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) 
SELECT u.id, r.id FROM sys_user u, sys_role r 
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

-- =============================================
-- 执行完成
-- =============================================
