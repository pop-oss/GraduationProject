package com.erkang.common;

import lombok.Getter;

/**
 * 错误码枚举
 * 
 * 错误码区间划分：
 * 1000-1999: 认证与权限
 * 2000-2999: 患者模块
 * 3000-3999: 问诊模块
 * 4000-4999: 处方模块
 * 5000-5999: 转诊/会诊模块
 * 6000-6999: 随访模块
 * 9000-9999: 系统错误
 */
@Getter
public enum ErrorCode {
    
    // 通用错误
    SUCCESS(0, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    
    // 认证与权限 1000-1999
    AUTH_INVALID_CREDENTIALS(1001, "用户名或密码错误"),
    AUTH_TOKEN_EXPIRED(1002, "Token已过期"),
    AUTH_TOKEN_INVALID(1003, "Token无效"),
    AUTH_USER_DISABLED(1004, "用户已被禁用"),
    AUTH_USER_NOT_FOUND(1005, "用户不存在"),
    AUTH_ROLE_NOT_ALLOWED(1006, "角色权限不足"),
    AUTH_DATA_SCOPE_DENIED(1007, "无权访问该数据"),
    
    // 患者模块 2000-2999
    PATIENT_NOT_FOUND(2001, "患者档案不存在"),
    PATIENT_PROFILE_INCOMPLETE(2002, "患者档案不完整"),
    
    // 问诊模块 3000-3999
    CONSULT_NOT_FOUND(3001, "问诊记录不存在"),
    CONSULT_STATUS_INVALID(3002, "问诊状态不允许此操作"),
    CONSULT_NOT_BELONG(3003, "无权操作此问诊"),
    APPOINTMENT_NOT_FOUND(3004, "预约记录不存在"),
    APPOINTMENT_TIME_CONFLICT(3005, "预约时间冲突"),
    
    // 处方模块 4000-4999
    PRESCRIPTION_NOT_FOUND(4001, "处方不存在"),
    PRESCRIPTION_STATUS_INVALID(4002, "处方状态不允许此操作"),
    PRESCRIPTION_ALREADY_REVIEWED(4003, "处方已审核，无法修改"),
    REVIEW_NOT_FOUND(4004, "审方记录不存在"),
    
    // 转诊/会诊模块 5000-5999
    REFERRAL_NOT_FOUND(5001, "转诊记录不存在"),
    REFERRAL_INFO_INCOMPLETE(5002, "转诊信息不完整"),
    REFERRAL_CANNOT_DELETE(5003, "转诊记录不可删除"),
    MDT_NOT_FOUND(5004, "会诊记录不存在"),
    MDT_CANNOT_DELETE(5005, "会诊记录不可删除"),
    
    // 随访模块 6000-6999
    FOLLOWUP_PLAN_NOT_FOUND(6001, "随访计划不存在"),
    FOLLOWUP_RECORD_NOT_FOUND(6002, "随访记录不存在"),
    
    // 病历模块 7000-7999
    MEDICAL_RECORD_NOT_FOUND(7001, "病历不存在"),
    MEDICAL_RECORD_CANNOT_DELETE(7002, "病历不可删除"),
    
    // 文件模块 8000-8999
    FILE_UPLOAD_FAILED(8001, "文件上传失败"),
    FILE_NOT_FOUND(8002, "文件不存在"),
    FILE_ACCESS_DENIED(8003, "无权访问该文件"),
    
    // 系统错误 9000-9999
    SYSTEM_BUSY(9001, "系统繁忙，请稍后重试"),
    EXTERNAL_SERVICE_ERROR(9002, "外部服务调用失败");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
