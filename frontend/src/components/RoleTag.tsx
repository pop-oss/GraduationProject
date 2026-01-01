/**
 * 角色标签组件
 * _Requirements: 2.7_
 */

import React from 'react';
import { Tag } from 'antd';
import { Role } from '@/types';

export interface RoleTagProps {
  role: Role | string;
}

const roleConfig: Record<string, { color: string; text: string }> = {
  [Role.PATIENT]: { color: 'blue', text: '患者' },
  [Role.DOCTOR_PRIMARY]: { color: 'green', text: '基层医生' },
  [Role.DOCTOR_EXPERT]: { color: 'purple', text: '专家医生' },
  [Role.PHARMACIST]: { color: 'cyan', text: '药师' },
  [Role.ADMIN]: { color: 'red', text: '管理员' },
};

/**
 * 角色标签组件
 * _Requirements: 2.7_
 */
export const RoleTag: React.FC<RoleTagProps> = ({ role }) => {
  const config = roleConfig[role] || { color: 'default', text: role };
  return <Tag color={config.color}>{config.text}</Tag>;
};

export default RoleTag;
