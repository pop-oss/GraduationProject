package com.erkang.controller;

import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.common.Result;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.PermissionMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.mapper.RoleMapper;
import com.erkang.mapper.UserMapper;
import com.erkang.service.AuditService;
import com.erkang.service.AuthService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeTry;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 管理员控制器测试 - 角色权限管理
 * _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3_
 */
class AdminControllerTest {

    private UserMapper userMapper;
    private ConsultationMapper consultationMapper;
    private PrescriptionMapper prescriptionMapper;
    private AuthService authService;
    private RoleMapper roleMapper;
    private PermissionMapper permissionMapper;
    private AuditService auditService;
    private AdminController adminController;

    @BeforeTry
    void setUp() {
        userMapper = mock(UserMapper.class);
        consultationMapper = mock(ConsultationMapper.class);
        prescriptionMapper = mock(PrescriptionMapper.class);
        authService = mock(AuthService.class);
        roleMapper = mock(RoleMapper.class);
        permissionMapper = mock(PermissionMapper.class);
        auditService = mock(AuditService.class);
        
        adminController = new AdminController(
            userMapper, consultationMapper, prescriptionMapper,
            authService, roleMapper, permissionMapper, auditService
        );
    }


    /**
     * 测试权限更新接口正常流程
     * **Validates: Requirements 4.1, 4.2**
     */
    @Property(tries = 50)
    void updateRolePermissions_withValidPermissions_shouldSucceed(
            @ForAll @LongRange(min = 1, max = 100) Long roleId,
            @ForAll @Size(min = 1, max = 10) List<@IntRange(min = 1, max = 100) Integer> permissionIds) {
        
        // Given: 角色存在且权限ID有效
        Map<String, Object> role = new HashMap<>();
        role.put("id", roleId);
        role.put("roleCode", "TEST_ROLE");
        role.put("roleName", "测试角色");
        
        when(roleMapper.selectRoleById(roleId)).thenReturn(role);
        when(roleMapper.selectPermissionIdsByRoleId(roleId)).thenReturn(List.of(1L, 2L));
        
        List<Long> permIdLongs = permissionIds.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        when(permissionMapper.selectPermissionIdsByIds(anyList())).thenReturn(permIdLongs);
        
        doNothing().when(roleMapper).deleteRolePermissions(roleId);
        doNothing().when(roleMapper).insertRolePermission(anyLong(), anyLong());
        doNothing().when(auditService).log(anyString(), anyString(), anyString(), anyLong(), anyString());
        
        Map<String, Object> request = new HashMap<>();
        request.put("permissionIds", permissionIds);
        
        // When
        Result<Void> result = adminController.updateRolePermissions(roleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(0);
        verify(roleMapper).deleteRolePermissions(roleId);
        verify(roleMapper, times(permissionIds.size())).insertRolePermission(eq(roleId), anyLong());
        verify(auditService).log(eq("UPDATE_ROLE_PERMISSION"), eq("权限管理"), eq("ROLE"), eq(roleId), anyString());
    }

    /**
     * 测试无效权限ID被拒绝
     * **Validates: Requirements 5.3**
     */
    @Property(tries = 50)
    void updateRolePermissions_withInvalidPermissionIds_shouldReject(
            @ForAll @LongRange(min = 1, max = 100) Long roleId,
            @ForAll @Size(min = 1, max = 5) List<@IntRange(min = 1, max = 100) Integer> validIds,
            @ForAll @Size(min = 1, max = 3) List<@IntRange(min = 101, max = 200) Integer> invalidIds) {
        
        // Given: 角色存在但部分权限ID无效
        Map<String, Object> role = new HashMap<>();
        role.put("id", roleId);
        role.put("roleCode", "TEST_ROLE");
        role.put("roleName", "测试角色");
        
        when(roleMapper.selectRoleById(roleId)).thenReturn(role);
        when(roleMapper.selectPermissionIdsByRoleId(roleId)).thenReturn(List.of());
        
        // 只返回有效的权限ID
        List<Long> validLongs = validIds.stream()
                .map(Integer::longValue)
                .collect(Collectors.toList());
        when(permissionMapper.selectPermissionIdsByIds(anyList())).thenReturn(validLongs);
        
        // 合并有效和无效ID
        List<Integer> allIds = new ArrayList<>(validIds);
        allIds.addAll(invalidIds);
        
        Map<String, Object> request = new HashMap<>();
        request.put("permissionIds", allIds);
        
        // When & Then
        assertThatThrownBy(() -> adminController.updateRolePermissions(roleId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无效的权限ID");
        
        // 验证没有执行删除和插入操作
        verify(roleMapper, never()).deleteRolePermissions(anyLong());
        verify(roleMapper, never()).insertRolePermission(anyLong(), anyLong());
    }

    /**
     * 测试角色不存在时返回错误
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 20)
    void updateRolePermissions_withNonExistentRole_shouldFail(
            @ForAll @LongRange(min = 1, max = 100) Long roleId) {
        
        // Given: 角色不存在
        when(roleMapper.selectRoleById(roleId)).thenReturn(null);
        
        Map<String, Object> request = new HashMap<>();
        request.put("permissionIds", List.of(1, 2, 3));
        
        // When & Then
        assertThatThrownBy(() -> adminController.updateRolePermissions(roleId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("角色不存在");
    }

    /**
     * 测试空权限列表更新
     * **Validates: Requirements 4.1, 4.2**
     */
    @Property(tries = 20)
    void updateRolePermissions_withEmptyPermissions_shouldClearAllPermissions(
            @ForAll @LongRange(min = 1, max = 100) Long roleId) {
        
        // Given: 角色存在，传入空权限列表
        Map<String, Object> role = new HashMap<>();
        role.put("id", roleId);
        role.put("roleCode", "TEST_ROLE");
        role.put("roleName", "测试角色");
        
        when(roleMapper.selectRoleById(roleId)).thenReturn(role);
        when(roleMapper.selectPermissionIdsByRoleId(roleId)).thenReturn(List.of(1L, 2L, 3L));
        doNothing().when(roleMapper).deleteRolePermissions(roleId);
        doNothing().when(auditService).log(anyString(), anyString(), anyString(), anyLong(), anyString());
        
        Map<String, Object> request = new HashMap<>();
        request.put("permissionIds", List.of());
        
        // When
        Result<Void> result = adminController.updateRolePermissions(roleId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(0);
        verify(roleMapper).deleteRolePermissions(roleId);
        verify(roleMapper, never()).insertRolePermission(anyLong(), anyLong());
    }

    /**
     * 测试获取角色列表
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 20)
    void getRoles_shouldReturnAllRoles() {
        // Given
        List<Map<String, Object>> mockRoles = List.of(
            Map.of("id", 1L, "roleCode", "ADMIN", "roleName", "管理员"),
            Map.of("id", 2L, "roleCode", "DOCTOR_PRIMARY", "roleName", "初诊医生")
        );
        when(roleMapper.selectAllRoles()).thenReturn(mockRoles);
        
        // When
        Result<List<Map<String, Object>>> result = adminController.getRoles();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).hasSize(2);
    }

    /**
     * 测试获取角色详情含权限ID
     * **Validates: Requirements 4.1**
     */
    @Property(tries = 20)
    void getRoleDetail_shouldReturnRoleWithPermissionIds(
            @ForAll @LongRange(min = 1, max = 100) Long roleId) {
        
        // Given
        Map<String, Object> role = new HashMap<>();
        role.put("id", roleId);
        role.put("roleCode", "TEST_ROLE");
        role.put("roleName", "测试角色");
        
        List<Long> permissionIds = List.of(1L, 2L, 3L);
        
        when(roleMapper.selectRoleById(roleId)).thenReturn(role);
        when(roleMapper.selectPermissionIdsByRoleId(roleId)).thenReturn(permissionIds);
        
        // When
        Result<Map<String, Object>> result = adminController.getRoleDetail(roleId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).containsKey("permissionIds");
        assertThat((List<?>) result.getData().get("permissionIds")).hasSize(3);
    }

    /**
     * 测试获取权限树
     * **Validates: Requirements 5.1**
     */
    @Property(tries = 10)
    void getPermissionTree_shouldReturnHierarchicalStructure() {
        // Given
        List<Map<String, Object>> mockPermissions = new ArrayList<>();
        mockPermissions.add(createPermission(1L, "system", "系统管理", 0L));
        mockPermissions.add(createPermission(2L, "system:user", "用户管理", 1L));
        mockPermissions.add(createPermission(3L, "system:role", "角色管理", 1L));
        
        when(permissionMapper.selectAllPermissions()).thenReturn(mockPermissions);
        
        // When
        Result<List<Map<String, Object>>> result = adminController.getPermissionTree();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(0);
        assertThat(result.getData()).isNotEmpty();
    }

    private Map<String, Object> createPermission(Long id, String code, String name, Long parentId) {
        Map<String, Object> perm = new HashMap<>();
        perm.put("id", id);
        perm.put("permCode", code);
        perm.put("permName", name);
        perm.put("parentId", parentId);
        return perm;
    }
}
