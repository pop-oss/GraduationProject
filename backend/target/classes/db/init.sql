-- =============================================
-- 耳康云诊 - 数据库初始化脚本
-- 执行顺序: 按文件名V1-V7依次执行
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS erkang_cloud 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE erkang_cloud;

-- 依次执行各版本SQL文件:
-- V1__create_user_permission_tables.sql  - 用户权限表
-- V2__create_profile_tables.sql          - 患者医生档案表
-- V3__create_consultation_tables.sql     - 问诊病历表
-- V4__create_prescription_tables.sql     - 处方审方表
-- V5__create_referral_mdt_tables.sql     - 转诊会诊表
-- V6__create_followup_tables.sql         - 随访表
-- V7__create_ai_audit_tables.sql         - AI审计表
