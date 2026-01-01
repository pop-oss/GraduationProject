/**
 * 网络状态指示器组件
 * _Requirements: 4.7_
 */

import React from 'react';
import { Tooltip, Space } from 'antd';
import { WifiOutlined } from '@ant-design/icons';

export interface NetworkIndicatorProps {
  /** 网络质量 0-5，0 表示未知 */
  quality: number;
  /** 是否显示文字 */
  showText?: boolean;
}

const qualityConfig: Record<number, { color: string; text: string }> = {
  0: { color: '#999', text: '未知' },
  1: { color: '#ff4d4f', text: '极差' },
  2: { color: '#fa8c16', text: '较差' },
  3: { color: '#faad14', text: '一般' },
  4: { color: '#52c41a', text: '良好' },
  5: { color: '#52c41a', text: '优秀' },
};

/**
 * 网络状态指示器组件
 * _Requirements: 4.7_
 */
export const NetworkIndicator: React.FC<NetworkIndicatorProps> = ({
  quality,
  showText = false,
}) => {
  const config = qualityConfig[quality] || qualityConfig[0];

  return (
    <Tooltip title={`网络状态: ${config.text}`}>
      <Space size={4}>
        <WifiOutlined style={{ color: config.color, fontSize: 16 }} />
        {showText && <span style={{ color: config.color }}>{config.text}</span>}
      </Space>
    </Tooltip>
  );
};

export default NetworkIndicator;
