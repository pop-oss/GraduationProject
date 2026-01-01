-- =============================================
-- 耳康云诊 - 测试数据脚本
-- 生成时间: 2026-01-01
-- 说明: 包含所有模块的测试数据
-- =============================================

USE erkang_cloud;

-- =============================================
-- 1. 用户数据 (密码统一为: 123456)
-- BCrypt加密后: $2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK
-- =============================================

-- 清空现有测试数据（保留admin）
DELETE FROM sys_user WHERE username != 'admin';

-- 更新 admin 密码为 admin123
UPDATE sys_user SET password = '$2a$10$LSUFK9JtgiQFAQQy4VRGQ.YnZG8AsMP22yw9zdTd2IBw2JkhMVXJG' WHERE username = 'admin';

-- 插入测试用户 (密码: 123456)
INSERT INTO sys_user (username, password, phone, email, real_name, avatar, status, last_login_at, created_at) VALUES
-- 患者
('patient1', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800001001', 'patient1@test.com', '张三', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 30 DAY)),
('patient2', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800001002', 'patient2@test.com', '李四', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 25 DAY)),
('patient3', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800001003', 'patient3@test.com', '王五', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 20 DAY)),
('patient4', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800001004', 'patient4@test.com', '赵六', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 15 DAY)),
('patient5', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800001005', 'patient5@test.com', '钱七', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 基层医生
('doctor1', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800002001', 'doctor1@test.com', '陈医生', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 60 DAY)),
('doctor2', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800002002', 'doctor2@test.com', '林医生', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 55 DAY)),
('doctor3', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800002003', 'doctor3@test.com', '黄医生', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 50 DAY)),
-- 专家医生
('expert1', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800003001', 'expert1@test.com', '王教授', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 90 DAY)),
('expert2', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800003002', 'expert2@test.com', '李教授', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 85 DAY)),
-- 药师
('pharmacist1', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800004001', 'pharmacist1@test.com', '周药师', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 45 DAY)),
('pharmacist2', '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK', '13800004002', 'pharmacist2@test.com', '吴药师', NULL, 1, NOW(), DATE_SUB(NOW(), INTERVAL 40 DAY));

-- 分配用户角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username LIKE 'patient%' AND r.role_code = 'PATIENT';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username LIKE 'doctor%' AND r.role_code = 'DOCTOR_PRIMARY';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username LIKE 'expert%' AND r.role_code = 'DOCTOR_EXPERT';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username LIKE 'pharmacist%' AND r.role_code = 'PHARMACIST';


-- =============================================
-- 2. 医院与科室数据
-- =============================================

INSERT INTO org_hospital (name, level, address, phone, status) VALUES
('耳康云诊中心医院', '三级甲等', '北京市朝阳区健康路100号', '010-12345678', 1),
('耳康社区卫生服务中心', '一级', '北京市海淀区社区路50号', '010-87654321', 1);

INSERT INTO org_department (hospital_id, name, description, sort_order, status) VALUES
(1, '耳科', '耳部疾病诊治，包括中耳炎、耳鸣、听力下降等', 1, 1),
(1, '鼻科', '鼻部疾病诊治，包括鼻炎、鼻窦炎、鼻息肉等', 2, 1),
(1, '咽喉科', '咽喉疾病诊治，包括咽炎、扁桃体炎、声带疾病等', 3, 1),
(1, '头颈外科', '头颈部肿瘤诊治', 4, 1),
(2, '全科', '常见耳鼻喉疾病诊治', 1, 1);

-- =============================================
-- 3. 患者档案数据
-- =============================================

INSERT INTO patient_profile (user_id, gender, birth_date, id_card, address, emergency_contact, emergency_phone, medical_history, allergy_history)
SELECT id, 1, '1985-03-15', '110101198503150011', '北京市朝阳区XX小区1号楼', '张母', '13900001001', '高血压病史5年', '青霉素过敏' FROM sys_user WHERE username = 'patient1';

INSERT INTO patient_profile (user_id, gender, birth_date, id_card, address, emergency_contact, emergency_phone, medical_history, allergy_history)
SELECT id, 0, '1990-07-22', '110101199007220022', '北京市海淀区YY小区2号楼', '李父', '13900001002', '无', '无' FROM sys_user WHERE username = 'patient2';

INSERT INTO patient_profile (user_id, gender, birth_date, id_card, address, emergency_contact, emergency_phone, medical_history, allergy_history)
SELECT id, 1, '1978-11-08', '110101197811080033', '北京市西城区ZZ小区3号楼', '王妻', '13900001003', '糖尿病病史3年', '磺胺类药物过敏' FROM sys_user WHERE username = 'patient3';

INSERT INTO patient_profile (user_id, gender, birth_date, id_card, address, emergency_contact, emergency_phone, medical_history, allergy_history)
SELECT id, 0, '1995-05-20', '110101199505200044', '北京市东城区AA小区4号楼', '赵母', '13900001004', '无', '无' FROM sys_user WHERE username = 'patient4';

INSERT INTO patient_profile (user_id, gender, birth_date, id_card, address, emergency_contact, emergency_phone, medical_history, allergy_history)
SELECT id, 1, '1982-09-12', '110101198209120055', '北京市丰台区BB小区5号楼', '钱妻', '13900001005', '慢性鼻炎病史10年', '无' FROM sys_user WHERE username = 'patient5';

-- =============================================
-- 4. 医生档案数据
-- =============================================

INSERT INTO doctor_profile (user_id, hospital_id, department_id, title, specialty, introduction, license_no, consultation_fee, is_expert, status)
SELECT id, 1, 1, '主治医师', '中耳炎、耳鸣', '从事耳科临床工作10年，擅长中耳炎、耳鸣等疾病的诊治', 'D110101001', 50.00, 0, 1 FROM sys_user WHERE username = 'doctor1';

INSERT INTO doctor_profile (user_id, hospital_id, department_id, title, specialty, introduction, license_no, consultation_fee, is_expert, status)
SELECT id, 1, 2, '主治医师', '鼻炎、鼻窦炎', '从事鼻科临床工作8年，擅长各类鼻炎、鼻窦炎的诊治', 'D110101002', 50.00, 0, 1 FROM sys_user WHERE username = 'doctor2';

INSERT INTO doctor_profile (user_id, hospital_id, department_id, title, specialty, introduction, license_no, consultation_fee, is_expert, status)
SELECT id, 1, 3, '主治医师', '咽炎、扁桃体炎', '从事咽喉科临床工作6年，擅长咽喉部疾病的诊治', 'D110101003', 50.00, 0, 1 FROM sys_user WHERE username = 'doctor3';

INSERT INTO doctor_profile (user_id, hospital_id, department_id, title, specialty, introduction, license_no, consultation_fee, is_expert, status)
SELECT id, 1, 1, '主任医师', '耳科疑难杂症、人工耳蜗', '从事耳科临床工作25年，国内知名耳科专家，擅长耳科疑难杂症及人工耳蜗手术', 'D110101004', 200.00, 1, 1 FROM sys_user WHERE username = 'expert1';

INSERT INTO doctor_profile (user_id, hospital_id, department_id, title, specialty, introduction, license_no, consultation_fee, is_expert, status)
SELECT id, 1, 4, '主任医师', '头颈部肿瘤', '从事头颈外科临床工作20年，擅长头颈部肿瘤的诊断与手术治疗', 'D110101005', 200.00, 1, 1 FROM sys_user WHERE username = 'expert2';


-- =============================================
-- 5. 预约数据
-- =============================================

-- 获取用户ID变量（MySQL不支持变量赋值，使用子查询）
INSERT INTO appointment (patient_id, doctor_id, appointment_date, time_slot, chief_complaint, status, created_at) VALUES
((SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), CURDATE(), '09:00-09:30', '左耳听力下降一周', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
((SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor2'), CURDATE(), '10:00-10:30', '鼻塞流涕两周', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), CURDATE(), '14:00-14:30', '咽喉疼痛三天', 'PENDING', NOW()),
((SELECT id FROM sys_user WHERE username='patient4'), (SELECT id FROM sys_user WHERE username='doctor1'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30-10:00', '耳鸣一个月', 'PENDING', NOW()),
((SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00-10:30', '听力严重下降，需专家会诊', 'CONFIRMED', NOW()),
((SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor2'), DATE_SUB(CURDATE(), INTERVAL 7 DAY), '15:00-15:30', '过敏性鼻炎复诊', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
((SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor1'), DATE_SUB(CURDATE(), INTERVAL 14 DAY), '11:00-11:30', '中耳炎复查', 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 17 DAY));

-- =============================================
-- 6. 问诊数据
-- =============================================

INSERT INTO consultation (appointment_id, patient_id, doctor_id, consultation_no, consultation_type, status, start_time, end_time, duration, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), 'CON20260101001', 'VIDEO', 'FINISHED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 30, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor2'), 'CON20260101002', 'VIDEO', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor2'), 'CON20251225001', 'VIDEO', 'FINISHED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 25 MINUTE, 25, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor1'), 'CON20251218001', 'TEXT', 'FINISHED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 20 MINUTE, 20, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(NULL, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'CON20251220001', 'VIDEO', 'FINISHED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 35 MINUTE, 35, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(NULL, (SELECT id FROM sys_user WHERE username='patient4'), (SELECT id FROM sys_user WHERE username='doctor1'), 'CON20251222001', 'PHONE', 'FINISHED', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 15 MINUTE, 15, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(NULL, (SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), 'CON20251228001', 'VIDEO', 'FINISHED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 45 MINUTE, 45, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(NULL, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), 'CON20251230001', 'VIDEO', 'CANCELED', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'CON20260101003', 'VIDEO', 'WAITING', NULL, NULL, NULL, NOW());

-- =============================================
-- 7. 聊天消息数据
-- =============================================

INSERT INTO chat_message (consultation_id, sender_id, sender_type, content_type, content, is_read, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='patient1'), 'PATIENT', 'TEXT', '医生您好，我左耳听力下降已经一周了', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, (SELECT id FROM sys_user WHERE username='doctor1'), 'DOCTOR', 'TEXT', '您好，请问是突然发生的还是逐渐加重的？', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR) + INTERVAL 2 MINUTE),
(1, (SELECT id FROM sys_user WHERE username='patient1'), 'PATIENT', 'TEXT', '是逐渐加重的，最近感冒了', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR) + INTERVAL 5 MINUTE),
(1, (SELECT id FROM sys_user WHERE username='doctor1'), 'DOCTOR', 'TEXT', '了解了，可能是分泌性中耳炎，我给您开个处方', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR) + INTERVAL 8 MINUTE),
(2, (SELECT id FROM sys_user WHERE username='patient2'), 'PATIENT', 'TEXT', '医生，我鼻塞很严重，晚上睡不好', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, (SELECT id FROM sys_user WHERE username='doctor2'), 'DOCTOR', 'TEXT', '请问有流鼻涕吗？是清水样还是黄脓样？', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR) + INTERVAL 3 MINUTE),
(2, (SELECT id FROM sys_user WHERE username='patient2'), 'PATIENT', 'TEXT', '有的，是黄色的鼻涕', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));


-- =============================================
-- 8. 病历数据
-- =============================================

INSERT INTO medical_record (consultation_id, patient_id, doctor_id, record_no, chief_complaint, present_illness, past_history, allergy_history, physical_exam, diagnosis, treatment_plan, followup_advice, status, submitted_at, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), 'MR20260101001', 
'左耳听力下降一周', 
'患者一周前感冒后出现左耳听力下降，伴耳闷感，无耳痛、耳流脓，无眩晕、耳鸣。', 
'高血压病史5年，规律服药控制良好', 
'青霉素过敏', 
'左耳鼓膜内陷，鼓室积液征阳性，右耳正常', 
'分泌性中耳炎（左）', 
'1. 口服抗生素治疗感染\n2. 鼻腔喷雾剂改善咽鼓管功能\n3. 必要时行鼓膜穿刺', 
'一周后复诊，如症状加重及时就诊', 
'SUBMITTED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)),

(3, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor2'), 'MR20251225001', 
'过敏性鼻炎复诊', 
'患者过敏性鼻炎病史3年，近期症状加重，鼻塞、流清涕、打喷嚏明显。', 
'高血压病史5年', 
'青霉素过敏', 
'双侧鼻甲肿大，鼻腔黏膜苍白水肿', 
'过敏性鼻炎', 
'1. 继续使用鼻用糖皮质激素\n2. 口服抗组胺药\n3. 避免接触过敏原', 
'一个月后复诊', 
'SUBMITTED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

(4, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor1'), 'MR20251218001', 
'中耳炎复查', 
'患者两周前因急性中耳炎就诊，经抗生素治疗后症状好转，今日复查。', 
'无特殊', 
'无', 
'左耳鼓膜轻度充血，无穿孔，听力恢复正常', 
'急性中耳炎（恢复期）', 
'继续观察，注意预防感冒', 
'如有不适及时就诊', 
'SUBMITTED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),

(5, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'MR20251220001', 
'咽喉疼痛', 
'患者三天前出现咽喉疼痛，吞咽时加重，伴发热（38.5℃），无咳嗽。', 
'糖尿病病史3年', 
'磺胺类药物过敏', 
'咽部充血，双侧扁桃体II度肿大，表面可见脓点', 
'急性化脓性扁桃体炎', 
'1. 头孢类抗生素治疗\n2. 退热药物对症处理\n3. 多饮水，清淡饮食', 
'三天后复诊，如高热不退及时就诊', 
'SUBMITTED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),

(7, (SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), 'MR20251228001', 
'听力严重下降', 
'患者双耳听力进行性下降5年，近半年加重明显，影响日常交流。', 
'慢性鼻炎病史10年', 
'无', 
'双耳鼓膜完整，纯音测听示双耳重度感音神经性聋', 
'双耳感音神经性聋（重度）', 
'1. 建议行人工耳蜗植入术评估\n2. 完善术前检查\n3. 暂时配戴助听器', 
'两周后复诊，讨论手术方案', 
'SUBMITTED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));


-- =============================================
-- 9. 处方数据
-- =============================================

INSERT INTO prescription (consultation_id, patient_id, doctor_id, prescription_no, diagnosis, status, submitted_at, approved_at, notes, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), 'RX20260101001', 
'分泌性中耳炎（左）', 'APPROVED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 
'注意休息，多饮水', DATE_SUB(NOW(), INTERVAL 3 HOUR)),

(3, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor2'), 'RX20251225001', 
'过敏性鼻炎', 'APPROVED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 30 MINUTE, 
'避免接触过敏原', DATE_SUB(NOW(), INTERVAL 7 DAY)),

(4, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor1'), 'RX20251218001', 
'急性中耳炎（恢复期）', 'APPROVED', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 20 MINUTE, 
'继续观察', DATE_SUB(NOW(), INTERVAL 14 DAY)),

(5, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'RX20251220001', 
'急性化脓性扁桃体炎', 'APPROVED', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 25 MINUTE, 
'注意血糖监测', DATE_SUB(NOW(), INTERVAL 12 DAY)),

(2, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor2'), 'RX20260101002', 
'急性鼻窦炎', 'PENDING_REVIEW', NOW(), NULL, 
'多休息', NOW()),

(6, (SELECT id FROM sys_user WHERE username='patient4'), (SELECT id FROM sys_user WHERE username='doctor1'), 'RX20251222001', 
'神经性耳鸣', 'REJECTED', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, 
'建议进一步检查', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =============================================
-- 10. 处方明细数据
-- =============================================

INSERT INTO prescription_item (prescription_id, drug_name, drug_spec, dosage, frequency, duration, quantity, unit, notes, sort_order) VALUES
-- 处方1的药品
(1, '阿莫西林胶囊', '0.5g*24粒', '0.5g', '每日3次', '7天', 2, '盒', '饭后服用', 1),
(1, '盐酸羟甲唑啉喷雾剂', '10ml', '每侧鼻孔2喷', '每日2次', '5天', 1, '瓶', '不超过7天', 2),
(1, '桉柠蒎肠溶软胶囊', '0.3g*20粒', '0.3g', '每日3次', '7天', 1, '盒', '饭前服用', 3),

-- 处方2的药品
(2, '布地奈德鼻喷雾剂', '120喷', '每侧鼻孔2喷', '每日2次', '1个月', 1, '瓶', '长期使用', 1),
(2, '氯雷他定片', '10mg*6片', '10mg', '每日1次', '2周', 3, '盒', '睡前服用', 2),

-- 处方3的药品
(3, '维生素B族片', '100片', '2片', '每日2次', '2周', 1, '瓶', '饭后服用', 1),

-- 处方4的药品
(4, '头孢克肟分散片', '0.1g*6片', '0.1g', '每日2次', '5天', 2, '盒', '饭后服用', 1),
(4, '布洛芬缓释胶囊', '0.3g*20粒', '0.3g', '发热时服用', '按需', 1, '盒', '体温超过38.5℃时服用', 2),
(4, '复方硼砂含漱液', '250ml', '适量', '每日3-4次', '5天', 1, '瓶', '含漱后吐出', 3),

-- 处方5的药品
(5, '阿莫西林克拉维酸钾片', '0.375g*6片', '0.375g', '每日3次', '7天', 3, '盒', '饭后服用', 1),
(5, '盐酸羟甲唑啉喷雾剂', '10ml', '每侧鼻孔2喷', '每日2次', '5天', 1, '瓶', '不超过7天', 2),

-- 处方6的药品
(6, '甲钴胺片', '0.5mg*20片', '0.5mg', '每日3次', '1个月', 2, '盒', '饭后服用', 1),
(6, '银杏叶提取物片', '40mg*24片', '40mg', '每日3次', '1个月', 2, '盒', '改善微循环', 2);


-- =============================================
-- 11. 审方记录数据
-- =============================================

INSERT INTO pharmacy_review (prescription_id, reviewer_id, review_status, risk_level, high_risk_items, medium_risk_items, low_risk_items, reject_reason, suggestion, ai_risk_hint, reviewed_at, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='pharmacist1'), 'APPROVED', 'LOW', NULL, NULL, '用药剂量在正常范围内', NULL, '处方合理，可以发药', '未发现明显风险', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

(2, (SELECT id FROM sys_user WHERE username='pharmacist1'), 'APPROVED', 'LOW', NULL, NULL, NULL, NULL, '长期用药，注意定期复查', '鼻用激素长期使用需监测', DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL 30 MINUTE, DATE_SUB(NOW(), INTERVAL 7 DAY)),

(3, (SELECT id FROM sys_user WHERE username='pharmacist2'), 'APPROVED', 'LOW', NULL, NULL, NULL, NULL, '处方合理', '无特殊风险', DATE_SUB(NOW(), INTERVAL 14 DAY) + INTERVAL 20 MINUTE, DATE_SUB(NOW(), INTERVAL 14 DAY)),

(4, (SELECT id FROM sys_user WHERE username='pharmacist1'), 'APPROVED', 'MEDIUM', NULL, '患者有糖尿病史，使用头孢类抗生素需注意', NULL, NULL, '注意监测血糖', '糖尿病患者用药需谨慎', DATE_SUB(NOW(), INTERVAL 12 DAY) + INTERVAL 25 MINUTE, DATE_SUB(NOW(), INTERVAL 12 DAY)),

(6, (SELECT id FROM sys_user WHERE username='pharmacist2'), 'REJECTED', 'HIGH', '甲钴胺与银杏叶提取物联用可能增加出血风险', NULL, NULL, '建议调整用药方案，避免联合用药风险', '请医生重新评估用药方案', '检测到潜在药物相互作用', DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 1 HOUR, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =============================================
-- 12. 转诊数据
-- =============================================

INSERT INTO referral (consultation_id, patient_id, from_doctor_id, to_doctor_id, to_hospital_id, to_department_id, referral_no, referral_reason, clinical_summary, status, accepted_at, new_consultation_id, created_at) VALUES
(6, (SELECT id FROM sys_user WHERE username='patient4'), (SELECT id FROM sys_user WHERE username='doctor1'), (SELECT id FROM sys_user WHERE username='expert1'), 1, 1, 'REF20251222001', 
'患者耳鸣症状持续，常规治疗效果不佳，建议专家会诊', 
'患者，男，40岁，耳鸣一个月，经初步治疗效果不明显，纯音测听示轻度感音神经性聋，建议专家进一步评估。', 
'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 DAY), 7, DATE_SUB(NOW(), INTERVAL 10 DAY)),

(5, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), (SELECT id FROM sys_user WHERE username='expert2'), 1, 4, 'REF20251220001', 
'患者扁桃体反复感染，建议评估手术指征', 
'患者，男，47岁，急性化脓性扁桃体炎，既往有反复发作史，建议头颈外科评估是否需要手术治疗。', 
'PENDING', NULL, NULL, DATE_SUB(NOW(), INTERVAL 11 DAY)),

(7, (SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), (SELECT id FROM sys_user WHERE username='expert2'), 1, 4, 'REF20251228001', 
'患者拟行人工耳蜗植入术，需头颈外科协助评估', 
'患者，男，43岁，双耳重度感音神经性聋，拟行人工耳蜗植入术，需头颈外科协助术前评估。', 
'ACCEPTED', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, DATE_SUB(NOW(), INTERVAL 4 DAY));


-- =============================================
-- 13. MDT会诊数据
-- =============================================

INSERT INTO mdt_case (consultation_id, patient_id, initiator_id, mdt_no, title, clinical_summary, discussion_points, scheduled_time, actual_start_time, actual_end_time, status, created_at) VALUES
(7, (SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), 'MDT20251230001', 
'人工耳蜗植入术术前多学科讨论', 
'患者，男，43岁，双耳重度感音神经性聋5年，进行性加重，影响日常交流，拟行人工耳蜗植入术。', 
'1. 手术适应症评估\n2. 术前准备事项\n3. 手术方案讨论\n4. 术后康复计划', 
DATE_ADD(NOW(), INTERVAL 3 DAY), NULL, NULL, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY)),

(5, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'MDT20251225001', 
'反复扁桃体炎手术指征讨论', 
'患者，男，47岁，急性化脓性扁桃体炎反复发作，近一年发作4次以上，影响生活质量。', 
'1. 手术指征评估\n2. 手术方式选择\n3. 围手术期管理', 
DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- MDT参会成员
INSERT INTO mdt_member (mdt_id, doctor_id, role, invite_status, join_time, leave_time) VALUES
(1, (SELECT id FROM sys_user WHERE username='expert1'), 'INITIATOR', 'ACCEPTED', NULL, NULL),
(1, (SELECT id FROM sys_user WHERE username='expert2'), 'PARTICIPANT', 'ACCEPTED', NULL, NULL),
(1, (SELECT id FROM sys_user WHERE username='doctor1'), 'PARTICIPANT', 'PENDING', NULL, NULL),
(2, (SELECT id FROM sys_user WHERE username='doctor3'), 'INITIATOR', 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR),
(2, (SELECT id FROM sys_user WHERE username='expert2'), 'PARTICIPANT', 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR),
(2, (SELECT id FROM sys_user WHERE username='doctor1'), 'PARTICIPANT', 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 5 MINUTE, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 1 HOUR);

-- MDT会诊结论
INSERT INTO mdt_conclusion (mdt_id, conclusion, treatment_plan, followup_plan, recorder_id) VALUES
(2, '经多学科讨论，患者符合扁桃体切除术手术指征，建议择期手术。', 
'1. 完善术前检查\n2. 控制血糖在理想范围\n3. 择期行扁桃体切除术\n4. 术后抗感染治疗', 
'术后一周复查，观察伤口愈合情况', 
(SELECT id FROM sys_user WHERE username='expert2'));

-- =============================================
-- 14. 随访计划数据
-- =============================================

INSERT INTO followup_plan (consultation_id, patient_id, doctor_id, plan_no, diagnosis, followup_type, interval_days, total_times, completed_times, next_followup_date, question_list, red_flags, status, created_at) VALUES
(1, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor1'), 'FP20260101001', 
'分泌性中耳炎', 'REGULAR', 7, 4, 0, DATE_ADD(CURDATE(), INTERVAL 7 DAY), 
'["听力是否改善？", "耳闷感是否减轻？", "是否有耳痛？", "是否按时服药？"]', 
'["听力突然下降", "剧烈耳痛", "高热", "耳流脓"]', 
'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR)),

(3, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor2'), 'FP20251225001', 
'过敏性鼻炎', 'CHRONIC', 30, 12, 1, DATE_ADD(CURDATE(), INTERVAL 23 DAY), 
'["鼻塞症状如何？", "是否有流涕？", "打喷嚏频率？", "是否规律用药？"]', 
'["呼吸困难", "嗅觉丧失", "鼻出血"]', 
'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY)),

(5, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor3'), 'FP20251220001', 
'急性化脓性扁桃体炎', 'REGULAR', 3, 3, 3, NULL, 
'["咽痛是否缓解？", "体温是否正常？", "是否能正常进食？"]', 
'["高热不退", "呼吸困难", "吞咽困难加重"]', 
'COMPLETED', DATE_SUB(NOW(), INTERVAL 12 DAY)),

(7, (SELECT id FROM sys_user WHERE username='patient5'), (SELECT id FROM sys_user WHERE username='expert1'), 'FP20251228001', 
'双耳感音神经性聋', 'REGULAR', 14, 6, 1, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 
'["助听器使用情况？", "日常交流是否改善？", "是否有头晕？"]', 
'["突发眩晕", "听力急剧下降", "耳鸣加重"]', 
'ACTIVE', DATE_SUB(NOW(), INTERVAL 4 DAY));


-- =============================================
-- 15. 随访记录数据
-- =============================================

INSERT INTO followup_record (plan_id, patient_id, record_no, followup_date, symptoms, answers, has_red_flag, doctor_comment, next_action, status, submitted_at, reviewed_at, reviewer_id, created_at) VALUES
(2, (SELECT id FROM sys_user WHERE username='patient1'), 'FR20260101001', CURDATE(), 
'鼻塞症状有所改善，偶有流涕', 
'{"q1": "明显改善", "q2": "偶尔有", "q3": "每天2-3次", "q4": "是"}', 
0, '症状控制良好，继续当前治疗方案', '继续用药，一个月后复诊', 
'REVIEWED', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), (SELECT id FROM sys_user WHERE username='doctor2'), DATE_SUB(NOW(), INTERVAL 1 DAY)),

(3, (SELECT id FROM sys_user WHERE username='patient3'), 'FR20251221001', DATE_SUB(CURDATE(), INTERVAL 11 DAY), 
'咽痛明显缓解，体温正常', 
'{"q1": "明显缓解", "q2": "正常", "q3": "可以正常进食"}', 
0, '恢复良好', '继续观察', 
'REVIEWED', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), (SELECT id FROM sys_user WHERE username='doctor3'), DATE_SUB(NOW(), INTERVAL 11 DAY)),

(3, (SELECT id FROM sys_user WHERE username='patient3'), 'FR20251224001', DATE_SUB(CURDATE(), INTERVAL 8 DAY), 
'症状基本消失', 
'{"q1": "无疼痛", "q2": "正常", "q3": "正常"}', 
0, '痊愈', '无需继续随访', 
'REVIEWED', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), (SELECT id FROM sys_user WHERE username='doctor3'), DATE_SUB(NOW(), INTERVAL 8 DAY)),

(3, (SELECT id FROM sys_user WHERE username='patient3'), 'FR20251227001', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 
'完全恢复', 
'{"q1": "无", "q2": "正常", "q3": "正常"}', 
0, '随访计划完成', '建议择期手术评估', 
'REVIEWED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), (SELECT id FROM sys_user WHERE username='doctor3'), DATE_SUB(NOW(), INTERVAL 5 DAY)),

(4, (SELECT id FROM sys_user WHERE username='patient5'), 'FR20251231001', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 
'助听器适应中，交流有所改善', 
'{"q1": "每天佩戴8小时", "q2": "有改善", "q3": "无"}', 
0, '适应良好，继续佩戴', '两周后复诊，讨论手术事宜', 
'REVIEWED', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), (SELECT id FROM sys_user WHERE username='expert1'), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 16. AI任务数据
-- =============================================

INSERT INTO ai_task (task_type, related_id, related_type, user_id, request_data, response_data, status, tokens_used, latency_ms, created_at, completed_at) VALUES
('HEALTH_QA', NULL, NULL, (SELECT id FROM sys_user WHERE username='patient1'), 
'{"question": "耳朵进水了怎么办？"}', 
'{"answer": "耳朵进水后，可以尝试以下方法：1. 将头偏向进水的一侧，轻轻跳动让水流出；2. 用干净的棉签轻轻吸干外耳道的水；3. 可以用吹风机低温档从远处吹干。如果水进入较深或伴有疼痛，建议及时就医。"}', 
'COMPLETED', 256, 1200, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),

('RECORD_SUMMARY', 1, 'CONSULTATION', (SELECT id FROM sys_user WHERE username='doctor1'), 
'{"consultation_id": 1, "messages": "..."}', 
'{"summary": "患者因左耳听力下降一周就诊，感冒后发病，诊断为分泌性中耳炎，给予抗生素及鼻腔喷雾剂治疗。"}', 
'COMPLETED', 512, 2500, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

('RISK_CHECK', 1, 'PRESCRIPTION', (SELECT id FROM sys_user WHERE username='pharmacist1'), 
'{"prescription_id": 1, "drugs": ["阿莫西林", "盐酸羟甲唑啉", "桉柠蒎"]}', 
'{"risk_level": "LOW", "warnings": [], "suggestions": ["用药剂量在正常范围内"]}', 
'COMPLETED', 384, 1800, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

('FOLLOWUP_GEN', 1, 'CONSULTATION', (SELECT id FROM sys_user WHERE username='doctor1'), 
'{"diagnosis": "分泌性中耳炎", "treatment": "抗生素治疗"}', 
'{"questions": ["听力是否改善？", "耳闷感是否减轻？", "是否有耳痛？", "是否按时服药？"], "red_flags": ["听力突然下降", "剧烈耳痛", "高热", "耳流脓"], "interval_days": 7}', 
'COMPLETED', 448, 2100, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)),

('HEALTH_QA', NULL, NULL, (SELECT id FROM sys_user WHERE username='patient2'), 
'{"question": "鼻炎能根治吗？"}', 
'{"answer": "鼻炎的治疗效果因类型而异。过敏性鼻炎目前难以根治，但可以通过规范治疗有效控制症状；急性鼻炎（感冒引起）通常可以痊愈；慢性鼻炎需要长期管理。建议：1. 明确鼻炎类型；2. 规范用药；3. 避免诱发因素；4. 定期复查。"}', 
'COMPLETED', 320, 1500, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));


-- =============================================
-- 17. AI对话会话数据
-- =============================================

INSERT INTO ai_chat_session (user_id, session_type, title, status, created_at) VALUES
((SELECT id FROM sys_user WHERE username='patient1'), 'HEALTH_QA', '耳朵护理咨询', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM sys_user WHERE username='patient2'), 'HEALTH_QA', '鼻炎相关问题', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
((SELECT id FROM sys_user WHERE username='patient3'), 'HEALTH_QA', '咽喉保健咨询', 'CLOSED', DATE_SUB(NOW(), INTERVAL 10 DAY)),
((SELECT id FROM sys_user WHERE username='patient4'), 'HEALTH_QA', '耳鸣问题咨询', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- AI对话消息
INSERT INTO ai_chat_message (session_id, role, content, tokens, created_at) VALUES
(1, 'USER', '耳朵进水了怎么办？', 12, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'ASSISTANT', '耳朵进水后，可以尝试以下方法：1. 将头偏向进水的一侧，轻轻跳动让水流出；2. 用干净的棉签轻轻吸干外耳道的水；3. 可以用吹风机低温档从远处吹干。如果水进入较深或伴有疼痛，建议及时就医。', 128, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 'USER', '游泳时怎么预防耳朵进水？', 15, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 5 MINUTE),
(1, 'ASSISTANT', '游泳时预防耳朵进水的方法：1. 佩戴专业游泳耳塞；2. 戴泳帽覆盖耳朵；3. 游泳后及时清理耳道；4. 避免在不干净的水域游泳；5. 如有中耳炎等耳部疾病，建议暂时避免游泳。', 96, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 5 MINUTE),

(2, 'USER', '鼻炎能根治吗？', 8, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'ASSISTANT', '鼻炎的治疗效果因类型而异。过敏性鼻炎目前难以根治，但可以通过规范治疗有效控制症状；急性鼻炎（感冒引起）通常可以痊愈；慢性鼻炎需要长期管理。建议：1. 明确鼻炎类型；2. 规范用药；3. 避免诱发因素；4. 定期复查。', 160, DATE_SUB(NOW(), INTERVAL 3 DAY)),

(3, 'USER', '嗓子经常发炎怎么办？', 12, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 'ASSISTANT', '嗓子经常发炎可能与以下因素有关：1. 用嗓过度；2. 慢性咽炎；3. 胃食管反流；4. 环境因素。建议：1. 多喝水，保持咽喉湿润；2. 避免辛辣刺激食物；3. 戒烟限酒；4. 注意休息；5. 如反复发作，建议就医检查。', 144, DATE_SUB(NOW(), INTERVAL 10 DAY)),

(4, 'USER', '耳鸣是什么原因引起的？', 14, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 'ASSISTANT', '耳鸣的常见原因包括：1. 噪音损伤；2. 年龄相关的听力下降；3. 耳部疾病（中耳炎、耳硬化症等）；4. 血管问题；5. 药物副作用；6. 压力和焦虑。建议进行专业检查，明确病因后针对性治疗。', 128, DATE_SUB(NOW(), INTERVAL 8 DAY));

-- =============================================
-- 18. 审计日志数据
-- =============================================

INSERT INTO audit_log (user_id, username, user_role, action, module, target_type, target_id, target_desc, request_method, request_url, ip_address, duration_ms, created_at) VALUES
((SELECT id FROM sys_user WHERE username='admin'), 'admin', 'ADMIN', 'LOGIN', 'auth', 'USER', 1, '管理员登录', 'POST', '/api/auth/login', '127.0.0.1', 150, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
((SELECT id FROM sys_user WHERE username='doctor1'), 'doctor1', 'DOCTOR_PRIMARY', 'LOGIN', 'auth', 'USER', (SELECT id FROM sys_user WHERE username='doctor1'), '医生登录', 'POST', '/api/auth/login', '192.168.1.100', 120, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
((SELECT id FROM sys_user WHERE username='doctor1'), 'doctor1', 'DOCTOR_PRIMARY', 'CREATE_PRESCRIPTION', 'prescription', 'PRESCRIPTION', 1, '开具处方 RX20260101001', 'POST', '/api/prescriptions', '192.168.1.100', 250, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
((SELECT id FROM sys_user WHERE username='pharmacist1'), 'pharmacist1', 'PHARMACIST', 'REVIEW_PRESCRIPTION', 'prescription', 'PRESCRIPTION', 1, '审核处方 RX20260101001', 'POST', '/api/pharmacy/review', '192.168.1.101', 180, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
((SELECT id FROM sys_user WHERE username='patient1'), 'patient1', 'PATIENT', 'VIEW_RECORD', 'medical_record', 'MEDICAL_RECORD', 1, '查看病历 MR20260101001', 'GET', '/api/medical-records/1', '192.168.1.102', 80, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
((SELECT id FROM sys_user WHERE username='expert1'), 'expert1', 'DOCTOR_EXPERT', 'CREATE_MDT', 'mdt', 'MDT_CASE', 1, '发起MDT会诊 MDT20251230001', 'POST', '/api/mdt', '192.168.1.103', 200, DATE_SUB(NOW(), INTERVAL 2 DAY)),
((SELECT id FROM sys_user WHERE username='doctor1'), 'doctor1', 'DOCTOR_PRIMARY', 'CREATE_REFERRAL', 'referral', 'REFERRAL', 1, '发起转诊 REF20251222001', 'POST', '/api/referrals', '192.168.1.100', 180, DATE_SUB(NOW(), INTERVAL 10 DAY)),
((SELECT id FROM sys_user WHERE username='admin'), 'admin', 'ADMIN', 'VIEW_STATS', 'stats', NULL, NULL, '查看系统统计', 'GET', '/api/admin/stats', '127.0.0.1', 350, NOW()),
((SELECT id FROM sys_user WHERE username='admin'), 'admin', 'ADMIN', 'EXPORT_STATS', 'stats', NULL, NULL, '导出统计数据', 'GET', '/api/stats/export', '127.0.0.1', 1200, DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM sys_user WHERE username='pharmacist2'), 'pharmacist2', 'PHARMACIST', 'REJECT_PRESCRIPTION', 'prescription', 'PRESCRIPTION', 6, '驳回处方 RX20251222001', 'POST', '/api/pharmacy/review', '192.168.1.104', 160, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =============================================
-- 19. 权限数据
-- =============================================

-- 插入权限定义
INSERT INTO sys_permission (perm_code, perm_name, perm_type, parent_id, path, icon, sort_order) VALUES
-- 一级菜单
('dashboard', '工作台', 'menu', 0, '/dashboard', 'DashboardOutlined', 1),
('patient', '患者管理', 'menu', 0, '/patient', 'UserOutlined', 2),
('consultation', '问诊管理', 'menu', 0, '/consultation', 'MessageOutlined', 3),
('prescription', '处方管理', 'menu', 0, '/prescription', 'MedicineBoxOutlined', 4),
('pharmacy', '药房审核', 'menu', 0, '/pharmacy', 'ExperimentOutlined', 5),
('referral', '转诊管理', 'menu', 0, '/referral', 'SwapOutlined', 6),
('mdt', 'MDT会诊', 'menu', 0, '/mdt', 'TeamOutlined', 7),
('followup', '随访管理', 'menu', 0, '/followup', 'ScheduleOutlined', 8),
('ai', 'AI助手', 'menu', 0, '/ai', 'RobotOutlined', 9),
('admin', '系统管理', 'menu', 0, '/admin', 'SettingOutlined', 10),

-- 患者管理子菜单
('patient:list', '患者列表', 'menu', 2, '/patient/list', NULL, 1),
('patient:profile', '患者档案', 'menu', 2, '/patient/profile', NULL, 2),
('patient:appointment', '预约挂号', 'menu', 2, '/patient/appointment', NULL, 3),

-- 问诊管理子菜单
('consultation:waiting', '候诊列表', 'menu', 3, '/consultation/waiting', NULL, 1),
('consultation:room', '问诊室', 'menu', 3, '/consultation/room', NULL, 2),
('consultation:history', '问诊记录', 'menu', 3, '/consultation/history', NULL, 3),
('consultation:record', '病历管理', 'menu', 3, '/consultation/record', NULL, 4),

-- 处方管理子菜单
('prescription:create', '开具处方', 'menu', 4, '/prescription/create', NULL, 1),
('prescription:list', '处方列表', 'menu', 4, '/prescription/list', NULL, 2),
('prescription:detail', '处方详情', 'menu', 4, '/prescription/detail', NULL, 3),

-- 药房审核子菜单
('pharmacy:pending', '待审处方', 'menu', 5, '/pharmacy/pending', NULL, 1),
('pharmacy:history', '审核历史', 'menu', 5, '/pharmacy/history', NULL, 2),

-- 转诊管理子菜单
('referral:create', '发起转诊', 'menu', 6, '/referral/create', NULL, 1),
('referral:received', '收到转诊', 'menu', 6, '/referral/received', NULL, 2),
('referral:history', '转诊记录', 'menu', 6, '/referral/history', NULL, 3),

-- MDT会诊子菜单
('mdt:create', '发起会诊', 'menu', 7, '/mdt/create', NULL, 1),
('mdt:list', '会诊列表', 'menu', 7, '/mdt/list', NULL, 2),
('mdt:room', '会诊室', 'menu', 7, '/mdt/room', NULL, 3),

-- 随访管理子菜单
('followup:plan', '随访计划', 'menu', 8, '/followup/plan', NULL, 1),
('followup:record', '随访记录', 'menu', 8, '/followup/record', NULL, 2),

-- AI助手子菜单
('ai:chat', '健康咨询', 'menu', 9, '/ai/chat', NULL, 1),
('ai:summary', '病历摘要', 'menu', 9, '/ai/summary', NULL, 2),

-- 系统管理子菜单
('admin:user', '用户管理', 'menu', 10, '/admin/user', NULL, 1),
('admin:role', '角色管理', 'menu', 10, '/admin/role', NULL, 2),
('admin:audit', '审计日志', 'menu', 10, '/admin/audit', NULL, 3),
('admin:stats', '数据统计', 'menu', 10, '/admin/stats', NULL, 4),

-- 按钮权限
('patient:create', '新增患者', 'button', 2, NULL, NULL, 100),
('patient:edit', '编辑患者', 'button', 2, NULL, NULL, 101),
('patient:delete', '删除患者', 'button', 2, NULL, NULL, 102),
('consultation:start', '开始问诊', 'button', 3, NULL, NULL, 100),
('consultation:end', '结束问诊', 'button', 3, NULL, NULL, 101),
('prescription:submit', '提交处方', 'button', 4, NULL, NULL, 100),
('pharmacy:approve', '审核通过', 'button', 5, NULL, NULL, 100),
('pharmacy:reject', '审核驳回', 'button', 5, NULL, NULL, 101),
('referral:accept', '接受转诊', 'button', 6, NULL, NULL, 100),
('referral:reject', '拒绝转诊', 'button', 6, NULL, NULL, 101),
('mdt:invite', '邀请参会', 'button', 7, NULL, NULL, 100),
('admin:export', '导出数据', 'button', 10, NULL, NULL, 100);

-- =============================================
-- 20. 角色权限关联数据
-- =============================================

-- 患者角色权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'PATIENT' AND p.perm_code IN (
    'dashboard', 'patient', 'patient:profile', 'patient:appointment',
    'consultation', 'consultation:room', 'consultation:history',
    'prescription', 'prescription:list', 'prescription:detail',
    'followup', 'followup:record',
    'ai', 'ai:chat'
);

-- 基层医生角色权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'DOCTOR_PRIMARY' AND p.perm_code IN (
    'dashboard', 'patient', 'patient:list', 'patient:profile',
    'consultation', 'consultation:waiting', 'consultation:room', 'consultation:history', 'consultation:record',
    'consultation:start', 'consultation:end',
    'prescription', 'prescription:create', 'prescription:list', 'prescription:detail', 'prescription:submit',
    'referral', 'referral:create', 'referral:history',
    'followup', 'followup:plan', 'followup:record',
    'ai', 'ai:chat', 'ai:summary'
);

-- 专家医生角色权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'DOCTOR_EXPERT' AND p.perm_code IN (
    'dashboard', 'patient', 'patient:list', 'patient:profile',
    'consultation', 'consultation:waiting', 'consultation:room', 'consultation:history', 'consultation:record',
    'consultation:start', 'consultation:end',
    'prescription', 'prescription:create', 'prescription:list', 'prescription:detail', 'prescription:submit',
    'referral', 'referral:received', 'referral:history', 'referral:accept', 'referral:reject',
    'mdt', 'mdt:create', 'mdt:list', 'mdt:room', 'mdt:invite',
    'followup', 'followup:plan', 'followup:record',
    'ai', 'ai:chat', 'ai:summary'
);

-- 药师角色权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'PHARMACIST' AND p.perm_code IN (
    'dashboard',
    'pharmacy', 'pharmacy:pending', 'pharmacy:history', 'pharmacy:approve', 'pharmacy:reject',
    'prescription', 'prescription:list', 'prescription:detail'
);

-- 管理员角色权限（全部权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p 
WHERE r.role_code = 'ADMIN';

-- =============================================
-- 21. 病历附件数据
-- =============================================

INSERT INTO medical_attachment (record_id, consultation_id, patient_id, file_name, file_type, file_size, file_url, category, description, uploader_id, created_at) VALUES
-- 病历1的附件（分泌性中耳炎）
(1, 1, (SELECT id FROM sys_user WHERE username='patient1'), '左耳听力检查报告.pdf', 'application/pdf', 256000, '/uploads/medical/2026/01/hearing_test_001.pdf', 'REPORT', '纯音测听检查报告', (SELECT id FROM sys_user WHERE username='doctor1'), DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(1, 1, (SELECT id FROM sys_user WHERE username='patient1'), '左耳鼓膜照片.jpg', 'image/jpeg', 512000, '/uploads/medical/2026/01/eardrum_001.jpg', 'IMAGE', '内窥镜检查图像', (SELECT id FROM sys_user WHERE username='doctor1'), DATE_SUB(NOW(), INTERVAL 3 HOUR)),

-- 病历2的附件（过敏性鼻炎）
(2, 3, (SELECT id FROM sys_user WHERE username='patient1'), '过敏原检测报告.pdf', 'application/pdf', 384000, '/uploads/medical/2025/12/allergy_test_001.pdf', 'REPORT', '血清过敏原IgE检测', (SELECT id FROM sys_user WHERE username='doctor2'), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 3, (SELECT id FROM sys_user WHERE username='patient1'), '鼻腔CT扫描.dcm', 'application/dicom', 2048000, '/uploads/medical/2025/12/nasal_ct_001.dcm', 'IMAGE', '鼻窦CT检查', (SELECT id FROM sys_user WHERE username='doctor2'), DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- 病历3的附件（中耳炎复查）
(3, 4, (SELECT id FROM sys_user WHERE username='patient2'), '复查听力报告.pdf', 'application/pdf', 192000, '/uploads/medical/2025/12/hearing_followup_001.pdf', 'REPORT', '治疗后听力复查', (SELECT id FROM sys_user WHERE username='doctor1'), DATE_SUB(NOW(), INTERVAL 14 DAY)),

-- 病历4的附件（扁桃体炎）
(4, 5, (SELECT id FROM sys_user WHERE username='patient3'), '咽部检查照片.jpg', 'image/jpeg', 384000, '/uploads/medical/2025/12/throat_001.jpg', 'IMAGE', '咽部内窥镜图像', (SELECT id FROM sys_user WHERE username='doctor3'), DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 5, (SELECT id FROM sys_user WHERE username='patient3'), '血常规报告.pdf', 'application/pdf', 128000, '/uploads/medical/2025/12/blood_test_001.pdf', 'REPORT', '血常规检查结果', (SELECT id FROM sys_user WHERE username='doctor3'), DATE_SUB(NOW(), INTERVAL 12 DAY)),

-- 病历5的附件（感音神经性聋）
(5, 7, (SELECT id FROM sys_user WHERE username='patient5'), '双耳纯音测听报告.pdf', 'application/pdf', 320000, '/uploads/medical/2025/12/hearing_bilateral_001.pdf', 'REPORT', '双耳听力详细检测', (SELECT id FROM sys_user WHERE username='expert1'), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 7, (SELECT id FROM sys_user WHERE username='patient5'), '颞骨CT报告.pdf', 'application/pdf', 512000, '/uploads/medical/2025/12/temporal_ct_001.pdf', 'REPORT', '颞骨高分辨CT', (SELECT id FROM sys_user WHERE username='expert1'), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 7, (SELECT id FROM sys_user WHERE username='patient5'), 'ABR检查报告.pdf', 'application/pdf', 256000, '/uploads/medical/2025/12/abr_001.pdf', 'REPORT', '听性脑干反应检查', (SELECT id FROM sys_user WHERE username='expert1'), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 患者自行上传的附件
(NULL, 2, (SELECT id FROM sys_user WHERE username='patient2'), '既往鼻炎病历.jpg', 'image/jpeg', 768000, '/uploads/patient/2026/01/history_001.jpg', 'OTHER', '之前医院的诊断记录', (SELECT id FROM sys_user WHERE username='patient2'), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- =============================================
-- 测试数据插入完成
-- =============================================

SELECT '测试数据插入完成！' AS message;
SELECT '用户账号密码统一为: 123456 (或 admin123 for admin)' AS note;
SELECT CONCAT('共插入 ', (SELECT COUNT(*) FROM sys_user), ' 个用户') AS user_count;
SELECT CONCAT('共插入 ', (SELECT COUNT(*) FROM consultation), ' 条问诊记录') AS consultation_count;
SELECT CONCAT('共插入 ', (SELECT COUNT(*) FROM prescription), ' 条处方记录') AS prescription_count;
