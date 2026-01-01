/**
 * 管理员服务层
 * _Requirements: 9.1-9.4_
 */
import { get, post, put, del } from './http'

export type UserRole = 'PATIENT' | 'DOCTOR' | 'PHARMACIST' | 'ADMIN'
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
  async resetPassword(id: number | string): Promise<{ tempPassword: string }> {
    const res = await post<{ tempPassword: string }>(`/admin/users/${id}/reset-password`)
    return res.data!
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
}
