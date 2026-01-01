package com.erkang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erkang.domain.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted_at IS NULL")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 查询用户角色编码列表
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色编码查询角色ID
     */
    @Select("SELECT id FROM sys_role WHERE role_code = #{roleCode}")
    Long selectRoleIdByCode(@Param("roleCode") String roleCode);
    
    /**
     * 插入用户角色关联
     */
    @Insert("INSERT INTO sys_user_role (user_id, role_id, created_at) VALUES (#{userId}, #{roleId}, NOW())")
    void insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 统计指定角色的用户数
     */
    @Select("SELECT COUNT(DISTINCT ur.user_id) FROM sys_user_role ur " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "INNER JOIN sys_user u ON ur.user_id = u.id " +
            "WHERE r.role_code = #{roleCode} AND u.deleted_at IS NULL")
    Long countUsersByRole(@Param("roleCode") String roleCode);
    
    /**
     * 查询医生的科室信息
     */
    @Select("SELECT dp.department_id, d.name as department_name " +
            "FROM doctor_profile dp " +
            "LEFT JOIN org_department d ON dp.department_id = d.id " +
            "WHERE dp.user_id = #{userId}")
    java.util.Map<String, Object> selectDoctorDepartment(@Param("userId") Long userId);
    
    /**
     * 根据角色编码查询用户ID列表
     */
    @Select("SELECT DISTINCT ur.user_id FROM sys_user_role ur " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "INNER JOIN sys_user u ON ur.user_id = u.id " +
            "WHERE (r.role_code = #{roleCode} OR " +
            "(#{roleCode} = 'DOCTOR' AND r.role_code IN ('DOCTOR_PRIMARY', 'DOCTOR_EXPERT'))) " +
            "AND u.deleted_at IS NULL")
    List<Long> selectUserIdsByRole(@Param("roleCode") String roleCode);
}
