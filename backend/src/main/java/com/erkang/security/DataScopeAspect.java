package com.erkang.security;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.domain.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 数据范围控制切面
 * 在Service方法执行前检查数据访问权限
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {
    
    @Before("@annotation(dataScope)")
    public void checkDataScope(JoinPoint joinPoint, DataScope dataScope) {
        LoginUser user = UserContext.getUser();
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        DataScopeType scopeType = dataScope.value();
        
        switch (scopeType) {
            case PATIENT_SELF -> checkPatientScope(user);
            case DOCTOR_CONSULT -> checkDoctorScope(user);
            case PHARMACIST_REVIEW -> checkPharmacistScope(user);
            case ADMIN_ALL -> checkAdminScope(user);
        }
    }
    
    /**
     * 检查患者数据范围
     * 患者只能访问自己的数据
     */
    private void checkPatientScope(LoginUser user) {
        if (!user.hasRole(RoleEnum.PATIENT.getCode())) {
            // 非患者角色，检查是否有其他合法角色
            if (!user.hasAnyRole(
                    RoleEnum.DOCTOR_PRIMARY.getCode(),
                    RoleEnum.DOCTOR_EXPERT.getCode(),
                    RoleEnum.PHARMACIST.getCode(),
                    RoleEnum.ADMIN.getCode())) {
                throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
            }
        }
        // 患者角色的具体数据过滤在Service层通过userId实现
    }
    
    /**
     * 检查医生数据范围
     * 医生只能访问其接诊范围内的数据
     */
    private void checkDoctorScope(LoginUser user) {
        if (!user.hasAnyRole(
                RoleEnum.DOCTOR_PRIMARY.getCode(),
                RoleEnum.DOCTOR_EXPERT.getCode(),
                RoleEnum.ADMIN.getCode())) {
            throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }
    
    /**
     * 检查药师数据范围
     * 药师只能访问待审核处方
     */
    private void checkPharmacistScope(LoginUser user) {
        if (!user.hasAnyRole(
                RoleEnum.PHARMACIST.getCode(),
                RoleEnum.ADMIN.getCode())) {
            throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }
    
    /**
     * 检查管理员数据范围
     */
    private void checkAdminScope(LoginUser user) {
        if (!user.hasRole(RoleEnum.ADMIN.getCode())) {
            throw new BusinessException(ErrorCode.AUTH_DATA_SCOPE_DENIED);
        }
    }
}
