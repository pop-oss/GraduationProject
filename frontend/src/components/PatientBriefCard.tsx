/**
 * 患者简要信息卡组件
 * _Requirements: 3.1, 6.5_
 */

import React from 'react';
import { Card, Descriptions, Button, Space, Avatar } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import type { Id } from '@/types';

export interface PatientBriefCardProps {
  patient: {
    id: Id;
    name: string;
    gender?: string;
    age?: number;
    phoneMasked?: string;
    idNoMasked?: string;
    avatar?: string;
  };
  onViewDetail?: (id: Id) => void;
  extra?: React.ReactNode;
  style?: React.CSSProperties;
  /** 紧凑模式，只显示姓名和基本信息 */
  compact?: boolean;
}

/**
 * 患者简要信息卡组件
 * 实现脱敏显示
 * _Requirements: 3.1, 6.5_
 */
export const PatientBriefCard: React.FC<PatientBriefCardProps> = ({
  patient,
  onViewDetail,
  extra,
  style,
  compact = false,
}) => {
  const genderText = patient.gender === 'MALE' || patient.gender === '男' ? '男' 
    : patient.gender === 'FEMALE' || patient.gender === '女' ? '女' : '-';

  // 紧凑模式：只显示姓名和基本信息
  if (compact) {
    return (
      <Space>
        <Avatar size="small" icon={<UserOutlined />} src={patient.avatar} />
        <span>{patient.name}</span>
        {patient.gender && <span style={{ color: '#999' }}>{genderText}</span>}
        {patient.age && <span style={{ color: '#999' }}>{patient.age}岁</span>}
      </Space>
    );
  }

  return (
    <Card
      size="small"
      title={
        <Space>
          <Avatar icon={<UserOutlined />} src={patient.avatar} />
          <span>{patient.name}</span>
        </Space>
      }
      extra={
        <Space>
          {extra}
          {onViewDetail && (
            <Button type="link" size="small" onClick={() => onViewDetail(patient.id)}>
              查看详情
            </Button>
          )}
        </Space>
      }
      style={style}
    >
      <Descriptions column={2} size="small">
        <Descriptions.Item label="性别">{genderText}</Descriptions.Item>
        <Descriptions.Item label="年龄">{patient.age ? `${patient.age}岁` : '-'}</Descriptions.Item>
        {patient.phoneMasked && (
          <Descriptions.Item label="手机号">{patient.phoneMasked}</Descriptions.Item>
        )}
        {patient.idNoMasked && (
          <Descriptions.Item label="身份证">{patient.idNoMasked}</Descriptions.Item>
        )}
      </Descriptions>
    </Card>
  );
};

export default PatientBriefCard;
