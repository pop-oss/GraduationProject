/**
 * 处方预览组件
 * _Requirements: 5.2_
 */

import React from 'react';
import { Card, Table, Tag, Descriptions, Space } from 'antd';
import type { Id, PrescriptionItem } from '@/types';
import { formatDateTime } from '@/utils/date';

export interface PrescriptionPreviewProps {
  prescription: {
    id: Id;
    consultationId: Id;
    status: string;
    doctorName?: string;
    createdAt: string;
    diagnosis?: string[];
    items: PrescriptionItem[];
  };
  showDoctor?: boolean;
  extra?: React.ReactNode;
  style?: React.CSSProperties;
}

const statusConfig: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' },
  PENDING_REVIEW: { color: 'orange', text: '待审核' },
  APPROVED: { color: 'success', text: '已通过' },
  REJECTED: { color: 'error', text: '已驳回' },
};

/**
 * 处方预览组件
 * 展示处方明细、诊断、状态
 * _Requirements: 5.2_
 */
export const PrescriptionPreview: React.FC<PrescriptionPreviewProps> = ({
  prescription,
  showDoctor = true,
  extra,
  style,
}) => {
  console.log('[PrescriptionPreview] prescription:', prescription);
  console.log('[PrescriptionPreview] items:', prescription.items);
  
  const statusInfo = statusConfig[prescription.status] || { color: 'default', text: prescription.status };

  const columns = [
    {
      title: '药品名称',
      dataIndex: 'drugName',
      key: 'drugName',
    },
    {
      title: '规格',
      dataIndex: 'drugSpec',
      key: 'drugSpec',
      render: (text: string) => text || '-',
    },
    {
      title: '用法用量',
      dataIndex: 'dosage',
      key: 'dosage',
      render: (text: string) => text || '-',
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      render: (text: number) => text || '-',
    },
    {
      title: '备注',
      dataIndex: 'notes',
      key: 'notes',
      render: (text: string) => text || '-',
    },
  ];

  return (
    <Card
      title={
        <Space>
          <span>处方详情</span>
          <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
        </Space>
      }
      extra={extra}
      style={style}
    >
      <Descriptions column={2} size="small" style={{ marginBottom: 16 }}>
        <Descriptions.Item label="处方编号">{prescription.id}</Descriptions.Item>
        <Descriptions.Item label="开具时间">{formatDateTime(prescription.createdAt)}</Descriptions.Item>
        {showDoctor && prescription.doctorName && (
          <Descriptions.Item label="开方医生">{prescription.doctorName}</Descriptions.Item>
        )}
        {prescription.diagnosis && prescription.diagnosis.length > 0 && (
          <Descriptions.Item label="诊断" span={2}>
            {prescription.diagnosis.join('；')}
          </Descriptions.Item>
        )}
      </Descriptions>

      <Table
        columns={columns}
        dataSource={prescription.items}
        rowKey={(_, index) => `item-${index}`}
        pagination={false}
        size="small"
      />
    </Card>
  );
};

export default PrescriptionPreview;
