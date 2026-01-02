/**
 * 菜单配置
 * _Requirements: 1.6_
 */

import { Role } from '@/types';

/**
 * 菜单项接口
 */
export interface MenuItem {
  key: string;
  label: string;
  icon?: string;
  path?: string;
  roles?: Role[];
  children?: MenuItem[];
}

/**
 * 完整菜单配置
 */
export const menuConfig: MenuItem[] = [
  // 患者端菜单
  {
    key: 'patient',
    label: '患者中心',
    icon: 'UserOutlined',
    roles: [Role.PATIENT],
    children: [
      {
        key: 'patient-home',
        label: '首页',
        path: '/patient',
        roles: [Role.PATIENT],
      },
      {
        key: 'patient-appointment',
        label: '预约问诊',
        path: '/patient/appointment',
        roles: [Role.PATIENT],
      },
      {
        key: 'patient-records',
        label: '问诊记录',
        path: '/patient/consultations',
        roles: [Role.PATIENT],
      },
      {
        key: 'patient-prescriptions',
        label: '我的处方',
        path: '/patient/prescriptions',
        roles: [Role.PATIENT],
      },
      {
        key: 'patient-followups',
        label: '随访计划',
        path: '/patient/followups',
        roles: [Role.PATIENT],
      },
      {
        key: 'patient-ai-chat',
        label: 'AI健康问答',
        path: '/patient/ai-chat',
        roles: [Role.PATIENT],
      },
    ],
  },
  // 医生端菜单
  {
    key: 'doctor',
    label: '医生工作台',
    icon: 'MedicineBoxOutlined',
    roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
    children: [
      {
        key: 'doctor-workbench',
        label: '工作台',
        path: '/doctor',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
      },
      {
        key: 'doctor-waiting',
        label: '待接诊',
        path: '/doctor/waiting',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
      },
      {
        key: 'doctor-referral',
        label: '转诊管理',
        path: '/doctor/referral',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
      },
      {
        key: 'doctor-mdt',
        label: 'MDT会诊',
        path: '/doctor/mdt',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
      },
      {
        key: 'doctor-followups',
        label: '随访管理',
        path: '/doctor/followups',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT],
      },
    ],
  },
  // 药师端菜单
  {
    key: 'pharmacist',
    label: '药师审方',
    icon: 'ExperimentOutlined',
    roles: [Role.PHARMACIST],
    children: [
      {
        key: 'pharmacist-pending',
        label: '待审处方',
        path: '/pharmacist',
        roles: [Role.PHARMACIST],
      },
      {
        key: 'pharmacist-history',
        label: '审方历史',
        path: '/pharmacist/history',
        roles: [Role.PHARMACIST],
      },
    ],
  },
  // 管理员端菜单
  {
    key: 'admin',
    label: '系统管理',
    icon: 'SettingOutlined',
    roles: [Role.ADMIN],
    children: [
      {
        key: 'admin-dashboard',
        label: '控制台',
        path: '/admin',
        roles: [Role.ADMIN],
      },
      {
        key: 'admin-users',
        label: '用户管理',
        path: '/admin/users',
        roles: [Role.ADMIN],
      },
      {
        key: 'admin-roles',
        label: '权限管理',
        path: '/admin/roles',
        roles: [Role.ADMIN],
      },
      {
        key: 'admin-audit-log',
        label: '审计日志',
        path: '/admin/audit-log',
        roles: [Role.ADMIN],
      },
    ],
  },
  // 统计分析（医生和管理员可见）
  {
    key: 'stats',
    label: '统计分析',
    icon: 'BarChartOutlined',
    roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT, Role.ADMIN],
    children: [
      {
        key: 'stats-dashboard',
        label: '统计看板',
        path: '/stats',
        roles: [Role.DOCTOR_PRIMARY, Role.DOCTOR_EXPERT, Role.ADMIN],
      },
    ],
  },
];

/**
 * 根据用户角色过滤菜单项
 * _Requirements: 1.6_
 * @param userRoles 用户角色列表
 * @returns 过滤后的菜单项
 */
export function filterMenuByRoles(userRoles: string[]): MenuItem[] {
  const filterItems = (items: MenuItem[]): MenuItem[] => {
    return items
      .filter((item) => {
        // 如果没有指定角色限制，则所有人可见
        if (!item.roles || item.roles.length === 0) {
          return true;
        }
        // 检查用户是否有任一所需角色
        return item.roles.some((role) => userRoles.includes(role));
      })
      .map((item) => ({
        ...item,
        children: item.children ? filterItems(item.children) : undefined,
      }))
      .filter((item) => {
        // 过滤掉没有子菜单的父菜单
        if (item.children && item.children.length === 0) {
          return false;
        }
        return true;
      });
  };

  return filterItems(menuConfig);
}

/**
 * 获取用户默认首页路径
 * @param userRoles 用户角色列表
 * @returns 默认首页路径
 */
export function getDefaultHomePath(userRoles: string[]): string {
  if (userRoles.includes(Role.PATIENT)) {
    return '/patient';
  }
  if (userRoles.includes(Role.DOCTOR_PRIMARY) || userRoles.includes(Role.DOCTOR_EXPERT)) {
    return '/doctor';
  }
  if (userRoles.includes(Role.PHARMACIST)) {
    return '/pharmacist';
  }
  if (userRoles.includes(Role.ADMIN)) {
    return '/admin';
  }
  return '/login';
}

export default menuConfig;
