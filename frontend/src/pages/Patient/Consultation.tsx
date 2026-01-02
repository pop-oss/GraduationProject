import { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Timeline, Descriptions, Space, Spin, message, Select } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { consultationService } from '@/services/consultation';
import PageHeader from '@/components/PageHeader';
import DataTable from '@/components/DataTable';
import ConsultationStatusTag from '@/components/ConsultationStatusTag';
import DoctorBriefCard from '@/components/DoctorBriefCard';
import AttachmentList from '@/components/AttachmentList';
import ErrorState from '@/components/ErrorState';
import { formatDateTime } from '@/utils/date';
import type { Consultation, ConsultationDetail, ConsultationStatus } from '@/types';
import type { ColumnsType } from 'antd/es/table';

/**
 * 问诊列表与详情页面
 * _Requirements: 4.3, 4.4, 4.5_
 */
const ConsultationPage = () => {
  const { id } = useParams<{ id: string }>();

  // 如果有 id 参数，显示详情页面
  if (id) {
    return <ConsultationDetailView id={id} />;
  }

  // 否则显示列表页面
  return <ConsultationListView />;
};

/**
 * 问诊列表视图
 * _Requirements: 4.3_
 */
const ConsultationListView = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<Consultation[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<ConsultationStatus | undefined>();

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await consultationService.getList({ page, pageSize, status });
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

  const columns: ColumnsType<Consultation> = [
    {
      title: '问诊编号',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '预约时间',
      dataIndex: 'scheduledAt',
      key: 'scheduledAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: ConsultationStatus) => <ConsultationStatusTag status={status} />,
    },
    {
      title: '症状描述',
      dataIndex: 'symptoms',
      key: 'symptoms',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/patient/consultation/${record.id}`)}>
            查看
          </Button>
          {record.status === 'IN_PROGRESS' && (
            <Button type="primary" onClick={() => navigate(`/patient/consultation/${record.id}/room`)}>
              进入房间
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="问诊记录"
        breadcrumbs={[{ title: '首页', href: '/patient' }, { title: '问诊记录' }]}
        extra={
          <Button type="primary" onClick={() => navigate('/patient/appointment')}>
            预约问诊
          </Button>
        }
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
                { value: 'WAITING', label: '待接诊' },
                { value: 'IN_PROGRESS', label: '进行中' },
                { value: 'FINISHED', label: '已完成' },
                { value: 'CANCELED', label: '已取消' },
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
          emptyText="暂无问诊记录"
          emptyActionText="立即预约"
          onEmptyAction={() => navigate('/patient/appointment')}
        />
      </Card>
    </div>
  );
};

/**
 * 问诊详情视图
 * _Requirements: 4.4, 4.5_
 */
const ConsultationDetailView = ({ id }: { id: string }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<ConsultationDetail | null>(null);

  useEffect(() => {
    const loadDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await consultationService.getDetail(id);
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
    navigate('/patient/consultations');
  };

  const handleEnterRoom = () => {
    navigate(`/patient/consultation/${detail?.id}/room`);
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !detail) {
    return <ErrorState message={error || '问诊不存在'} onRetry={() => window.location.reload()} />;
  }

  return (
    <div>
      <PageHeader
        title={`问诊详情 #${detail.id}`}
        breadcrumbs={[
          { title: '首页', href: '/patient' },
          { title: '问诊记录', href: '/patient/consultations' },
          { title: '问诊详情' },
        ]}
        onBack={handleBack}
        extra={
          detail.status === 'IN_PROGRESS' && (
            <Button type="primary" onClick={handleEnterRoom}>
              进入视频房间
            </Button>
          )
        }
      />

      <Row gutter={24}>
        <Col xs={24} lg={16}>
          {/* 基本信息 */}
          <Card title="问诊信息" style={{ marginBottom: 24 }}>
            <Descriptions column={2}>
              <Descriptions.Item label="问诊编号">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <ConsultationStatusTag status={detail.status} />
              </Descriptions.Item>
              <Descriptions.Item label="预约时间">{formatDateTime(detail.scheduledAt)}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(detail.createdAt)}</Descriptions.Item>
              {detail.startedAt && (
                <Descriptions.Item label="开始时间">{formatDateTime(detail.startedAt)}</Descriptions.Item>
              )}
              {detail.finishedAt && (
                <Descriptions.Item label="结束时间">{formatDateTime(detail.finishedAt)}</Descriptions.Item>
              )}
              <Descriptions.Item label="症状描述" span={2}>
                {detail.symptoms || '-'}
              </Descriptions.Item>
            </Descriptions>
          </Card>

          {/* 附件 */}
          {detail.attachments && detail.attachments.length > 0 && (
            <Card title="附件" style={{ marginBottom: 24 }}>
              <AttachmentList attachments={detail.attachments} showPreview showDownload />
            </Card>
          )}
        </Col>

        <Col xs={24} lg={8}>
          {/* 医生信息 */}
          {detail.doctor && (
            <Card title="接诊医生" style={{ marginBottom: 24 }}>
              <DoctorBriefCard doctor={detail.doctor} />
            </Card>
          )}

          {/* 状态时间线 */}
          {detail.timeline && detail.timeline.length > 0 && (
            <Card title="状态时间线">
              <Timeline
                items={detail.timeline.map((item) => ({
                  children: (
                    <div>
                      <div>{item.title}</div>
                      <div style={{ color: '#999', fontSize: 12 }}>{formatDateTime(item.time)}</div>
                    </div>
                  ),
                }))}
              />
            </Card>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default ConsultationPage;
