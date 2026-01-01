/**
 * 审方相关类型定义
 */

import type { Id } from './common';

// 审方决策枚举
export type ReviewDecision = 'APPROVE' | 'REJECT' | 'NEED_MORE_INFO';

// 审方状态枚举
export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'NEED_MORE_INFO';

// 审方提交请求
export interface ReviewSubmission {
  prescriptionId: Id;
  decision: ReviewDecision;
  reason: string;
  riskLevel?: string;
  suggestion?: string;
}

// 审方记录
export interface Review {
  id: Id;
  prescriptionId: Id;
  pharmacistId: Id;
  status: ReviewStatus;
  decision?: ReviewDecision;
  reason?: string;
  riskLevel?: string;
  suggestion?: string;
  createdAt: string;
}

// 审方详情（包含处方和患者信息）
export interface ReviewDetail extends Review {
  prescription?: {
    id: Id;
    diagnosis?: string[];
    items: Array<{
      drugName: string;
      spec?: string;
      dosage?: string;
      frequency?: string;
      quantity?: number;
      usage?: string;
    }>;
    doctorName?: string;
    createdAt: string;
  };
  patientSummary?: {
    name: string;
    gender?: string;
    age?: number;
    allergies?: string[];
  };
}

// 审方列表查询参数
export interface ReviewQuery {
  page: number;
  pageSize: number;
  status?: ReviewStatus;
  startDate?: string;
  endDate?: string;
}
