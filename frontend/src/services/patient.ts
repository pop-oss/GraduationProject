/**
 * 患者服务
 * _Requirements: 3.1-3.5_
 */

import { get } from './http';
import type { PatientBrief, PageResult } from '@/types';

export interface PatientProfile extends PatientBrief {
  phone?: string;
  idNo?: string;
  address?: string;
  emergencyContact?: string;
  emergencyPhone?: string;
  allergies?: string[];
  medicalHistory?: string[];
}

export interface PatientDashboard {
  profile: PatientProfile;
  upcomingConsultations: number;
  totalConsultations: number;
  activePrescriptions: number;
  pendingFollowups: number;
}

/**
 * 患者服务
 * _Requirements: 3.1-3.5_
 */
export const patientService = {
  /**
   * 获取患者仪表盘数据
   * _Requirements: 3.1, 3.2_
   */
  getDashboard: async (): Promise<PatientDashboard> => {
    const response = await get<PatientDashboard>('/patient/dashboard');
    return response.data;
  },

  /**
   * 获取患者个人信息
   * _Requirements: 3.1_
   */
  getProfile: async (): Promise<PatientProfile> => {
    const response = await get<PatientProfile>('/patient/profile');
    return response.data;
  },

  /**
   * 获取患者列表（医生端使用）
   */
  getPatients: async (params: {
    page: number;
    pageSize: number;
    keyword?: string;
  }): Promise<PageResult<PatientBrief>> => {
    const response = await get<PageResult<PatientBrief>>('/patients', params);
    return response.data;
  },

  /**
   * 获取患者详情
   */
  getPatientDetail: async (id: number | string): Promise<PatientProfile> => {
    const response = await get<PatientProfile>(`/patients/${id}`);
    return response.data;
  },
};

export default patientService;
