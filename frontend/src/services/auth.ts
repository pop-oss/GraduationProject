/**
 * 认证服务
 * _Requirements: 1.2, 1.4_
 */

import { post, get } from './http';
import type { UserInfo, LoginRequest, LoginResponse, RefreshTokenResponse } from '@/types';
import { getRefreshToken } from '@/utils/storage';

export interface LoginParams extends LoginRequest {}

export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  user: UserInfo;
}

/**
 * 认证服务
 */
export const authService = {
  /**
   * 登录
   * _Requirements: 1.2_
   */
  login: async (params: LoginParams): Promise<LoginResult> => {
    const response = await post<LoginResponse>('/auth/login', params, {
      skipAuth: true,
    });
    const data = response.data;
    // 如果 roles 为空数组或 null/undefined，默认为 PATIENT
    const roles = data.roles && data.roles.length > 0 ? data.roles : ['PATIENT'];

    return {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      user: {
        id: data.userId,
        username: data.username,
        realName: data.realName,
        avatar: data.avatar,
        roles: roles,
      },
    };
  },

  /**
   * 登出
   * _Requirements: 1.5_
   */
  logout: async (): Promise<void> => {
    try {
      await post('/auth/logout', {}, { skipErrorHandler: true });
    } catch {
      // 忽略登出错误，确保本地状态被清除
    }
  },

  /**
   * 刷新 Token
   * _Requirements: 1.4_
   */
  refreshToken: async (): Promise<RefreshTokenResponse> => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await post<RefreshTokenResponse>(
      '/auth/refresh',
      { refreshToken },
      { skipAuth: true, skipErrorHandler: true }
    );
    return response.data;
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: async (): Promise<UserInfo> => {
    const response = await get<UserInfo>('/auth/me');
    return response.data;
  },

  /**
   * 更新个人信息
   */
  updateProfile: async (data: { realName?: string; phone?: string; email?: string }): Promise<void> => {
    await post('/auth/profile', data);
  },

  /**
   * 修改密码
   */
  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    await post('/auth/change-password', { oldPassword, newPassword });
  },
};

export default authService;
