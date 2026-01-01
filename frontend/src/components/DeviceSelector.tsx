/**
 * 设备选择器组件
 * _Requirements: 4.7_
 */

import React from 'react';
import { Select, Space, Typography } from 'antd';
import { AudioOutlined, VideoCameraOutlined } from '@ant-design/icons';
import type { Device } from '@/services/rtc';

const { Text } = Typography;

export interface DeviceSelectorProps {
  /** 麦克风列表 */
  mics: Device[];
  /** 摄像头列表 */
  cameras: Device[];
  /** 当前麦克风 ID */
  selectedMicId?: string;
  /** 当前摄像头 ID */
  selectedCameraId?: string;
  /** 麦克风变化回调 */
  onMicChange?: (deviceId: string) => void;
  /** 摄像头变化回调 */
  onCameraChange?: (deviceId: string) => void;
  /** 是否禁用 */
  disabled?: boolean;
}

/**
 * 设备选择器组件
 * _Requirements: 4.7_
 */
export const DeviceSelector: React.FC<DeviceSelectorProps> = ({
  mics,
  cameras,
  selectedMicId,
  selectedCameraId,
  onMicChange,
  onCameraChange,
  disabled = false,
}) => {
  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      {/* 麦克风选择 */}
      <div>
        <Text type="secondary" style={{ display: 'block', marginBottom: 4 }}>
          <AudioOutlined /> 麦克风
        </Text>
        <Select
          style={{ width: '100%' }}
          value={selectedMicId}
          onChange={onMicChange}
          disabled={disabled || mics.length === 0}
          placeholder="选择麦克风"
          options={mics.map((mic) => ({
            value: mic.deviceId,
            label: mic.label,
          }))}
        />
      </div>

      {/* 摄像头选择 */}
      <div>
        <Text type="secondary" style={{ display: 'block', marginBottom: 4 }}>
          <VideoCameraOutlined /> 摄像头
        </Text>
        <Select
          style={{ width: '100%' }}
          value={selectedCameraId}
          onChange={onCameraChange}
          disabled={disabled || cameras.length === 0}
          placeholder="选择摄像头"
          options={cameras.map((camera) => ({
            value: camera.deviceId,
            label: camera.label,
          }))}
        />
      </div>
    </Space>
  );
};

export default DeviceSelector;
