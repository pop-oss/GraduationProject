/**
 * 问诊相关类型定义
 */

import type { Id, PatientBrief, DoctorBrief, TimelineItem } from './common';

// 问诊状态枚举
export type ConsultationStatus = 'WAITING' | 'IN_PROGRESS' | 'FINISHED' | 'CANCELED';

// 问诊记录
export interface Consultation {
  id: Id;
  patientId: Id;
  doctorId: Id;
  status: ConsultationStatus;
  scheduledAt?: string;
  startedAt?: string;
  finishedAt?: string;
  createdAt: string;
  symptoms?: string;
  attachmentIds?: Id[];
}

// 问诊详情（包含关联信息）
export interface ConsultationDetail extends Consultation {
  patient?: PatientBrief;
  doctor?: DoctorBrief;
  timeline?: TimelineItem[];
  attachments?: Array<{
    id: Id;
    url: string;
    name: string;
    size: number;
  }>;
}

// 创建问诊请求
export interface CreateConsultationRequest {
  departmentId?: Id;
  doctorId: Id;
  symptoms: string;
  attachmentIds?: Id[];
  scheduledAt?: string;
}

// 问诊列表查询参数
export interface ConsultationQuery {
  page: number;
  pageSize: number;
  status?: ConsultationStatus;
  startDate?: string;
  endDate?: string;
  [key: string]: unknown;
}
