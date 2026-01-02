package com.erkang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erkang.common.BusinessException;
import com.erkang.common.ErrorCode;
import com.erkang.common.Result;
import com.erkang.domain.entity.User;
import com.erkang.mapper.ConsultationMapper;
import com.erkang.mapper.PermissionMapper;
import com.erkang.mapper.PrescriptionMapper;
import com.erkang.mapper.RoleMapper;
import com.erkang.mapper.UserMapper;
import com.erkang.security.RequireRole;
import com.erkang.service.AuditService;
import com.erkang.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员控制器
 * _Requirements: 9.1-9.4_
 */
@Slf4j
@Tag(name = "管理员管理", description = "用户管理、系统统计")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ConsultationMapper consultationMapper;
    private final PrescriptionMapper prescriptionMapper;
    private final AuthService authService;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取用户列表")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        // 状态筛选：前端传 ACTIVE/DISABLED，转换为 1/0
        if (status != null && !status.isEmpty()) {
            int statusValue = "ACTIVE".equals(status) ? 1 : 0;
            wrapper.eq(User::getStatus, statusValue);
        }
        // 角色筛选：需要通过子查询过滤
        if (role != null && !role.isEmpty()) {
            // 获取该角色的用户ID列表
            List<Long> userIds = userMapper.selectUserIdsByRole(role);
            if (userIds.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("records", List.of());
                emptyData.put("total", 0);
                emptyData.put("pages", 0);
                emptyData.put("current", page);
                return Result.success(emptyData);
            }
            wrapper.in(User::getId, userIds);
        }
        wrapper.isNull(User::getDeletedAt);
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        // 获取用户角色和科室信息
        List<Map<String, Object>> records = result.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            // 转换状态为字符串格式
            map.put("status", user.getStatus() == 1 ? "ACTIVE" : "DISABLED");
            map.put("createdAt", user.getCreatedAt());
            map.put("lastLoginAt", user.getLastLoginAt());
            
            List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
            String roleCode = roles.isEmpty() ? "PATIENT" : roles.get(0);
            // 转换角色编码为前端期望的格式
            String displayRole = roleCode;
            if ("DOCTOR_PRIMARY".equals(roleCode) || "DOCTOR_EXPERT".equals(roleCode)) {
                displayRole = "DOCTOR";
            }
            map.put("role", displayRole);
            map.put("roles", roles);
            
            // 获取医生的科室信息
            if (roleCode.startsWith("DOCTOR")) {
                try {
                    Map<String, Object> doctorInfo = userMapper.selectDoctorDepartment(user.getId());
                    if (doctorInfo != null) {
                        map.put("departmentId", doctorInfo.get("department_id"));
                        map.put("departmentName", doctorInfo.get("department_name"));
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            
            return map;
        }).toList();
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", result.getTotal());
        data.put("pages", result.getPages());
        data.put("current", result.getCurrent());
        
        return Result.success(data);
    }


    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取用户详情")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("status", user.getStatus());
        data.put("createdAt", user.getCreatedAt());
        data.put("lastLoginAt", user.getLastLoginAt());
        data.put("lastLoginIp", user.getLastLoginIp());
        
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        data.put("role", roles.isEmpty() ? "PATIENT" : roles.get(0));
        data.put("roles", roles);
        
        return Result.success(data);
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    @RequireRole({"ADMIN"})
    @Operation(summary = "创建用户")
    public Result<Map<String, Object>> createUser(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String realName = (String) request.get("realName");
        String phone = (String) request.get("phone");
        String email = (String) request.get("email");
        String role = (String) request.get("role");
        
        // 检查用户名是否已存在
        User existing = userMapper.selectByUsername(username);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(authService.encodePassword(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 分配角色
        if (role != null && !role.isEmpty()) {
            Long roleId = userMapper.selectRoleIdByCode(role);
            if (roleId != null) {
                userMapper.insertUserRole(user.getId(), roleId);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        
        log.info("创建用户成功: userId={}, username={}", user.getId(), user.getUsername());
        return Result.success(data);
    }

    /**
     * 更新用户
     */
    @PutMapping("/users/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "更新用户")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        if (request.containsKey("realName")) {
            user.setRealName((String) request.get("realName"));
        }
        if (request.containsKey("phone")) {
            user.setPhone((String) request.get("phone"));
        }
        if (request.containsKey("email")) {
            user.setEmail((String) request.get("email"));
        }
        if (request.containsKey("status")) {
            user.setStatus((Integer) request.get("status"));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("更新用户成功: userId={}", id);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        // 软删除
        user.setDeletedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("删除用户成功: userId={}", id);
        return Result.success();
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    @RequireRole({"ADMIN"})
    @Operation(summary = "更新用户状态")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        String status = (String) request.get("status");
        user.setStatus("ACTIVE".equals(status) ? 1 : 0);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("更新用户状态: userId={}, status={}", id, status);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/users/{id}/reset-password")
    @RequireRole({"ADMIN"})
    @Operation(summary = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.AUTH_USER_NOT_FOUND);
        }
        
        String newPassword = (String) request.get("password");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码至少6位");
        }
        
        user.setPassword(authService.encodePassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("重置用户密码: userId={}", id);
        return Result.success();
    }

    /**
     * 获取科室列表
     */
    @GetMapping("/departments")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取科室列表")
    public Result<List<Map<String, Object>>> getDepartments() {
        // 返回默认科室列表
        List<Map<String, Object>> departments = List.of(
            Map.of("id", 1, "name", "耳科", "description", "耳部疾病诊治"),
            Map.of("id", 2, "name", "鼻科", "description", "鼻部疾病诊治"),
            Map.of("id", 3, "name", "咽喉科", "description", "咽喉疾病诊治"),
            Map.of("id", 4, "name", "头颈外科", "description", "头颈部肿瘤诊治")
        );
        return Result.success(departments);
    }

    /**
     * 获取系统统计
     */
    @GetMapping("/stats")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取系统统计")
    public Result<Map<String, Object>> getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        try {
            long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().isNull(User::getDeletedAt));
            stats.put("totalUsers", totalUsers);
        } catch (Exception e) {
            log.warn("查询用户总数失败: {}", e.getMessage());
            stats.put("totalUsers", 0);
        }
        
        // 按角色统计
        try {
            long totalDoctors = userMapper.countUsersByRole("DOCTOR_PRIMARY") + userMapper.countUsersByRole("DOCTOR_EXPERT");
            long totalPatients = userMapper.countUsersByRole("PATIENT");
            long totalPharmacists = userMapper.countUsersByRole("PHARMACIST");
            stats.put("totalDoctors", totalDoctors);
            stats.put("totalPatients", totalPatients);
            stats.put("totalPharmacists", totalPharmacists);
        } catch (Exception e) {
            log.warn("查询角色统计失败: {}", e.getMessage());
            stats.put("totalDoctors", 0);
            stats.put("totalPatients", 0);
            stats.put("totalPharmacists", 0);
        }
        
        // 问诊统计
        try {
            long totalConsultations = consultationMapper.selectCount(null);
            long todayConsultations = consultationMapper.selectCount(
                new LambdaQueryWrapper<com.erkang.domain.entity.Consultation>()
                    .between(com.erkang.domain.entity.Consultation::getCreatedAt, todayStart, todayEnd)
            );
            stats.put("totalConsultations", totalConsultations);
            stats.put("todayConsultations", todayConsultations);
        } catch (Exception e) {
            log.warn("查询问诊统计失败: {}", e.getMessage());
            stats.put("totalConsultations", 0);
            stats.put("todayConsultations", 0);
        }
        
        // 处方统计
        try {
            long totalPrescriptions = prescriptionMapper.selectCount(null);
            stats.put("totalPrescriptions", totalPrescriptions);
        } catch (Exception e) {
            log.warn("查询处方统计失败: {}", e.getMessage());
            stats.put("totalPrescriptions", 0);
        }
        
        stats.put("onlineDoctors", 0); // 简化处理
        
        return Result.success(stats);
    }

    /**
     * 获取角色列表
     */
    @GetMapping("/roles")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取角色列表")
    public Result<List<Map<String, Object>>> getRoles() {
        List<Map<String, Object>> roles = roleMapper.selectAllRoles();
        return Result.success(roles);
    }

    /**
     * 获取角色详情（含权限ID列表）
     */
    @GetMapping("/roles/{id}")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取角色详情")
    public Result<Map<String, Object>> getRoleDetail(@PathVariable Long id) {
        Map<String, Object> role = roleMapper.selectRoleById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }
        // 获取角色的权限ID列表
        List<Long> permissionIds = roleMapper.selectPermissionIdsByRoleId(id);
        role.put("permissionIds", permissionIds);
        return Result.success(role);
    }

    /**
     * 更新角色权限
     * _Requirements: 4.1, 4.2, 4.3, 4.4, 5.3_
     */
    @PutMapping("/roles/{id}/permissions")
    @RequireRole({"ADMIN"})
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "更新角色权限")
    public Result<Void> updateRolePermissions(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        // 验证角色是否存在
        Map<String, Object> role = roleMapper.selectRoleById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "角色不存在");
        }
        
        @SuppressWarnings("unchecked")
        List<Integer> permissionIds = (List<Integer>) request.get("permissionIds");
        
        // 获取原有权限用于审计日志
        List<Long> oldPermissionIds = roleMapper.selectPermissionIdsByRoleId(id);
        
        // 验证权限ID有效性 _Requirements: 5.3_
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<Long> permIdLongs = permissionIds.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());
            
            List<Long> validPermissionIds = permissionMapper.selectPermissionIdsByIds(permIdLongs);
            Set<Long> validSet = new HashSet<>(validPermissionIds);
            
            // 检查是否有无效的权限ID
            List<Long> invalidIds = permIdLongs.stream()
                    .filter(pid -> !validSet.contains(pid))
                    .collect(Collectors.toList());
            
            if (!invalidIds.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, 
                        "无效的权限ID: " + invalidIds);
            }
        }
        
        // 删除原有权限 _Requirements: 4.1_
        roleMapper.deleteRolePermissions(id);
        
        // 添加新权限 _Requirements: 4.2_
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Integer permId : permissionIds) {
                roleMapper.insertRolePermission(id, permId.longValue());
            }
        }
        
        // 记录审计日志 _Requirements: 4.4_
        String roleCode = (String) role.get("roleCode");
        String detail = String.format("角色[%s]权限更新: 原权限数=%d, 新权限数=%d", 
                roleCode, 
                oldPermissionIds.size(), 
                permissionIds != null ? permissionIds.size() : 0);
        auditService.log("UPDATE_ROLE_PERMISSION", "权限管理", "ROLE", id, detail);
        
        log.info("更新角色权限: roleId={}, roleCode={}, permissionCount={}", 
                id, roleCode, permissionIds != null ? permissionIds.size() : 0);
        return Result.success();
    }

    /**
     * 获取权限树
     */
    @GetMapping("/permissions/tree")
    @RequireRole({"ADMIN"})
    @Operation(summary = "获取权限树")
    public Result<List<Map<String, Object>>> getPermissionTree() {
        List<Map<String, Object>> allPermissions = permissionMapper.selectAllPermissions();
        // 构建树形结构
        List<Map<String, Object>> tree = buildPermissionTree(allPermissions, 0L);
        return Result.success(tree);
    }

    /**
     * 构建权限树
     */
    private List<Map<String, Object>> buildPermissionTree(List<Map<String, Object>> allPermissions, Long parentId) {
        return allPermissions.stream()
            .filter(p -> {
                Object pid = p.get("parentId");
                if (pid == null) return parentId == 0L;
                return pid.equals(parentId) || pid.equals(parentId.intValue());
            })
            .map(p -> {
                Map<String, Object> node = new HashMap<>(p);
                Long id = ((Number) p.get("id")).longValue();
                List<Map<String, Object>> children = buildPermissionTree(allPermissions, id);
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                return node;
            })
            .toList();
    }
}
