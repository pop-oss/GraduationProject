/**
 * 处方相关类型定义
 */

import type { Id } from './common';

// 处方状态枚举
export type PrescriptionStatus = 'DRAFT' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';

// 处方药品项
export interface PrescriptionItem {
  drugName: string;
  spec?: string;
  dosage?: string;
  frequency?: string;
  duration?: string;
  quantity?: number;
  usage?: string;
  remark?: string;
}

// 处方
export interface Prescription {
  id: Id;
  consultationId: Id;
  doctorId: Id;
  status: PrescriptionStatus;
  diagnosis?: string[];
  items: PrescriptionItem[];
  createdAt: string;
  updatedAt?: string;
}

// 处方详情（包含审方信息）
export interface PrescriptionDetail extends Prescription {
  doctorName?: string;
  patientName?: string;
  reviewStatus?: string;
  reviewReason?: string;
  reviewedAt?: string;
}

// 创建处方请求
export interface CreatePrescriptionRequest {
  consultationId: Id;
  diagnosis: string[];
  items: PrescriptionItem[];
}

// 处方列表查询参数
export interface PrescriptionQuery {
  page: number;
  pageSize: number;
  status?: PrescriptionStatus;
  consultationId?: Id;
  [key: string]: unknown;
}
