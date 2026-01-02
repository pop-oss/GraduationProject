/**
 * RolePermission 工具函数
 * _Requirements: 2.2, 2.3, 3.4_
 */

import type { DataNode } from 'antd/es/tree'
import type { Permission } from '@/services/admin'

/**
 * 将权限列表转换为树形结构
 * _Requirements: 2.2_
 */
export const convertToTreeData = (permissions: Permission[]): DataNode[] => {
  return permissions.map(perm => ({
    key: perm.id,
    title: `${perm.permName} (${perm.permCode})`,
    children: perm.children ? convertToTreeData(perm.children) : undefined,
  }))
}

/**
 * 获取所有权限ID（用于全选）
 * _Requirements: 3.4_
 */
export const getAllPermissionIds = (permissions: Permission[]): number[] => {
  const ids: number[] = []
  const traverse = (perms: Permission[]) => {
    perms.forEach(p => {
      ids.push(p.id)
      if (p.children) traverse(p.children)
    })
  }
  traverse(permissions)
  return ids
}
