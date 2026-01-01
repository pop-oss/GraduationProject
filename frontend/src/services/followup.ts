/**
 * 随访服务
 * _Requirements: 5.3, 5.4, 5.5_
 */

import { get, post } from './http';
import type {
  FollowupPlan,
  FollowupPlanDetail,
  FollowupQuery,
  SubmitFollowupRequest,
  PageResult,
  Id,
} from '@/types';

/**
 * 随访服务
 * _Requirements: 5.3, 5.4, 5.5_
 */
export const followupService = {
  /**
   * 获取随访计划列表
   * _Requirements: 5.3_
   */
  getList: async (params: FollowupQuery): Promise<PageResult<FollowupPlan>> => {
    const response = await get<any>('/followups', params);
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
   * 获取随访计划详情
   * _Requirements: 5.4_
   */
  getDetail: async (id: Id): Promise<FollowupPlanDetail> => {
    const response = await get<FollowupPlanDetail>(`/followups/${id}`);
    return response.data;
  },

  /**
   * 提交随访问卷答案
   * _Requirements: 5.5_
   */
  submitAnswers: async (data: SubmitFollowupRequest): Promise<void> => {
    await post(`/followups/${data.planId}/submit`, { answers: data.answers });
  },

  /**
   * 创建随访计划（医生端）
   */
  create: async (data: {
    patientId: Id;
    consultationId?: Id;
    title?: string;
    description?: string;
    questions: Array<{
      title: string;
      type: string;
      options?: Array<{ label: string; value: string }>;
      required?: boolean;
    }>;
    scheduledAt: string;
  }): Promise<FollowupPlan> => {
    const response = await post<FollowupPlan>('/followups', data);
    return response.data;
  },
};

export default followupService;
