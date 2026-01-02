/**
 * 管理员服务层
 * _Requirements: 9.1-9.4_
 */
import { get, post, put, del } from './http'

export type UserRole = 'PATIENT' | 'DOCTOR_PRIMARY' | 'DOCTOR_EXPERT' | 'PHARMACIST' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'DISABLED' | 'PENDING'

export interface User {
  id: number
  username: string
  realName: string
  phone: string
  email?: string
  role: UserRole
  status: UserStatus
  departmentId?: number
  departmentName?: string
  title?: string
  createdAt: string
  lastLoginAt?: string
}

export interface UserDetail extends User {
  avatar?: string
  idCard?: string
  gender?: string
  birthDate?: string
}

export interface CreateUserRequest {
  username: string
  password: string
  realName: string
  phone: string
  email?: string
  role: UserRole
  departmentId?: number
  title?: string
}

export interface UpdateUserRequest {
  realName?: string
  phone?: string
  email?: string
  role?: UserRole
  status?: UserStatus
  departmentId?: number
  title?: string
}

interface UserListQuery {
  page?: number
  pageSize?: number
  keyword?: string
  role?: UserRole
  status?: UserStatus
  [key: string]: unknown
}

interface UserListResult {
  list: User[]
  total: number
}

export interface Department {
  id: number
  name: string
  description?: string
}

export interface SystemStats {
  totalUsers: number
  totalDoctors: number
  totalPatients: number
  totalPharmacists: number
  totalConsultations: number
  totalPrescriptions: number
  onlineDoctors: number
  todayConsultations: number
}

export interface Role {
  id: number
  roleCode: string
  roleName: string
  description?: string
  createdAt: string
  permissionIds?: number[]
}

export interface Permission {
  id: number
  permCode: string
  permName: string
  permType: 'menu' | 'button'
  parentId: number
  path?: string
  icon?: string
  sortOrder: number
  children?: Permission[]
}

/**
 * 带重试的请求包装器
 * _Requirements: 3.8_
 */
async function withRetry<T>(
  fn: () => Promise<T>,
  maxRetries = 2,
  delay = 1000
): Promise<T> {
  let lastError: Error | null = null
  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await fn()
    } catch (err) {
      lastError = err instanceof Error ? err : new Error(String(err))
      if (i < maxRetries) {
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)))
      }
    }
  }
  throw lastError
}

export const adminService = {
  /** 获取用户列表 */
  async getUserList(query: UserListQuery = {}): Promise<UserListResult> {
    // 构建查询参数，排除 pageSize（后端用 size）
    const params: Record<string, unknown> = {
      page: query.page || 1,
      size: query.pageSize || 10,
    }
    // 添加筛选条件
    if (query.keyword) params.keyword = query.keyword
    if (query.role) params.role = query.role
    if (query.status) params.status = query.status
    
    const res = await get<{ records: User[]; total: number }>('/admin/users', params)
    return {
      list: res.data?.records || [],
      total: res.data?.total || 0,
    }
  },

  /** 获取用户详情 */
  async getUserDetail(id: number | string): Promise<UserDetail> {
    const res = await get<UserDetail>(`/admin/users/${id}`)
    return res.data!
  },

  /** 创建用户 */
  async createUser(data: CreateUserRequest): Promise<User> {
    const res = await post<User>('/admin/users', data)
    return res.data!
  },

  /** 更新用户 */
  async updateUser(id: number | string, data: UpdateUserRequest): Promise<void> {
    await put(`/admin/users/${id}`, data)
  },

  /** 删除用户 */
  async deleteUser(id: number | string): Promise<void> {
    await del(`/admin/users/${id}`)
  },

  /** 启用/禁用用户 */
  async toggleUserStatus(id: number | string, status: UserStatus): Promise<void> {
    await put(`/admin/users/${id}/status`, { status })
  },

  /** 重置用户密码 */
  async resetPassword(id: number | string, newPassword: string): Promise<void> {
    await post(`/admin/users/${id}/reset-password`, { password: newPassword })
  },

  /** 获取科室列表 */
  async getDepartments(): Promise<Department[]> {
    const res = await get<Department[]>('/admin/departments')
    return res.data || []
  },

  /** 获取系统统计 */
  async getStats(): Promise<SystemStats> {
    const res = await get<SystemStats>('/admin/stats')
    return res.data || {
      totalUsers: 0,
      totalDoctors: 0,
      totalPatients: 0,
      totalPharmacists: 0,
      totalConsultations: 0,
      totalPrescriptions: 0,
      onlineDoctors: 0,
      todayConsultations: 0,
    }
  },

  /** 获取角色列表 - 带重试 _Requirements: 1.5, 3.8_ */
  async getRoles(): Promise<Role[]> {
    return withRetry(async () => {
      const res = await get<Role[]>('/admin/roles')
      return res.data || []
    })
  },

  /** 获取角色详情（含权限） - 带重试 _Requirements: 3.8_ */
  async getRoleDetail(id: number): Promise<Role> {
    return withRetry(async () => {
      const res = await get<Role>(`/admin/roles/${id}`)
      return res.data!
    })
  },

  /** 更新角色权限 _Requirements: 4.1, 4.2_ */
  async updateRolePermissions(roleId: number, permissionIds: number[]): Promise<void> {
    await put(`/admin/roles/${roleId}/permissions`, { permissionIds })
  },

  /** 获取权限树 - 带重试 _Requirements: 2.1, 3.8_ */
  async getPermissionTree(): Promise<Permission[]> {
    return withRetry(async () => {
      const res = await get<Permission[]>('/admin/permissions/tree')
      return res.data || []
    })
  },
}
