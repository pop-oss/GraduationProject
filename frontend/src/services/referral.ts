/**
 * 转诊服务
 * _Requirements: 7.1, 7.2_
 */

import { get, post, put } from './http';
import type { Id, PageResult } from '@/types';

// 转诊状态
export type ReferralStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COMPLETED' | 'CANCELED';

// 转诊记录
export interface Referral {
  id: Id;
  consultationId: Id;
  fromDoctorId: Id;
  toDoctorId?: Id;
  toDepartmentId?: Id;
  status: ReferralStatus;
  summary: string;
  description?: string;
  attachmentIds?: Id[];
  createdAt: string;
  updatedAt?: string;
}

// 转诊详情
export interface ReferralDetail extends Referral {
  fromDoctorName?: string;
  toDoctorName?: string;
  toDepartmentName?: string;
  patientName?: string;
  attachments?: Array<{
    id: Id;
    url: string;
    name: string;
    size: number;
  }>;
}

// 创建转诊请求
export interface CreateReferralRequest {
  consultationId: Id;
  toDoctorId?: Id;
  toDepartmentId?: Id;
  summary: string;
  description?: string;
  attachmentIds?: Id[];
}

// 转诊查询参数
export interface ReferralQuery {
  page: number;
  pageSize: number;
  status?: ReferralStatus;
  [key: string]: unknown;
}

/**
 * 转诊服务
 * _Requirements: 7.1, 7.2_
 */
export const referralService = {
  /**
   * 创建转诊
   * _Requirements: 7.1_
   */
  create: async (data: CreateReferralRequest): Promise<Referral> => {
    const response = await post<Referral>('/referrals', data);
    return response.data;
  },

  /**
   * 获取转诊列表
   * _Requirements: 7.2_
   */
  getList: async (params: ReferralQuery): Promise<PageResult<Referral>> => {
    const response = await get<PageResult<Referral>>('/referrals', params);
    return response.data;
  },

  /**
   * 获取转诊详情
   */
  getDetail: async (id: Id): Promise<ReferralDetail> => {
    const response = await get<ReferralDetail>(`/referrals/${id}`);
    return response.data;
  },

  /**
   * 接受转诊
   */
  accept: async (id: Id): Promise<void> => {
    await put(`/referrals/${id}/accept`);
  },

  /**
   * 拒绝转诊
   */
  reject: async (id: Id, reason: string): Promise<void> => {
    await put(`/referrals/${id}/reject`, { reason });
  },

  /**
   * 取消转诊
   */
  cancel: async (id: Id): Promise<void> => {
    await put(`/referrals/${id}/cancel`);
  },
};

export default referralService;
