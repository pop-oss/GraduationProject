package com.erkang.security;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.enums.RoleEnum;

/**
 * 数据范围辅助工具类
 * 用于Service层进行数据范围过滤
 */
public class DataScopeHelper {
    
    /**
     * 获取当前患者ID（患者角色使用）
     * 患者只能访问自己的数据
     */
    public static Long getCurrentPatientId() {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        if (user.hasRole(RoleEnum.PATIENT.getCode())) {
            return user.getUserId();
        }
        
        // 非患者角色返回null，表示不限制
        return null;
    }
    
    /**
     * 获取当前医生ID（医生角色使用）
     * 医生只能访问自己接诊的数据
     */
    public static Long getCurrentDoctorId() {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        if (user.hasAnyRole(RoleEnum.DOCTOR_PRIMARY.getCode(), RoleEnum.DOCTOR_EXPERT.getCode())) {
            return user.getUserId();
        }
        
        // 管理员不限制
        if (user.hasRole(RoleEnum.ADMIN.getCode())) {
            return null;
        }
        
        throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
    }
    
    /**
     * 检查是否有权访问指定患者数据
     */
    public static void checkPatientAccess(Long patientId) {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 管理员可访问所有
        if (user.hasRole(RoleEnum.ADMIN.getCode())) {
            return;
        }
        
        // 患者只能访问自己
        if (user.hasRole(RoleEnum.PATIENT.getCode())) {
            if (!user.getUserId().equals(patientId)) {
                throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
            }
        }
        
        // 医生和药师的访问权限在具体业务中校验
    }
    
    /**
     * 是否为管理员
     */
    public static boolean isAdmin() {
        LoginUser user = UserContext.getUser();
        return user != null && user.hasRole(RoleEnum.ADMIN.getCode());
    }
    
    /**
     * 是否为医生
     */
    public static boolean isDoctor() {
        LoginUser user = UserContext.getUser();
        return user != null && user.hasAnyRole(
                RoleEnum.DOCTOR_PRIMARY.getCode(), 
                RoleEnum.DOCTOR_EXPERT.getCode());
    }
    
    /**
     * 是否为患者
     */
    public static boolean isPatient() {
        LoginUser user = UserContext.getUser();
        return user != null && user.hasRole(RoleEnum.PATIENT.getCode());
    }
}
