/**
 * 审计日志服务层
 * _Requirements: 9.3, 9.4_
 */
import { get } from './http'

export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'VIEW' | 'LOGIN' | 'LOGOUT' | 'EXPORT'
export type AuditModule = 'AUTH' | 'USER' | 'CONSULTATION' | 'PRESCRIPTION' | 'REFERRAL' | 'MDT' | 'REVIEW'

export interface AuditLog {
  id: number
  userId: number
  username: string
  realName?: string
  phone?: string
  action: AuditAction
  module: AuditModule
  targetId?: string
  detail: string
  ip: string
  userAgent?: string
  createdAt: string
}

interface AuditLogQuery {
  page?: number
  pageSize?: number
  username?: string
  module?: AuditModule
  action?: AuditAction
  startDate?: string
  endDate?: string
  [key: string]: unknown
}

interface AuditLogResult {
  list: AuditLog[]
  total: number
}

export const auditService = {
  /** 获取审计日志列表 */
  async getList(query: AuditLogQuery = {}): Promise<AuditLogResult> {
    const res = await get<{ records: AuditLog[]; total: number }>('/audit-logs', {
      page: query.page || 1,
      size: query.pageSize || 20,
      ...query,
    })
    return {
      list: res.data?.records || [],
      total: res.data?.total || 0,
    }
  },

  /** 导出审计日志 */
  async exportLogs(query: Omit<AuditLogQuery, 'page' | 'pageSize'>): Promise<Blob> {
    const { httpClient } = await import('./http')
    const res = await httpClient.get('/audit-logs/export', {
      params: query,
      responseType: 'blob',
    })
    return res.data
  },
}
