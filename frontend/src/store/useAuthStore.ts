import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserInfo } from '@/types';
import {
  setAccessToken,
  setRefreshToken,
  getAccessToken,
  getRefreshToken,
  clearAuth,
  setItem,
  getItem,
  USER_INFO_KEY,
} from '@/utils/storage';

/**
 * 认证状态接口
 * _Requirements: 1.2, 1.4, 1.5_
 */
export interface AuthState {
  // 状态
  accessToken: string | null;
  refreshToken: string | null;
  me: UserInfo | null;
  isAuthed: boolean;
  isInitialized: boolean;

  // 兼容旧接口
  token: string | null;
  user: UserInfo | null;

  // Actions
  setToken: (accessToken: string, refreshToken: string) => void;
  setMe: (me: UserInfo) => void;
  logout: () => void;
  bootstrap: () => void;

  // 兼容旧接口
  setUser: (user: UserInfo | null) => void;
}

/**
 * 认证状态管理
 * _Requirements: 1.2, 1.4, 1.5_
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // 初始状态
      accessToken: null,
      refreshToken: null,
      me: null,
      isAuthed: false,
      isInitialized: false,

      // 兼容旧接口
      token: null,
      user: null,

      /**
       * 设置 Token
       * @param accessToken 访问令牌
       * @param refreshToken 刷新令牌
       */
      setToken: (accessToken: string, refreshToken: string) => {
        // 存储到 localStorage
        setAccessToken(accessToken);
        setRefreshToken(refreshToken);

        set({
          accessToken,
          refreshToken,
          isAuthed: true,
          // 兼容旧接口
          token: accessToken,
        });
      },

      /**
       * 设置用户信息
       * @param me 用户信息
       */
      setMe: (me: UserInfo) => {
        // 存储到 localStorage
        setItem(USER_INFO_KEY, me);

        set({
          me,
          isAuthed: true,
          // 兼容旧接口
          user: me,
        });
      },

      /**
       * 登出
       * 清除所有认证状态和存储
       */
      logout: () => {
        // 清除 localStorage
        clearAuth();

        set({
          accessToken: null,
          refreshToken: null,
          me: null,
          isAuthed: false,
          // 兼容旧接口
          token: null,
          user: null,
        });
      },

      /**
       * 初始化/恢复认证状态
       * 从 localStorage 恢复 token 和用户信息
       */
      bootstrap: () => {
        const accessToken = getAccessToken();
        const refreshToken = getRefreshToken();
        const me = getItem<UserInfo>(USER_INFO_KEY);

        if (accessToken && me) {
          set({
            accessToken,
            refreshToken,
            me,
            isAuthed: true,
            isInitialized: true,
            // 兼容旧接口
            token: accessToken,
            user: me,
          });
        } else {
          set({
            isInitialized: true,
          });
        }
      },

      /**
       * 兼容旧接口：设置用户
       */
      setUser: (user: UserInfo | null) => {
        if (user) {
          get().setMe(user);
        } else {
          set({ me: null, user: null });
        }
      },
    }),
    {
      name: 'erkang-auth',
      partialize: (state) => ({
        // 只持久化必要的状态
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        me: state.me,
      }),
    }
  )
);
