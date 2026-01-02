package com.erkang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 权限Mapper
 */
@Mapper
public interface PermissionMapper {

    @Select("SELECT id, perm_code as permCode, perm_name as permName, perm_type as permType, " +
            "parent_id as parentId, path, icon, sort_order as sortOrder " +
            "FROM sys_permission ORDER BY sort_order, id")
    List<Map<String, Object>> selectAllPermissions();

    /**
     * 根据ID查询权限是否存在
     * _Requirements: 5.3_
     * @param id 权限ID
     * @return 权限信息，不存在返回null
     */
    @Select("SELECT id, perm_code as permCode, perm_name as permName FROM sys_permission WHERE id = #{id}")
    Map<String, Object> selectPermissionById(@Param("id") Long id);

    /**
     * 批量验证权限ID是否存在，返回存在的权限ID列表
     * _Requirements: 5.3_
     * @param ids 权限ID列表
     * @return 存在的权限ID列表
     */
    @Select("<script>" +
            "SELECT id FROM sys_permission WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> selectPermissionIdsByIds(@Param("ids") List<Long> ids);
}
