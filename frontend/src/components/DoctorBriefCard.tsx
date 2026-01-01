/**
 * 医生简要信息卡组件
 * _Requirements: 3.1, 6.5_
 */

import React from 'react';
import { Card, Descriptions, Button, Space, Avatar } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import type { Id } from '@/types';

export interface DoctorBriefCardProps {
  doctor: {
    id: Id;
    name: string;
    title?: string;
    department?: string;
    avatar?: string;
    hospital?: string;
  };
  onViewDetail?: (id: Id) => void;
  extra?: React.ReactNode;
  style?: React.CSSProperties;
}

/**
 * 医生简要信息卡组件
 * _Requirements: 3.1, 6.5_
 */
export const DoctorBriefCard: React.FC<DoctorBriefCardProps> = ({
  doctor,
  onViewDetail,
  extra,
  style,
}) => {
  return (
    <Card
      size="small"
      title={
        <Space>
          <Avatar icon={<UserOutlined />} src={doctor.avatar} />
          <span>{doctor.name}</span>
          {doctor.title && <span style={{ color: '#999', fontSize: 12 }}>{doctor.title}</span>}
        </Space>
      }
      extra={
        <Space>
          {extra}
          {onViewDetail && (
            <Button type="link" size="small" onClick={() => onViewDetail(doctor.id)}>
              查看详情
            </Button>
          )}
        </Space>
      }
      style={style}
    >
      <Descriptions column={2} size="small">
        {doctor.department && (
          <Descriptions.Item label="科室">{doctor.department}</Descriptions.Item>
        )}
        {doctor.hospital && (
          <Descriptions.Item label="医院">{doctor.hospital}</Descriptions.Item>
        )}
      </Descriptions>
    </Card>
  );
};

export default DoctorBriefCard;
