/**
 * 空状态组件
 * _Requirements: 2.1_
 */

import React from 'react';
import { Empty, Button } from 'antd';

export interface EmptyStateProps {
  /** 标题 */
  title?: string;
  /** 描述文字 */
  description?: string;
  /** 操作按钮文字 */
  actionText?: string;
  /** 操作按钮点击回调 */
  onAction?: () => void;
  /** 自定义图片 */
  image?: React.ReactNode;
  /** 自定义样式 */
  style?: React.CSSProperties;
}

/**
 * 空状态组件
 * 用于展示列表为空、搜索无结果等场景
 * _Requirements: 2.1_
 */
export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  description,
  actionText,
  onAction,
  image,
  style,
}) => {
  return (
    <Empty
      image={image || Empty.PRESENTED_IMAGE_SIMPLE}
      description={
        <div>
          {title && <div style={{ fontSize: 16, fontWeight: 500, marginBottom: 8 }}>{title}</div>}
          {description && <div style={{ color: '#999' }}>{description}</div>}
        </div>
      }
      style={{ padding: '40px 0', ...style }}
    >
      {actionText && onAction && (
        <Button type="primary" onClick={onAction}>
          {actionText}
        </Button>
      )}
    </Empty>
  );
};

export default EmptyState;
