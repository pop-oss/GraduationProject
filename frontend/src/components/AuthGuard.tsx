import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import type { Role } from '@/types';
import { useEffect } from 'react';

interface AuthGuardProps {
  children: React.ReactNode;
  allowedRoles?: Role[];
}

/**
 * 路由权限守卫
 * _Requirements: 1.1, 1.6, 1.7_
 */
export const AuthGuard: React.FC<AuthGuardProps> = ({ children, allowedRoles }) => {
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

/**
 * ProtectedRoute 组件 - AuthGuard 的别名
 * 用于路由配置中的权限保护
 */
export const ProtectedRoute = AuthGuard;

export default AuthGuard;
