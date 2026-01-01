/**
 * 病历服务
 * _Requirements: 6.7_
 */

import { get, post, put } from './http';
import type { Id } from '@/types';

// 病历类型
export interface MedicalRecord {
  id: Id;
  consultationId: Id;
  patientId: Id;
  doctorId: Id;
  chiefComplaint?: string;
  presentIllness?: string;
  pastHistory?: string;
  allergies?: string;
  physicalExam?: string;
  diagnosis?: string;
  treatment?: string;
  advice?: string;
  createdAt: string;
  updatedAt?: string;
}

// 创建/更新病历请求
export interface SaveMedicalRecordRequest {
  consultationId: Id;
  chiefComplaint?: string;
  presentIllness?: string;
  pastHistory?: string;
  allergies?: string;
  physicalExam?: string;
  diagnosis?: string;
  treatment?: string;
  advice?: string;
}

/**
 * 病历服务
 * _Requirements: 6.7_
 */
export const medicalRecordService = {
  /**
   * 获取病历详情
   */
  getByConsultation: async (consultationId: Id): Promise<MedicalRecord | null> => {
    try {
      const response = await get<MedicalRecord>(`/medical-records/consultation/${consultationId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  /**
   * 保存病历（创建或更新）
   * _Requirements: 6.7_
   */
  save: async (data: SaveMedicalRecordRequest): Promise<MedicalRecord> => {
    const response = await post<MedicalRecord>('/medical-records', data);
    return response.data;
  },

  /**
   * 更新病历
   */
  update: async (id: Id, data: Partial<SaveMedicalRecordRequest>): Promise<MedicalRecord> => {
    const response = await put<MedicalRecord>(`/medical-records/${id}`, data);
    return response.data;
  },
};

export default medicalRecordService;
