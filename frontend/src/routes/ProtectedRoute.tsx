/**
 * 受保护路由组件
 * _Requirements: 1.1, 1.6, 1.7_
 */

import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import type { Role } from '@/types';
import { useEffect } from 'react';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: Role[];
}

/**
 * 受保护路由组件
 * 实现未登录重定向和角色权限检查
 * _Requirements: 1.1, 1.6, 1.7_
 */
export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
}) => {
  const { isAuthed, me, isInitialized, bootstrap } = useAuthStore();
  const location = useLocation();

  // 初始化认证状态
  useEffect(() => {
    if (!isInitialized) {
      bootstrap();
    }
  }, [isInitialized, bootstrap]);

  // 等待初始化完成
  if (!isInitialized) {
    return null; // 或者返回 loading 组件
  }

  // 未登录，跳转到登录页
  // _Requirements: 1.1_
  if (!isAuthed) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 检查角色权限
  // _Requirements: 1.7_
  if (allowedRoles && me) {
    const userRoles = me.roles || [];
    const hasPermission = allowedRoles.some((role) => userRoles.includes(role));

    if (!hasPermission) {
      return <Navigate to="/403" replace />;
    }
  }

  return <>{children}</>;
};

export default ProtectedRoute;
