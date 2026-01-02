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

// 后端返回的随访计划类型
interface BackendFollowupPlan {
  id: number;
  consultationId?: number;
  patientId: number;
  doctorId: number;
  planNo: string;
  diagnosis?: string;
  followupType?: string;
  intervalDays?: number;
  totalTimes?: number;
  completedTimes?: number;
  nextFollowupDate?: string;
  questionList?: string; // JSON字符串
  redFlags?: string;
  status: string; // ACTIVE/COMPLETED/CANCELED
  createdAt: string;
  updatedAt?: string;
}

// 状态映射：后端状态 -> 前端显示
const statusConfig: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待填写' },
  ACTIVE: { color: 'processing', text: '进行中' },
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
  const [data, setData] = useState<BackendFollowupPlan[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [status, setStatus] = useState<string | undefined>();

  const loadData = async () => {
    setLoading(true);
    try {
      const result = await followupService.getList({ page, pageSize, status: status as FollowupStatus });
      setData(result.list as unknown as BackendFollowupPlan[]);
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

  // 解析问题列表
  const parseQuestions = (questionList?: string) => {
    if (!questionList) return [];
    try {
      return JSON.parse(questionList);
    } catch {
      return [];
    }
  };

  const columns: ColumnsType<BackendFollowupPlan> = [
    {
      title: '随访编号',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '标题',
      dataIndex: 'diagnosis',
      key: 'diagnosis',
      render: (text, record) => text || `随访计划 #${record.planNo?.slice(-8) || record.id}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const config = statusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '问题数量',
      dataIndex: 'questionList',
      key: 'questionCount',
      render: (questionList: string) => `${parseQuestions(questionList).length} 题`,
    },
    {
      title: '计划时间',
      dataIndex: 'nextFollowupDate',
      key: 'nextFollowupDate',
      render: (text, record) => text || formatDateTime(record.createdAt),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/patient/followups/${record.id}`)}>
            {record.status === 'ACTIVE' ? '填写问卷' : '查看详情'}
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
                { value: 'ACTIVE', label: '进行中' },
                { value: 'COMPLETED', label: '已完成' },
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
  const [detail, setDetail] = useState<BackendFollowupPlan | null>(null);

  // 解析问题列表
  const parseQuestions = (questionList?: string) => {
    if (!questionList) return [];
    try {
      return JSON.parse(questionList);
    } catch {
      return [];
    }
  };

  useEffect(() => {
    const loadDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await followupService.getDetail(id);
        setDetail(data as unknown as BackendFollowupPlan);
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
  const isReadonly = detail.status !== 'ACTIVE';
  const questions = parseQuestions(detail.questionList);
  const title = detail.diagnosis || `随访计划 #${detail.planNo?.slice(-8) || detail.id}`;

  return (
    <div>
      <PageHeader
        title={title}
        breadcrumbs={[
          { title: '首页', href: '/patient' },
          { title: '随访计划', href: '/patient/followups' },
          { title: '问卷详情' },
        ]}
        onBack={handleBack}
        extra={<Tag color={statusInfo.color}>{statusInfo.text}</Tag>}
      />

      <Card style={{ marginBottom: 24 }}>
        <p style={{ margin: 0, color: '#666' }}>
          随访类型: {detail.followupType === 'CHRONIC' ? '慢病随访' : '常规随访'} | 
          间隔天数: {detail.intervalDays || '-'} 天 | 
          已完成: {detail.completedTimes || 0}/{detail.totalTimes || '-'} 次
        </p>
      </Card>

      <Card title="问卷内容">
        {questions.length > 0 ? (
          <FollowupQuestionnaire
            questions={questions}
            initialAnswers={[]}
            onSubmit={handleSubmit}
            readonly={isReadonly}
            loading={submitting}
          />
        ) : (
          <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
            暂无问卷内容
          </div>
        )}
      </Card>
    </div>
  );
};

export default FollowupsPage;
