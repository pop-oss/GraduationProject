/**
 * 统计分析服务层
 * _Requirements: 10.1, 10.2, 10.3_
 */
import { get } from './http'

export interface OverviewStats {
  totalConsultations: number
  totalPrescriptions: number
  totalPatients: number
  totalDoctors: number
  avgConsultationDuration: number
  prescriptionApprovalRate: number
}

export interface ConsultationTrend {
  date: string
  count: number
  completedCount: number
}

export interface DepartmentStats {
  departmentId: number
  departmentName: string
  consultationCount: number
  prescriptionCount: number
  avgRating: number
}

export interface DoctorRanking {
  doctorId: number
  doctorName: string
  departmentName: string
  consultationCount: number
  avgRating: number
}

export interface PrescriptionStats {
  totalCount: number
  approvedCount: number
  rejectedCount: number
  pendingCount: number
  approvalRate: number
  avgReviewTime: number
}

export interface TimeRangeQuery {
  startDate?: string
  endDate?: string
  [key: string]: unknown
}

export const statsService = {
  /** 获取整体概览统计 */
  async getOverview(query: TimeRangeQuery = {}): Promise<OverviewStats> {
    const res = await get<OverviewStats>('/stats/overview', query)
    return res.data || {
      totalConsultations: 0,
      totalPrescriptions: 0,
      totalPatients: 0,
      totalDoctors: 0,
      avgConsultationDuration: 0,
      prescriptionApprovalRate: 0,
    }
  },

  /** 获取问诊趋势 */
  async getConsultationTrend(query: TimeRangeQuery = {}): Promise<ConsultationTrend[]> {
    const res = await get<ConsultationTrend[]>('/stats/consultation-trend', query)
    return res.data || []
  },

  /** 获取科室统计 */
  async getDepartmentStats(query: TimeRangeQuery = {}): Promise<DepartmentStats[]> {
    const res = await get<DepartmentStats[]>('/stats/departments', query)
    return res.data || []
  },

  /** 获取医生排行 */
  async getDoctorRanking(query: TimeRangeQuery & { limit?: number } = {}): Promise<DoctorRanking[]> {
    const res = await get<DoctorRanking[]>('/stats/doctor-ranking', {
      limit: 10,
      ...query,
    })
    return res.data || []
  },

  /** 获取处方审核统计 */
  async getPrescriptionStats(query: TimeRangeQuery = {}): Promise<PrescriptionStats> {
    const res = await get<PrescriptionStats>('/stats/prescriptions', query)
    return res.data || {
      totalCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      pendingCount: 0,
      approvalRate: 0,
      avgReviewTime: 0,
    }
  },
}
