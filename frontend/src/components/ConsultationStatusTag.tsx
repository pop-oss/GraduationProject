/**
 * 问诊状态标签组件
 * _Requirements: 2.7_
 */

import React from 'react';
import { Tag } from 'antd';
import type { ConsultationStatus } from '@/types';

export interface ConsultationStatusTagProps {
  status: ConsultationStatus;
}

const statusConfig: Record<ConsultationStatus, { color: string; text: string }> = {
  WAITING: { color: 'orange', text: '待接诊' },
  IN_PROGRESS: { color: 'processing', text: '进行中' },
  FINISHED: { color: 'success', text: '已完成' },
  CANCELED: { color: 'default', text: '已取消' },
};

/**
 * 问诊状态标签组件
 * _Requirements: 2.7_
 */
export const ConsultationStatusTag: React.FC<ConsultationStatusTagProps> = ({ status }) => {
  const config = statusConfig[status] || { color: 'default', text: status };
  return <Tag color={config.color}>{config.text}</Tag>;
};

export default ConsultationStatusTag;
