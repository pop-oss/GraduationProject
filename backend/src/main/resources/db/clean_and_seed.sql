-- 清理脏数据并插入有效测试数据
-- 用户ID映射:
-- 1: admin (系统管理员)
-- 15-19: patient1-5 (患者)
-- 20-22: doctor1-3 (主治医师)
-- 23-24: expert1-2 (专家)
-- 25-26: pharmacist1-2 (药师)

SET FOREIGN_KEY_CHECKS = 0;

-- 清空业务表
TRUNCATE TABLE prescription_item;
TRUNCATE TABLE prescription;
TRUNCATE TABLE pharmacy_review;
TRUNCATE TABLE mdt_conclusion;
TRUNCATE TABLE mdt_member;
TRUNCATE TABLE mdt_case;
TRUNCATE TABLE referral;
TRUNCATE TABLE followup_record;
TRUNCATE TABLE followup_plan;
TRUNCATE TABLE medical_attachment;
TRUNCATE TABLE medical_record;
TRUNCATE TABLE chat_message;
TRUNCATE TABLE consultation;
TRUNCATE TABLE appointment;
TRUNCATE TABLE audit_log;

SET FOREIGN_KEY_CHECKS = 1;

-- 插入问诊数据 (患者15-19, 医生20-24)
INSERT INTO consultation (id, patient_id, doctor_id, consultation_no, consultation_type, status, symptoms, created_at, updated_at) VALUES
(1, 15, 20, 'CON202601010001', 'VIDEO', 'FINISHED', '耳朵疼痛，听力下降', '2025-12-20 09:00:00', '2025-12-20 09:30:00'),
(2, 15, 21, 'CON202601010002', 'VIDEO', 'FINISHED', '鼻塞流涕一周', '2025-12-22 10:00:00', '2025-12-22 10:25:00'),
(3, 16, 20, 'CON202601010003', 'VIDEO', 'FINISHED', '咽喉肿痛，吞咽困难', '2025-12-25 14:00:00', '2025-12-25 14:35:00'),
(4, 16, 22, 'CON202601010004', 'VIDEO', 'FINISHED', '耳鸣持续两周', '2025-12-28 11:00:00', '2025-12-28 11:40:00'),
(5, 17, 21, 'CON202601010005', 'VIDEO', 'FINISHED', '鼻窦炎复发', '2025-12-30 15:00:00', '2025-12-30 15:30:00'),
(6, 17, 23, 'CON202601010006', 'VIDEO', 'FINISHED', '听力检查复诊', '2026-01-01 09:00:00', '2026-01-01 09:45:00'),
(7, 18, 20, 'CON202601010007', 'VIDEO', 'IN_PROGRESS', '中耳炎症状', '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(8, 18, 22, 'CON202601010008', 'VIDEO', 'WAITING', '扁桃体发炎', '2026-01-02 14:00:00', '2026-01-02 14:00:00'),
(9, 19, 21, 'CON202601010009', 'VIDEO', 'WAITING', '过敏性鼻炎', '2026-01-02 15:00:00', '2026-01-02 15:00:00'),
(10, 19, 24, 'CON202601010010', 'VIDEO', 'CANCELED', '预约取消', '2026-01-02 16:00:00', '2026-01-02 16:00:00');

-- 插入病历数据
INSERT INTO medical_record (id, consultation_id, patient_id, doctor_id, record_no, chief_complaint, present_illness, past_history, diagnosis, treatment_plan, created_at) VALUES
(1, 1, 15, 20, 'MR202512200001', '耳朵疼痛，听力下降', '患者诉左耳疼痛3天，伴听力下降', '既往体健', '急性中耳炎', '抗生素治疗，滴耳液', '2025-12-20 09:30:00'),
(2, 2, 15, 21, 'MR202512220001', '鼻塞流涕一周', '患者诉鼻塞、流清涕一周，无发热', '有过敏性鼻炎病史', '过敏性鼻炎急性发作', '抗过敏药物，鼻喷剂', '2025-12-22 10:25:00'),
(3, 3, 16, 20, 'MR202512250001', '咽喉肿痛，吞咽困难', '患者诉咽喉肿痛2天，吞咽时加重', '既往体健', '急性咽炎', '抗炎治疗，含片', '2025-12-25 14:35:00'),
(4, 4, 16, 22, 'MR202512280001', '耳鸣持续两周', '患者诉双耳持续性耳鸣两周', '高血压病史5年', '神经性耳鸣', '营养神经药物，改善微循环', '2025-12-28 11:40:00'),
(5, 5, 17, 21, 'MR202512300001', '鼻窦炎复发', '患者诉鼻塞、头痛、脓涕一周', '慢性鼻窦炎病史', '慢性鼻窦炎急性发作', '抗生素，鼻腔冲洗', '2025-12-30 15:30:00');

-- 插入处方数据
INSERT INTO prescription (id, consultation_id, patient_id, doctor_id, prescription_no, status, diagnosis, notes, created_at) VALUES
(1, 1, 15, 20, 'RX202512200001', 'APPROVED', '急性中耳炎', '按时服药，避免耳朵进水', '2025-12-20 09:30:00'),
(2, 2, 15, 21, 'RX202512220001', 'APPROVED', '过敏性鼻炎', '避免接触过敏原', '2025-12-22 10:25:00'),
(3, 3, 16, 20, 'RX202512250001', 'APPROVED', '急性咽炎', '多喝水，清淡饮食', '2025-12-25 14:35:00'),
(4, 4, 16, 22, 'RX202512280001', 'PENDING_REVIEW', '神经性耳鸣', '注意休息，避免噪音', '2025-12-28 11:40:00'),
(5, 5, 17, 21, 'RX202512300001', 'REJECTED', '慢性鼻窦炎', '需要调整用药剂量', '2025-12-30 15:30:00');

-- 插入处方明细
INSERT INTO prescription_item (id, prescription_id, drug_name, drug_spec, dosage, frequency, duration, quantity, unit, notes) VALUES
(1, 1, '阿莫西林胶囊', '0.5g*24粒', '0.5g', '每日3次', '7天', 2, '盒', '饭后服用'),
(2, 1, '氧氟沙星滴耳液', '5ml', '3-4滴', '每日2次', '7天', 1, '瓶', '滴入患耳'),
(3, 2, '氯雷他定片', '10mg*6片', '10mg', '每日1次', '14天', 3, '盒', '睡前服用'),
(4, 2, '布地奈德鼻喷剂', '120喷', '每侧2喷', '每日2次', '14天', 1, '瓶', ''),
(5, 3, '蒲地蓝消炎口服液', '10ml*6支', '10ml', '每日3次', '5天', 2, '盒', ''),
(6, 3, '西瓜霜含片', '0.6g*24片', '1片', '每日4-6次', '5天', 1, '盒', '含服'),
(7, 4, '甲钴胺片', '0.5mg*20片', '0.5mg', '每日3次', '30天', 5, '盒', ''),
(8, 4, '银杏叶片', '19.2mg*24片', '2片', '每日3次', '30天', 4, '盒', ''),
(9, 5, '头孢克洛缓释片', '0.375g*6片', '0.375g', '每日2次', '10天', 2, '盒', '');

-- 插入药师审核记录
INSERT INTO pharmacy_review (id, prescription_id, reviewer_id, review_status, reject_reason, reviewed_at, created_at) VALUES
(1, 1, 25, 'APPROVED', NULL, '2025-12-20 10:00:00', '2025-12-20 09:35:00'),
(2, 2, 25, 'APPROVED', NULL, '2025-12-22 11:00:00', '2025-12-22 10:30:00'),
(3, 3, 26, 'APPROVED', NULL, '2025-12-25 15:00:00', '2025-12-25 14:40:00'),
(5, 5, 26, 'REJECTED', '头孢类药物剂量偏大，建议调整', '2025-12-30 16:00:00', '2025-12-30 15:35:00');

-- 插入转诊数据
INSERT INTO referral (id, patient_id, from_doctor_id, to_doctor_id, consultation_id, referral_no, referral_reason, clinical_summary, status, created_at) VALUES
(1, 16, 22, 23, 4, 'REF202512280001', '耳鸣症状复杂，需专家会诊', '患者耳鸣持续两周，高血压病史5年', 'COMPLETED', '2025-12-28 12:00:00'),
(2, 17, 21, 24, 5, 'REF202512300001', '鼻窦炎反复发作，建议专家评估手术指征', '慢性鼻窦炎急性发作，药物治疗效果不佳', 'ACCEPTED', '2025-12-30 16:00:00'),
(3, 18, 20, 23, 7, 'REF202601020001', '中耳炎需要进一步检查', '患者中耳炎症状，需专家进一步评估', 'PENDING', '2026-01-02 10:30:00');

-- 插入MDT会诊数据
INSERT INTO mdt_case (id, patient_id, initiator_id, consultation_id, mdt_no, title, clinical_summary, status, scheduled_time, created_at) VALUES
(1, 16, 22, 4, 'MDT202512280001', '耳鸣患者多学科会诊', '患者耳鸣持续两周，高血压病史5年，需要多学科讨论治疗方案', 'COMPLETED', '2025-12-29 14:00:00', '2025-12-28 12:30:00'),
(2, 17, 21, 5, 'MDT202512300001', '慢性鼻窦炎手术评估', '慢性鼻窦炎反复发作，药物治疗效果不佳，评估是否需要手术治疗', 'PENDING', '2026-01-03 10:00:00', '2025-12-30 16:30:00');

-- 插入MDT成员
INSERT INTO mdt_member (id, mdt_id, doctor_id, role, invite_status, created_at) VALUES
(1, 1, 22, 'INITIATOR', 'ACCEPTED', '2025-12-28 12:30:00'),
(2, 1, 23, 'EXPERT', 'ACCEPTED', '2025-12-28 13:00:00'),
(3, 1, 24, 'EXPERT', 'ACCEPTED', '2025-12-28 13:30:00'),
(4, 2, 21, 'INITIATOR', 'ACCEPTED', '2025-12-30 16:30:00'),
(5, 2, 23, 'EXPERT', 'PENDING', '2025-12-30 17:00:00'),
(6, 2, 24, 'EXPERT', 'PENDING', '2025-12-30 17:00:00');

-- 插入MDT结论
INSERT INTO mdt_conclusion (id, mdt_id, recorder_id, conclusion, treatment_plan, followup_plan, created_at) VALUES
(1, 1, 23, '患者耳鸣考虑为血管性耳鸣，与高血压相关', '建议控制血压，配合营养神经治疗', '定期复查，每月随访一次', '2025-12-29 15:00:00');

-- 插入随访计划
INSERT INTO followup_plan (id, patient_id, doctor_id, consultation_id, plan_no, diagnosis, followup_type, interval_days, total_times, next_followup_date, status, created_at) VALUES
(1, 15, 20, 1, 'FP202512200001', '急性中耳炎', 'REGULAR', 7, 3, '2025-12-27', 'COMPLETED', '2025-12-20 09:35:00'),
(2, 15, 21, 2, 'FP202512220001', '过敏性鼻炎', 'REGULAR', 14, 2, '2026-01-05', 'ACTIVE', '2025-12-22 10:30:00'),
(3, 16, 20, 3, 'FP202512250001', '急性咽炎', 'REGULAR', 7, 2, '2026-01-01', 'COMPLETED', '2025-12-25 14:40:00'),
(4, 17, 21, 5, 'FP202512300001', '慢性鼻窦炎', 'REGULAR', 10, 3, '2026-01-10', 'ACTIVE', '2025-12-30 15:35:00');

-- 插入随访记录
INSERT INTO followup_record (id, plan_id, patient_id, record_no, followup_date, symptoms, doctor_comment, next_action, status, created_at) VALUES
(1, 1, 15, 'FR202512270001', '2025-12-27', '耳痛明显减轻，听力恢复', '恢复良好，继续用药', '一周后复诊', 'COMPLETED', '2025-12-27 10:00:00'),
(2, 3, 16, 'FR202601010001', '2026-01-01', '咽喉已无明显不适', '已痊愈，停药观察', '如有不适随时就诊', 'COMPLETED', '2026-01-01 09:00:00');

-- 插入审计日志
INSERT INTO audit_log (id, user_id, username, module, action, target_id, remark, ip_address, created_at) VALUES
(1, 1, 'admin', 'auth', 'LOGIN', NULL, '管理员登录系统', '192.168.1.100', '2025-12-20 08:00:00'),
(2, 20, 'doctor1', 'auth', 'LOGIN', NULL, '医生登录', '192.168.1.101', '2025-12-20 08:30:00'),
(3, 15, 'patient1', 'CONSULTATION', 'CREATE_CONSULTATION', 1, '创建问诊预约', '192.168.1.102', '2025-12-20 09:00:00'),
(4, 20, 'doctor1', 'CONSULTATION', 'ACCEPT_CONSULTATION', 1, '接受问诊', '192.168.1.101', '2025-12-20 09:05:00'),
(5, 20, 'doctor1', 'CONSULTATION', 'START_CONSULTATION', 1, '开始问诊', '192.168.1.101', '2025-12-20 09:10:00'),
(6, 20, 'doctor1', 'medical_record', 'VIEW_RECORD', 1, '查看病历', '192.168.1.101', '2025-12-20 09:15:00'),
(7, 20, 'doctor1', 'prescription', 'CREATE_PRESCRIPTION', 1, '开具处方', '192.168.1.101', '2025-12-20 09:30:00'),
(8, 25, 'pharmacist1', 'prescription', 'REVIEW_PRESCRIPTION', 1, '审核处方通过', '192.168.1.103', '2025-12-20 10:00:00'),
(9, 21, 'doctor2', 'auth', 'LOGIN', NULL, '医生登录', '192.168.1.104', '2025-12-22 09:30:00'),
(10, 15, 'patient1', 'CONSULTATION', 'CREATE_CONSULTATION', 2, '创建问诊预约', '192.168.1.102', '2025-12-22 10:00:00'),
(11, 22, 'doctor3', 'referral', 'CREATE_REFERRAL', 1, '发起转诊', '192.168.1.105', '2025-12-28 12:00:00'),
(12, 22, 'doctor3', 'mdt', 'CREATE_MDT', 1, '发起MDT会诊', '192.168.1.105', '2025-12-28 12:30:00'),
(13, 23, 'expert1', 'mdt', 'CREATE_MDT', 1, '参与MDT会诊', '192.168.1.106', '2025-12-29 14:00:00'),
(14, 26, 'pharmacist2', 'prescription', 'REJECT_PRESCRIPTION', 5, '驳回处方', '192.168.1.107', '2025-12-30 16:00:00'),
(15, 1, 'admin', 'stats', 'VIEW_STATS', NULL, '查看统计数据', '192.168.1.100', '2026-01-01 08:00:00'),
(16, 1, 'admin', 'stats', 'EXPORT_STATS', NULL, '导出统计报表', '192.168.1.100', '2026-01-01 08:30:00');

SELECT 'Data cleanup and seeding completed!' as result;
