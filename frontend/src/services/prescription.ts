/**
 * 处方服务
 * _Requirements: 5.1, 5.2, 6.8_
 */

import { get, post } from './http';
import type {
  Prescription,
  PrescriptionDetail,
  PrescriptionQuery,
  CreatePrescriptionRequest,
  PageResult,
  Id,
} from '@/types';

/**
 * 处方服务
 * _Requirements: 5.1, 5.2, 6.8_
 */
export const prescriptionService = {
  /**
   * 创建处方（医生端）
   * _Requirements: 6.8_
   */
  create: async (data: CreatePrescriptionRequest): Promise<Prescription> => {
    const response = await post<Prescription>('/prescriptions', data);
    return response.data;
  },

  /**
   * 获取处方列表
   * _Requirements: 5.1_
   */
  getList: async (params: PrescriptionQuery): Promise<PageResult<Prescription>> => {
    const response = await get<any>('/prescriptions', params);
    // 适配后端返回格式 { records, total, pages, current }
    const data = response.data;
    return {
      list: data.records || [],
      total: data.total || 0,
      page: data.current || 1,
      pageSize: params.pageSize || 10,
    };
  },

  /**
   * 获取处方详情
   * _Requirements: 5.2_
   */
  getDetail: async (id: Id): Promise<PrescriptionDetail> => {
    const response = await get<PrescriptionDetail>(`/prescriptions/${id}`);
    return response.data;
  },

  /**
   * 获取问诊关联的处方
   */
  getByConsultation: async (consultationId: Id): Promise<Prescription[]> => {
    const response = await get<Prescription[]>(`/consultations/${consultationId}/prescriptions`);
    return response.data;
  },
};

export default prescriptionService;
