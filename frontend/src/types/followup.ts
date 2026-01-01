/**
 * 随访相关类型定义
 */

import type { Id } from './common';

// 问题类型枚举
export type QuestionType = 'text' | 'single' | 'multi' | 'number' | 'rate';

// 随访状态枚举
export type FollowupStatus = 'PENDING' | 'COMPLETED' | 'EXPIRED' | 'CANCELED';

// 随访问题选项
export interface QuestionOption {
  label: string;
  value: string;
}

// 随访问题
export interface FollowupQuestion {
  id: Id;
  title: string;
  type: QuestionType;
  options?: QuestionOption[];
  required?: boolean;
  placeholder?: string;
  min?: number;
  max?: number;
}

// 随访答案
export interface FollowupAnswer {
  questionId: Id;
  value: unknown;
}

// 随访计划
export interface FollowupPlan {
  id: Id;
  patientId: Id;
  doctorId: Id;
  consultationId?: Id;
  title?: string;
  description?: string;
  questions: FollowupQuestion[];
  scheduledAt: string;
  status: FollowupStatus;
  createdAt: string;
}

// 随访计划详情
export interface FollowupPlanDetail extends FollowupPlan {
  doctorName?: string;
  answers?: FollowupAnswer[];
  completedAt?: string;
}

// 提交随访答案请求
export interface SubmitFollowupRequest {
  planId: Id;
  answers: FollowupAnswer[];
}

// 随访列表查询参数
export interface FollowupQuery {
  page: number;
  pageSize: number;
  status?: FollowupStatus;
  [key: string]: unknown;
}
