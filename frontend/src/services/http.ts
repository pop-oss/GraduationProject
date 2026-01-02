/**
 * HTTP 服务封装
 * 实现统一的 API 请求处理、错误处理和拦截器
 */

import axios, { AxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';
import { message } from 'antd';
import type { HttpResponse } from '@/types';
import { getAccessToken, clearAuth } from '@/utils/storage';

// 扩展请求配置
export interface HttpConfig extends AxiosRequestConfig {
  skipErrorHandler?: boolean; // 跳过统一错误处理
  skipAuth?: boolean; // 跳过认证头
}

// 创建 Axios 实例
const httpClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器 - 添加认证头
httpClient.interceptors.request.use(
  (config) => {
    const httpConfig = config as HttpConfig;
    
    // 如果不跳过认证，添加 Token
    if (!httpConfig.skipAuth) {
      const token = getAccessToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器 - 统一处理响应和错误
httpClient.interceptors.response.use(
  (response: AxiosResponse<HttpResponse<unknown>>) => {
    const httpConfig = response.config as HttpConfig;
    
    // 如果是 blob 响应类型，直接返回（不检查 code）
    if (httpConfig.responseType === 'blob') {
      return response;
    }
    
    const res = response.data;
    
    // 业务成功
    if (res.code === 0) {
      return response;
    }
    
    // 如果不跳过错误处理，显示错误消息
    if (!httpConfig.skipErrorHandler) {
      message.error(res.message || '请求失败');
    }
    
    // 返回带有错误信息的 rejected promise
    return Promise.reject(new Error(res.message || '请求失败'));
  },
  (error: AxiosError<HttpResponse<unknown>>) => {
    const httpConfig = error.config as HttpConfig | undefined;
    const status = error.response?.status;
    const responseData = error.response?.data;
    
    // 记录 traceId 到控制台
    if (responseData?.traceId) {
      console.error(`API Error [traceId: ${responseData.traceId}]:`, responseData.message);
    }
    
    // 401 未授权 - 清除凭证并跳转登录
    if (status === 401) {
      clearAuth();
      // 避免在登录页重复跳转
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
      return Promise.reject(new Error('登录已过期，请重新登录'));
    }
    
    // 403 禁止访问
    if (status === 403) {
      if (!httpConfig?.skipErrorHandler) {
        message.error('没有权限访问该资源');
      }
      // 可以跳转到 403 页面
      // window.location.href = '/403';
      return Promise.reject(new Error('没有权限访问该资源'));
    }
    
    // 其他错误
    const errorMessage = responseData?.message || error.message || '网络请求失败';
    
    if (!httpConfig?.skipErrorHandler) {
      message.error(errorMessage);
    }
    
    return Promise.reject(new Error(errorMessage));
  }
);

/**
 * 通用请求函数
 * @param config 请求配置
 * @returns Promise<HttpResponse<T>>
 */
export async function request<T>(config: HttpConfig): Promise<HttpResponse<T>> {
  const response = await httpClient.request<HttpResponse<T>>(config);
  return response.data;
}

/**
 * GET 请求
 * @param url 请求地址
 * @param params 查询参数
 * @param config 额外配置
 */
export async function get<T>(
  url: string,
  params?: Record<string, unknown>,
  config?: HttpConfig
): Promise<HttpResponse<T>> {
  return request<T>({ ...config, method: 'GET', url, params });
}

/**
 * POST 请求
 * @param url 请求地址
 * @param data 请求体
 * @param config 额外配置
 */
export async function post<T>(
  url: string,
  data?: unknown,
  config?: HttpConfig
): Promise<HttpResponse<T>> {
  return request<T>({ ...config, method: 'POST', url, data });
}

/**
 * PUT 请求
 * @param url 请求地址
 * @param data 请求体
 * @param config 额外配置
 */
export async function put<T>(
  url: string,
  data?: unknown,
  config?: HttpConfig
): Promise<HttpResponse<T>> {
  return request<T>({ ...config, method: 'PUT', url, data });
}

/**
 * DELETE 请求
 * @param url 请求地址
 * @param config 额外配置
 */
export async function del<T>(
  url: string,
  config?: HttpConfig
): Promise<HttpResponse<T>> {
  return request<T>({ ...config, method: 'DELETE', url });
}

/**
 * PATCH 请求
 * @param url 请求地址
 * @param data 请求体
 * @param config 额外配置
 */
export async function patch<T>(
  url: string,
  data?: unknown,
  config?: HttpConfig
): Promise<HttpResponse<T>> {
  return request<T>({ ...config, method: 'PATCH', url, data });
}

// 导出 axios 实例供特殊场景使用
export { httpClient };

export default {
  request,
  get,
  post,
  put,
  del,
  patch,
};
