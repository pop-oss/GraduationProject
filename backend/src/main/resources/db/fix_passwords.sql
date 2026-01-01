-- =============================================
-- 修复密码哈希脚本
-- 执行此脚本更新所有用户密码
-- =============================================

USE erkang_cloud;

-- 首先确保 deleted_at 为 NULL（软删除字段）
UPDATE sys_user SET deleted_at = NULL;

-- 更新 admin 用户密码为 admin123
UPDATE sys_user 
SET password = '$2a$10$LSUFK9JtgiQFAQQy4VRGQ.YnZG8AsMP22yw9zdTd2IBw2JkhMVXJG'
WHERE username = 'admin';

-- 更新所有其他用户密码为 123456
UPDATE sys_user 
SET password = '$2a$10$E5IlZqjUpMyiI4QlG1FHG.sNSm2r0NxUQ5OdR09Voi6owib..8XSK'
WHERE username != 'admin';

-- 验证更新结果
SELECT username, password, deleted_at FROM sys_user;
