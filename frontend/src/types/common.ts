/**
 * 通用类型定义
 */

// 通用 ID 类型
export type Id = number | string;

// 分页查询参数
export interface PageQuery {
  page: number;
  pageSize: number;
}

// 分页结果
export interface PageResult<T> {
  list: T[];
  page: number;
  pageSize: number;
  total: number;
}

// HTTP 响应结构
export interface HttpResponse<T> {
  code: number;
  message: string;
  data: T;
  traceId: string;
}

// 别名，保持向后兼容
export type ApiResponse<T = unknown> = HttpResponse<T>;

// 时间线项
export interface TimelineItem {
  id: Id;
  title: string;
  description?: string;
  time: string;
  status?: string;
}

// 患者简要信息
export interface PatientBrief {
  id: Id;
  name: string;
  gender?: string;
  age?: number;
  phoneMasked?: string;
  idNoMasked?: string;
}

// 医生简要信息
export interface DoctorBrief {
  id: Id;
  name: string;
  title?: string;
  department?: string;
  avatar?: string;
}

// 上传文件信息
export interface UploadedFile {
  id: Id;
  url: string;
  name: string;
  size: number;
  type?: string;
}
