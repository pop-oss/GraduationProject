package com.erkang.security;

import com.erkang.common.BusinessException;
import com.erkang.domain.enums.RoleEnum;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.List;

/**
 * 数据范围隔离属性测试
 * 
 * Feature: ent-telemedicine, Property 2: 数据范围隔离
 * Validates: Requirements 1.5, 1.6, 1.7
 */
class DataScopePropertyTest {
    
    /**
     * Property 1: 患者角色只能访问自己的数据
     * 对于任意患者用户和任意患者ID，如果患者ID不等于用户ID，则应该拒绝访问
     */
    @Property(tries = 100)
    @Label("患者只能访问自己的数据")
    void patientCanOnlyAccessOwnData(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000) Long targetPatientId
    ) {
        // Given: 患者用户
        LoginUser patientUser = LoginUser.builder()
                .userId(userId)
                .username("patient_" + userId)
                .roles(List.of(RoleEnum.PATIENT.getCode()))
                .build();
        
        UserContext.setUser(patientUser);
        
        try {
            // When: 尝试访问患者数据
            if (userId.equals(targetPatientId)) {
                // Then: 访问自己的数据应该成功
                DataScopeHelper.checkPatientAccess(targetPatientId);
                // 没有抛出异常，测试通过
            } else {
                // Then: 访问他人数据应该失败
                try {
                    DataScopeHelper.checkPatientAccess(targetPatientId);
                    assert false : "患者不应该能访问其他患者的数据";
                } catch (BusinessException e) {
                    // 预期的异常
                    assert e.getCode() == 1007 : "应该返回数据范围拒绝错误";
                }
            }
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * Property 2: 管理员可以访问所有数据
     * 对于任意管理员用户和任意患者ID，管理员都应该能访问
     */
    @Property(tries = 100)
    @Label("管理员可以访问所有数据")
    void adminCanAccessAllData(
            @ForAll @LongRange(min = 1, max = 1000) Long adminUserId,
            @ForAll @LongRange(min = 1, max = 1000) Long targetPatientId
    ) {
        // Given: 管理员用户
        LoginUser adminUser = LoginUser.builder()
                .userId(adminUserId)
                .username("admin_" + adminUserId)
                .roles(List.of(RoleEnum.ADMIN.getCode()))
                .build();
        
        UserContext.setUser(adminUser);
        
        try {
            // When & Then: 管理员访问任意患者数据都应该成功
            DataScopeHelper.checkPatientAccess(targetPatientId);
            // 没有抛出异常，测试通过
        } finally {
            UserContext.clear();
        }
    }

    /**
     * Property 3: 医生角色检查应该正确识别医生
     * 对于任意医生用户，getCurrentDoctorId应该返回其用户ID
     */
    @Property(tries = 100)
    @Label("医生角色返回正确的医生ID")
    void doctorRoleReturnsCorrectDoctorId(
            @ForAll @LongRange(min = 1, max = 1000) Long doctorUserId,
            @ForAll("doctorRoles") String doctorRole
    ) {
        // Given: 医生用户
        LoginUser doctorUser = LoginUser.builder()
                .userId(doctorUserId)
                .username("doctor_" + doctorUserId)
                .roles(List.of(doctorRole))
                .build();
        
        UserContext.setUser(doctorUser);
        
        try {
            // When: 获取当前医生ID
            Long currentDoctorId = DataScopeHelper.getCurrentDoctorId();
            
            // Then: 应该返回医生的用户ID
            assert doctorUserId.equals(currentDoctorId) : 
                String.format("医生ID不一致: 期望 %d, 实际 %d", doctorUserId, currentDoctorId);
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * Property 4: 非医生角色不能获取医生数据范围
     * 对于患者或药师角色，getCurrentDoctorId应该抛出异常
     */
    @Property(tries = 100)
    @Label("非医生角色不能获取医生数据范围")
    void nonDoctorCannotGetDoctorScope(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll("nonDoctorRoles") String role
    ) {
        // Given: 非医生用户
        LoginUser user = LoginUser.builder()
                .userId(userId)
                .username("user_" + userId)
                .roles(List.of(role))
                .build();
        
        UserContext.setUser(user);
        
        try {
            // When & Then: 获取医生数据范围应该失败
            try {
                DataScopeHelper.getCurrentDoctorId();
                assert false : "非医生角色不应该能获取医生数据范围";
            } catch (BusinessException e) {
                // 预期的异常
                assert e.getCode() == 1007 : "应该返回数据范围拒绝错误";
            }
        } finally {
            UserContext.clear();
        }
    }
    
    /**
     * Property 5: 角色判断方法应该正确
     */
    @Property(tries = 100)
    @Label("角色判断方法正确性")
    void roleCheckMethodsAreCorrect(
            @ForAll @LongRange(min = 1, max = 1000) Long userId,
            @ForAll("allRoles") String role
    ) {
        // Given: 用户
        LoginUser user = LoginUser.builder()
                .userId(userId)
                .username("user_" + userId)
                .roles(List.of(role))
                .build();
        
        UserContext.setUser(user);
        
        try {
            // Then: 角色判断应该正确
            boolean isAdmin = DataScopeHelper.isAdmin();
            boolean isDoctor = DataScopeHelper.isDoctor();
            boolean isPatient = DataScopeHelper.isPatient();
            
            assert isAdmin == role.equals(RoleEnum.ADMIN.getCode());
            assert isDoctor == (role.equals(RoleEnum.DOCTOR_PRIMARY.getCode()) || 
                               role.equals(RoleEnum.DOCTOR_EXPERT.getCode()));
            assert isPatient == role.equals(RoleEnum.PATIENT.getCode());
        } finally {
            UserContext.clear();
        }
    }
    
    @Provide
    Arbitrary<String> doctorRoles() {
        return Arbitraries.of(
            RoleEnum.DOCTOR_PRIMARY.getCode(),
            RoleEnum.DOCTOR_EXPERT.getCode()
        );
    }
    
    @Provide
    Arbitrary<String> nonDoctorRoles() {
        return Arbitraries.of(
            RoleEnum.PATIENT.getCode(),
            RoleEnum.PHARMACIST.getCode()
        );
    }
    
    @Provide
    Arbitrary<String> allRoles() {
        return Arbitraries.of(
            RoleEnum.PATIENT.getCode(),
            RoleEnum.DOCTOR_PRIMARY.getCode(),
            RoleEnum.DOCTOR_EXPERT.getCode(),
            RoleEnum.PHARMACIST.getCode(),
            RoleEnum.ADMIN.getCode()
        );
    }
}
