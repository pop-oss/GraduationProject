/**
 * 问诊服务
 * _Requirements: 4.1-4.8_
 */

import { get, post, put } from './http';
import type {
  Consultation,
  ConsultationDetail,
  ConsultationQuery,
  CreateConsultationRequest,
  PageResult,
  Id,
} from '@/types';

/**
 * 问诊服务
 * _Requirements: 4.1-4.8_
 */
export const consultationService = {
  /**
   * 创建问诊预约
   * _Requirements: 4.1, 4.2_
   */
  create: async (data: CreateConsultationRequest): Promise<Consultation> => {
    const response = await post<Consultation>('/consultations', data);
    return response.data;
  },

  /**
   * 获取问诊列表
   * _Requirements: 4.3_
   */
  getList: async (params: ConsultationQuery): Promise<PageResult<Consultation>> => {
    const response = await get<any>('/consultations', params);
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
   * 获取问诊详情
   * _Requirements: 4.4, 4.5_
   */
  getDetail: async (id: Id): Promise<ConsultationDetail> => {
    const response = await get<ConsultationDetail>(`/consultations/${id}`);
    return response.data;
  },

  /**
   * 取消问诊
   */
  cancel: async (id: Id): Promise<void> => {
    await put(`/consultations/${id}/cancel`);
  },

  /**
   * 接诊（医生端）
   * _Requirements: 6.3, 6.4_
   */
  accept: async (id: Id): Promise<void> => {
    await put(`/consultations/${id}/accept`);
  },

  /**
   * 结束问诊（医生端）
   */
  finish: async (id: Id): Promise<void> => {
    await put(`/consultations/${id}/finish`);
  },

  /**
   * 获取待接诊列表（医生端）
   * _Requirements: 6.2, 6.3_
   */
  getWaitingList: async (params: {
    page: number;
    pageSize: number;
  }): Promise<PageResult<ConsultationDetail>> => {
    const response = await get<any>('/consultations/waiting', params);
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
   * 获取进行中列表（医生端）
   * _Requirements: 6.2_
   */
  getInProgressList: async (params: {
    page: number;
    pageSize: number;
  }): Promise<PageResult<ConsultationDetail>> => {
    const response = await get<any>('/consultations/in-progress', params);
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
   * 获取今日统计（医生端）
   * _Requirements: 6.1_
   */
  getTodayStats: async (): Promise<{
    waiting: number;
    inProgress: number;
    finished: number;
  }> => {
    const response = await get<{
      waiting: number;
      inProgress: number;
      finished: number;
    }>('/consultations/today-stats');
    return response.data;
  },
};

export default consultationService;
