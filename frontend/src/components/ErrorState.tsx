/**
 * 错误状态组件
 * _Requirements: 2.2_
 */

import React from 'react';
import { Result, Button, Typography } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

const { Text } = Typography;

export interface ErrorStateProps {
  /** 错误标题 */
  title?: string;
  /** 错误信息 */
  message?: string;
  /** 追踪ID，用于排查问题 */
  traceId?: string;
  /** 重试回调 */
  onRetry?: () => void;
  /** 自定义样式 */
  style?: React.CSSProperties;
}

/**
 * 错误状态组件
 * 用于展示页面加载错误、请求失败等场景
 * _Requirements: 2.2_
 */
export const ErrorState: React.FC<ErrorStateProps> = ({
  title = '加载失败',
  message = '抱歉，页面加载出现问题',
  traceId,
  onRetry,
  style,
}) => {
  return (
    <Result
      status="error"
      title={title}
      subTitle={
        <div>
          <div>{message}</div>
          {traceId && (
            <Text type="secondary" style={{ fontSize: 12, marginTop: 8, display: 'block' }}>
              追踪ID: {traceId}
            </Text>
          )}
        </div>
      }
      style={{ padding: '40px 0', ...style }}
      extra={
        onRetry && (
          <Button type="primary" icon={<ReloadOutlined />} onClick={onRetry}>
            重试
          </Button>
        )
      }
    />
  );
};

export default ErrorState;
