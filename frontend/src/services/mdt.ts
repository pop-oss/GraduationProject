/**
 * MDT 多学科会诊服务
 * _Requirements: 7.3, 7.4, 7.5_
 */

import { get, post, put } from './http';
import type { Id, PageResult } from '@/types';

// MDT 状态
export type MDTStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELED';

// MDT 成员
export interface MDTMember {
  id: Id;
  doctorId: Id;
  doctorName: string;
  department?: string;
  role: 'ORGANIZER' | 'PARTICIPANT';
  joinedAt?: string;
}

// MDT 记录
export interface MDT {
  id: Id;
  consultationId: Id;
  organizerId: Id;
  title: string;
  description?: string;
  status: MDTStatus;
  scheduledAt?: string;
  startedAt?: string;
  finishedAt?: string;
  conclusion?: string;
  attachmentIds?: Id[];
  createdAt: string;
}

// MDT 详情
export interface MDTDetail extends MDT {
  organizerName?: string;
  patientName?: string;
  members: MDTMember[];
  attachments?: Array<{
    id: Id;
    url: string;
    name: string;
    size: number;
  }>;
}

// 创建 MDT 请求
export interface CreateMDTRequest {
  consultationId: Id;
  title: string;
  description?: string;
  memberIds: Id[];
  scheduledAt?: string;
  attachmentIds?: Id[];
}

// MDT 查询参数
export interface MDTQuery {
  page: number;
  pageSize: number;
  status?: MDTStatus;
  [key: string]: unknown;
}

/**
 * MDT 服务
 * _Requirements: 7.3, 7.4, 7.5_
 */
export const mdtService = {
  /**
   * 创建 MDT
   * _Requirements: 7.3_
   */
  create: async (data: CreateMDTRequest): Promise<MDT> => {
    const response = await post<MDT>('/mdt', data);
    return response.data;
  },

  /**
   * 获取 MDT 列表
   */
  getList: async (params: MDTQuery): Promise<PageResult<MDT>> => {
    const response = await get<any>('/mdt', params);
    // 适配后端返回格式
    const data = response.data;
    return {
      list: data.list || [],
      total: data.total || 0,
      page: data.page || 1,
      pageSize: data.pageSize || 10,
    };
  },

  /**
   * 获取 MDT 详情
   * _Requirements: 7.4_
   */
  getDetail: async (id: Id): Promise<MDTDetail> => {
    const response = await get<MDTDetail>(`/mdt/${id}`);
    return response.data;
  },

  /**
   * 添加成员
   */
  addMember: async (id: Id, doctorId: Id): Promise<void> => {
    await post(`/mdt/${id}/members`, { doctorId });
  },

  /**
   * 移除成员
   */
  removeMember: async (id: Id, memberId: Id): Promise<void> => {
    await put(`/mdt/${id}/members/${memberId}/remove`);
  },

  /**
   * 开始会诊
   */
  start: async (id: Id): Promise<void> => {
    await put(`/mdt/${id}/start`);
  },

  /**
   * 归档结论
   * _Requirements: 7.5_
   */
  archive: async (id: Id, conclusion: string): Promise<void> => {
    await put(`/mdt/${id}/archive`, { conclusion });
  },

  /**
   * 取消 MDT
   */
  cancel: async (id: Id): Promise<void> => {
    await put(`/mdt/${id}/cancel`);
  },
};

export default mdtService;
