/**
 * 审方服务层
 * _Requirements: 8.1-8.5_
 */
import { get, put } from './http'

export type ReviewStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED'
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH'

export interface PrescriptionItem {
  id: number
  drugName: string
  specification: string
  dosage: string
  frequency: string
  duration: string
  quantity: number
  unit: string
}

export interface ReviewPrescription {
  id: number
  prescriptionNo: string
  consultationId: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  diagnosis: string
  status: ReviewStatus
  riskLevel: RiskLevel
  drugCount: number
  createdAt: string
  items?: PrescriptionItem[]
  riskWarnings?: string[]
}

export interface ReviewDetail extends ReviewPrescription {
  items: PrescriptionItem[]
  patientAge?: number
  patientGender?: string
  allergies?: string[]
  reviewedAt?: string
  reviewedBy?: string
  rejectReason?: string
}

interface ReviewListQuery {
  page?: number
  pageSize?: number
  status?: ReviewStatus
  [key: string]: unknown
}

interface ReviewListResult {
  list: ReviewPrescription[]
  total: number
}

interface ReviewHistoryQuery {
  page?: number
  pageSize?: number
  startDate?: string
  endDate?: string
  [key: string]: unknown
}

export const reviewService = {
  /** 获取待审处方列表 */
  async getPendingList(query: ReviewListQuery = {}): Promise<ReviewListResult> {
    const res = await get<{ records: ReviewPrescription[]; total: number }>('/pharmacy-reviews/pending', {
      page: query.page || 1,
      size: query.pageSize || 10,
      ...query,
    })
    return {
      list: res.data?.records || [],
      total: res.data?.total || 0,
    }
  },

  /** 获取处方详情 */
  async getDetail(id: number | string): Promise<ReviewDetail> {
    const res = await get<ReviewDetail>(`/pharmacy-reviews/${id}`)
    return res.data!
  },

  /** 通过审核 */
  async approve(id: number | string, comment?: string): Promise<void> {
    await put(`/pharmacy-reviews/${id}/approve`, { comment })
  },

  /** 驳回处方 */
  async reject(id: number | string, reason: string): Promise<void> {
    await put(`/pharmacy-reviews/${id}/reject`, { reason })
  },

  /** 获取审方历史 */
  async getHistory(query: ReviewHistoryQuery = {}): Promise<ReviewListResult> {
    const res = await get<{ records: ReviewPrescription[]; total: number }>('/pharmacy-reviews/history', {
      page: query.page || 1,
      size: query.pageSize || 10,
      ...query,
    })
    return {
      list: res.data?.records || [],
      total: res.data?.total || 0,
    }
  },

  /** 获取审方统计 */
  async getStats(): Promise<{ pending: number; approvedToday: number; rejectedToday: number }> {
    const res = await get<{ pending: number; approvedToday: number; rejectedToday: number }>('/pharmacy-reviews/stats')
    return res.data || { pending: 0, approvedToday: 0, rejectedToday: 0 }
  },
}
