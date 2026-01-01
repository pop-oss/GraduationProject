/**
 * 认证相关类型定义
 */

// 用户角色枚举（保持向后兼容）
export enum Role {
  PATIENT = 'PATIENT',
  DOCTOR_PRIMARY = 'DOCTOR_PRIMARY',
  DOCTOR_EXPERT = 'DOCTOR_EXPERT',
  PHARMACIST = 'PHARMACIST',
  ADMIN = 'ADMIN',
}

// 别名，保持向后兼容
export const UserRole = Role;

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId: number;
  username: string;
  realName: string;
  avatar?: string;
  roles: string[];
}

// 用户信息
export interface UserInfo {
  id: number | string;
  username: string;
  realName: string;
  avatar?: string;
  roles: string[];
  role?: Role; // 保持向后兼容，单个角色
}

// Token 刷新请求
export interface RefreshTokenRequest {
  refreshToken: string;
}

// Token 刷新响应
export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}
