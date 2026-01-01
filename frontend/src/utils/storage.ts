/**
 * localStorage 封装工具函数
 */

const PREFIX = 'erkang_';

/**
 * 获取带前缀的存储键名
 * @param key 原始键名
 * @returns 带前缀的键名
 */
function getKey(key: string): string {
  return PREFIX + key;
}

/**
 * 存储数据到 localStorage
 * @param key 键名
 * @param value 值（会自动序列化）
 */
export function setItem<T>(key: string, value: T): void {
  try {
    const serialized = JSON.stringify(value);
    localStorage.setItem(getKey(key), serialized);
  } catch (error) {
    console.error('Failed to save to localStorage:', error);
  }
}

/**
 * 从 localStorage 获取数据
 * @param key 键名
 * @param defaultValue 默认值
 * @returns 存储的值或默认值
 */
export function getItem<T>(key: string, defaultValue: T | null = null): T | null {
  try {
    const serialized = localStorage.getItem(getKey(key));
    if (serialized === null) return defaultValue;
    return JSON.parse(serialized) as T;
  } catch (error) {
    console.error('Failed to read from localStorage:', error);
    return defaultValue;
  }
}

/**
 * 从 localStorage 移除数据
 * @param key 键名
 */
export function removeItem(key: string): void {
  try {
    localStorage.removeItem(getKey(key));
  } catch (error) {
    console.error('Failed to remove from localStorage:', error);
  }
}

/**
 * 清除所有带前缀的 localStorage 数据
 */
export function clearAll(): void {
  try {
    const keysToRemove: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith(PREFIX)) {
        keysToRemove.push(key);
      }
    }
    keysToRemove.forEach((key) => localStorage.removeItem(key));
  } catch (error) {
    console.error('Failed to clear localStorage:', error);
  }
}

// Token 相关常量
export const TOKEN_KEY = 'access_token';
export const REFRESH_TOKEN_KEY = 'refresh_token';
export const USER_INFO_KEY = 'user_info';

/**
 * 存储访问令牌
 * @param token 访问令牌
 */
export function setAccessToken(token: string): void {
  setItem(TOKEN_KEY, token);
}

/**
 * 获取访问令牌
 * @returns 访问令牌或 null
 */
export function getAccessToken(): string | null {
  return getItem<string>(TOKEN_KEY);
}

/**
 * 存储刷新令牌
 * @param token 刷新令牌
 */
export function setRefreshToken(token: string): void {
  setItem(REFRESH_TOKEN_KEY, token);
}

/**
 * 获取刷新令牌
 * @returns 刷新令牌或 null
 */
export function getRefreshToken(): string | null {
  return getItem<string>(REFRESH_TOKEN_KEY);
}

/**
 * 清除所有认证相关数据
 */
export function clearAuth(): void {
  removeItem(TOKEN_KEY);
  removeItem(REFRESH_TOKEN_KEY);
  removeItem(USER_INFO_KEY);
}
