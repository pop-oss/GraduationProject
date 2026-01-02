package com.erkang.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper {

    @Select("SELECT id, role_code as roleCode, role_name as roleName, description, created_at as createdAt FROM sys_role ORDER BY id")
    List<Map<String, Object>> selectAllRoles();

    @Select("SELECT id, role_code as roleCode, role_name as roleName, description, created_at as createdAt FROM sys_role WHERE id = #{id}")
    Map<String, Object> selectRoleById(@Param("id") Long id);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
