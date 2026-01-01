/**
 * 患者处方列表与详情页面
 * _Requirements: 5.1, 5.2_
 */

import { useState, useEffect } from 'react';
import { Card, Button, Space, Spin, message, Select, Tag, Descriptions } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { prescriptionService } from '@/services/prescription';
import PageHeader from '@/components/PageHeader';
import DataTable from '@/components/DataTable';
import PrescriptionPreview from '@/components/PrescriptionPreview';
import ErrorState from '@/components/ErrorState';
import { formatDateTime } from '@/utils/date';
import type { Prescription, PrescriptionDetail, PrescriptionStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';

const statusConfig: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' },
  PENDING_REVIEW: { color: 'orange', text: '待审核' },
  APPROVED: { color: 'success', text: '已通过' },
  REJECTED: { color: 'error', text: '已驳回' },
};

/**
 * 处方页面入口
 * _Requirements: 5.1, 5.2_
 */
const PrescriptionsPage = () => {
  const { id } = useParams<{ id: string }>();

  if (id) {
    return <PrescriptionDetailView id={id} />;
  }

  return <PrescriptionListView />;
};

/**
 * 处方列表视图
 * _Requirements: 5.1_
 */
const PrescriptionListView = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Prescription[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<PrescriptionStatus | undefined>();

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await prescriptionService.getList({ page, pageSize, status });
      setData(result.list);
      setTotal(result.total);
    } catch (err) {
      message.error((err as Error).message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [page, pageSize, status]);

  const columns: ColumnsType<Prescription> = [
    {
      title: '处方编号',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '诊断',
      dataIndex: 'diagnosis',
      key: 'diagnosis',
      render: (diagnosis: string[]) => diagnosis?.join('；') || '-',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: PrescriptionStatus) => {
        const config = statusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '药品数量',
      dataIndex: 'items',
      key: 'itemCount',
      render: (items: unknown[]) => `${items?.length || 0} 种`,
    },
    {
      title: '开具时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button type="link" onClick={() => navigate(`/patient/prescriptions/${record.id}`)}>
          查看详情
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="我的处方"
        breadcrumbs={[{ title: '首页', href: '/patient' }, { title: '我的处方' }]}
      />

      <Card>
        <div style={{ marginBottom: 16 }}>
          <Space>
            <span>状态筛选：</span>
            <Select
              allowClear
              placeholder="全部状态"
              style={{ width: 150 }}
              value={status}
              onChange={setStatus}
              options={[
                { value: 'PENDING_REVIEW', label: '待审核' },
                { value: 'APPROVED', label: '已通过' },
                { value: 'REJECTED', label: '已驳回' },
              ]}
            />
          </Space>
        </div>

        <DataTable
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={(p, ps) => {
            setPage(p);
            setPageSize(ps);
          }}
          emptyText="暂无处方记录"
        />
      </Card>
    </div>
  );
};


/**
 * 处方详情视图
 * _Requirements: 5.2_
 */
const PrescriptionDetailView = ({ id }: { id: string }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<PrescriptionDetail | null>(null);

  useEffect(() => {
    const loadDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await prescriptionService.getDetail(id);
        setDetail(data);
      } catch (err) {
        setError((err as Error).message || '加载失败');
      } finally {
        setLoading(false);
      }
    };
    loadDetail();
  }, [id]);

  const handleBack = () => {
    navigate('/patient/prescriptions');
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !detail) {
    return <ErrorState message={error || '处方不存在'} onRetry={() => window.location.reload()} />;
  }

  const statusInfo = statusConfig[detail.status] || { color: 'default', text: detail.status };

  return (
    <div>
      <PageHeader
        title={`处方详情 #${detail.id}`}
        breadcrumbs={[
          { title: '首页', href: '/patient' },
          { title: '我的处方', href: '/patient/prescriptions' },
          { title: '处方详情' },
        ]}
        onBack={handleBack}
      />

      <PrescriptionPreview prescription={detail} showDoctor />

      {/* 审方结果 */}
      {detail.reviewStatus && (
        <Card title="审方结果" style={{ marginTop: 24 }}>
          <Descriptions column={2}>
            <Descriptions.Item label="审核状态">
              <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
            </Descriptions.Item>
            {detail.reviewedAt && (
              <Descriptions.Item label="审核时间">{formatDateTime(detail.reviewedAt)}</Descriptions.Item>
            )}
            {detail.reviewReason && (
              <Descriptions.Item label="审核意见" span={2}>
                {detail.reviewReason}
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>
      )}
    </div>
  );
};

export default PrescriptionsPage;
