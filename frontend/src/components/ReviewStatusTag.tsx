/**
 * 审方状态标签组件
 * _Requirements: 2.7_
 */

import React from 'react';
import { Tag } from 'antd';
import type { ReviewStatus } from '@/types';

export interface ReviewStatusTagProps {
  status: ReviewStatus;
}

const statusConfig: Record<ReviewStatus, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待审核' },
  APPROVED: { color: 'success', text: '已通过' },
  REJECTED: { color: 'error', text: '已驳回' },
  NEED_MORE_INFO: { color: 'warning', text: '需补充' },
};

/**
 * 审方状态标签组件
 * _Requirements: 2.7_
 */
export const ReviewStatusTag: React.FC<ReviewStatusTagProps> = ({ status }) => {
  const config = statusConfig[status] || { color: 'default', text: status };
  return <Tag color={config.color}>{config.text}</Tag>;
};

export default ReviewStatusTag;
