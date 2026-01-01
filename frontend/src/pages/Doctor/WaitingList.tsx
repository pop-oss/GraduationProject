/**
 * 医生待接诊列表页面
 * _Requirements: 6.3, 6.4_
 */

import { useState, useEffect } from 'react';
import { Card, Button, Space, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { consultationService } from '@/services/consultation';
import PageHeader from '@/components/PageHeader';
import DataTable from '@/components/DataTable';
import ConsultationStatusTag from '@/components/ConsultationStatusTag';
import PatientBriefCard from '@/components/PatientBriefCard';
import { formatDateTime } from '@/utils/date';
import type { ConsultationDetail } from '@/types';
import type { ColumnsType } from 'antd/es/table';

/**
 * 待接诊列表页面
 * _Requirements: 6.3, 6.4_
 */
const WaitingListPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<ConsultationDetail[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [accepting, setAccepting] = useState<string | number | null>(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await consultationService.getWaitingList({ page, pageSize });
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
  }, [page, pageSize]);

  const handleAccept = async (id: string | number) => {
    setAccepting(id);
    try {
      await consultationService.accept(id);
      message.success('接诊成功');
      navigate(`/doctor/consultation/${id}`);
    } catch (err) {
      message.error((err as Error).message || '接诊失败');
    } finally {
      setAccepting(null);
    }
  };

  const columns: ColumnsType<ConsultationDetail> = [
    {
      title: '问诊编号',
      dataIndex: 'id',
      key: 'id',
      width: 100,
    },
    {
      title: '患者信息',
      key: 'patient',
      width: 200,
      render: (_, record) => 
        record.patient ? (
          <PatientBriefCard patient={record.patient} compact />
        ) : '-',
    },
    {
      title: '症状描述',
      dataIndex: 'symptoms',
      key: 'symptoms',
      ellipsis: true,
    },
    {
      title: '预约时间',
      dataIndex: 'scheduledAt',
      key: 'scheduledAt',
      width: 180,
      render: (text) => formatDateTime(text),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => <ConsultationStatusTag status={status} />,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/doctor/consultation/${record.id}`)}>
            查看
          </Button>
          {record.status === 'WAITING' && (
            <Button
              type="primary"
              size="small"
              loading={accepting === record.id}
              onClick={() => handleAccept(record.id)}
            >
              接诊
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="待接诊列表"
        breadcrumbs={[{ title: '工作台', href: '/doctor' }, { title: '待接诊列表' }]}
      />

      <Card>
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
          emptyText="暂无待接诊患者"
        />
      </Card>
    </div>
  );
};

export default WaitingListPage;
