/**
 * 患者随访页面
 * _Requirements: 5.3, 5.4, 5.5_
 */

import { useState, useEffect } from 'react';
import { Card, Button, Space, Spin, message, Select, Tag } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { followupService } from '@/services/followup';
import PageHeader from '@/components/PageHeader';
import DataTable from '@/components/DataTable';
import FollowupQuestionnaire from '@/components/FollowupQuestionnaire';
import ErrorState from '@/components/ErrorState';
import { formatDateTime } from '@/utils/date';
import type { FollowupPlan, FollowupPlanDetail, FollowupStatus, FollowupAnswer } from '@/types';
import type { ColumnsType } from 'antd/es/table';

const statusConfig: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待填写' },
  COMPLETED: { color: 'success', text: '已完成' },
  EXPIRED: { color: 'default', text: '已过期' },
  CANCELED: { color: 'error', text: '已取消' },
};

/**
 * 随访页面入口
 * _Requirements: 5.3, 5.4, 5.5_
 */
const FollowupsPage = () => {
  const { id } = useParams<{ id: string }>();

  if (id) {
    return <FollowupDetailView id={id} />;
  }

  return <FollowupListView />;
};

/**
 * 随访列表视图
 * _Requirements: 5.3_
 */
const FollowupListView = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<FollowupPlan[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<FollowupStatus | undefined>();

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await followupService.getList({ page, pageSize, status });
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

  const columns: ColumnsType<FollowupPlan> = [
    {
      title: '随访编号',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text) => text || '随访问卷',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: FollowupStatus) => {
        const config = statusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '问题数量',
      dataIndex: 'questions',
      key: 'questionCount',
      render: (questions: unknown[]) => `${questions?.length || 0} 题`,
    },
    {
      title: '计划时间',
      dataIndex: 'scheduledAt',
      key: 'scheduledAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/patient/followups/${record.id}`)}>
            {record.status === 'PENDING' ? '填写问卷' : '查看详情'}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="随访计划"
        breadcrumbs={[{ title: '首页', href: '/patient' }, { title: '随访计划' }]}
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
                { value: 'PENDING', label: '待填写' },
                { value: 'COMPLETED', label: '已完成' },
                { value: 'EXPIRED', label: '已过期' },
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
          emptyText="暂无随访计划"
        />
      </Card>
    </div>
  );
};

/**
 * 随访详情视图
 * _Requirements: 5.4, 5.5_
 */
const FollowupDetailView = ({ id }: { id: string }) => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<FollowupPlanDetail | null>(null);

  useEffect(() => {
    const loadDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await followupService.getDetail(id);
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
    navigate('/patient/followups');
  };

  const handleSubmit = async (answers: FollowupAnswer[]) => {
    if (!detail) return;
    
    setSubmitting(true);
    try {
      await followupService.submitAnswers({
        planId: detail.id,
        answers,
      });
      message.success('问卷提交成功');
      navigate('/patient/followups');
    } catch (err) {
      message.error((err as Error).message || '提交失败');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !detail) {
    return <ErrorState message={error || '随访计划不存在'} onRetry={() => window.location.reload()} />;
  }

  const statusInfo = statusConfig[detail.status] || { color: 'default', text: detail.status };
  const isReadonly = detail.status !== 'PENDING';

  return (
    <div>
      <PageHeader
        title={detail.title || `随访问卷 #${detail.id}`}
        breadcrumbs={[
          { title: '首页', href: '/patient' },
          { title: '随访计划', href: '/patient/followups' },
          { title: '问卷详情' },
        ]}
        onBack={handleBack}
        extra={<Tag color={statusInfo.color}>{statusInfo.text}</Tag>}
      />

      {detail.description && (
        <Card style={{ marginBottom: 24 }}>
          <p style={{ margin: 0, color: '#666' }}>{detail.description}</p>
        </Card>
      )}

      <Card title="问卷内容">
        <FollowupQuestionnaire
          questions={detail.questions}
          initialAnswers={detail.answers}
          onSubmit={handleSubmit}
          readonly={isReadonly}
          loading={submitting}
        />
      </Card>
    </div>
  );
};

export default FollowupsPage;
