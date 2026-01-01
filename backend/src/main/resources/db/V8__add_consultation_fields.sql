-- =============================================
-- 添加问诊表字段
-- =============================================

-- 添加症状描述字段
ALTER TABLE consultation ADD COLUMN symptoms TEXT COMMENT '症状描述' AFTER rtc_room_id;

-- 添加预约时间字段
ALTER TABLE consultation ADD COLUMN scheduled_at DATETIME COMMENT '预约时间' AFTER symptoms;
