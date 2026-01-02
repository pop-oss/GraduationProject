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
   * 获取处方详情（包含药品明细）
   * _Requirements: 5.2_
   */
  getDetail: async (id: Id): Promise<PrescriptionDetail> => {
    // 同时获取处方基本信息和药品明细
    const [prescriptionRes, itemsRes] = await Promise.all([
      get<any>(`/prescriptions/${id}`),
      get<any[]>(`/prescriptions/${id}/items`),
    ]);
    
    const prescription = prescriptionRes.data;
    const items = itemsRes.data || [];
    
    console.log('[PrescriptionService] getDetail - prescription:', prescription);
    console.log('[PrescriptionService] getDetail - items:', items);
    
    // 处理 diagnosis 字段，确保是数组格式
    let diagnosis: string[] = [];
    if (prescription.diagnosis) {
      if (Array.isArray(prescription.diagnosis)) {
        diagnosis = prescription.diagnosis;
      } else if (typeof prescription.diagnosis === 'string') {
        diagnosis = [prescription.diagnosis];
      }
    }
    
    const result = {
      ...prescription,
      diagnosis,
      items,
    };
    
    console.log('[PrescriptionService] getDetail - result:', result);
    
    return result;
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
