/**
 * 医生随访管理页面
 * _Requirements: 8.1, 8.4, 8.5_
 */

import { useState, useEffect } from 'react';
import { Card, Button, Space, Table, message, Select, Tag, Tabs, Modal, Form, Input, DatePicker, InputNumber } from 'antd';
import { PlusOutlined, EyeOutlined, CheckOutlined, FlagOutlined } from '@ant-design/icons';
import { get, post } from '@/services/http';
import PageHeader from '@/components/PageHeader';
import { formatDateTime } from '@/utils/date';
import type { ColumnsType } from 'antd/es/table';

interface FollowupPlan {
  id: number;
  patientId: number;
  patientName?: string;
  doctorId: number;
  consultationId?: number;
  title?: string;
  description?: string;
  status: string;
  scheduledAt: string;
  createdAt: string;
  questions?: any[];
}

interface FollowupRecord {
  id: number;
  planId: number;
  patientName?: string;
  symptoms?: string;
  answers?: string;
  status: string;
  hasRedFlag?: boolean;
  submittedAt?: string;
  reviewedAt?: string;
  reviewComment?: string;
  createdAt: string;
}

const planStatusConfig: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待填写' },
  COMPLETED: { color: 'success', text: '已完成' },
  EXPIRED: { color: 'default', text: '已过期' },
  CANCELED: { color: 'error', text: '已取消' },
};

const recordStatusConfig: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待提交' },
  SUBMITTED: { color: 'processing', text: '待审阅' },
  REVIEWED: { color: 'success', text: '已审阅' },
};

/**
 * 医生随访管理页面
 */
const DoctorFollowupPage = () => {
  const [activeTab, setActiveTab] = useState('plans');

  return (
    <div>
      <PageHeader
        title="随访管理"
        breadcrumbs={[{ title: '工作台', href: '/doctor' }, { title: '随访管理' }]}
      />

      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'plans', label: '我的随访计划', children: <FollowupPlanList /> },
            { key: 'pending', label: '待审阅记录', children: <PendingReviewList /> },
            { key: 'redflags', label: '红旗征象', children: <RedFlagList /> },
          ]}
        />
      </Card>
    </div>
  );
};


/**
 * 随访计划列表
 */
const FollowupPlanList = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<FollowupPlan[]>([]);
  const [status, setStatus] = useState<string | undefined>();
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [currentPlan, setCurrentPlan] = useState<FollowupPlan | null>(null);
  const [form] = Form.useForm();
  const [creating, setCreating] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await get<FollowupPlan[]>('/followup/plan/created');
      let list = response.data || [];
      if (status) {
        list = list.filter(item => item.status === status);
      }
      setData(list);
    } catch (err) {
      message.error((err as Error).message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [status]);

  const handleCreate = async (values: any) => {
    setCreating(true);
    try {
      await post('/followup/plan', {
        ...values,
        scheduledAt: values.scheduledAt?.format('YYYY-MM-DD HH:mm:ss'),
        questions: values.questions || [],
      });
      message.success('创建成功');
      setCreateModalVisible(false);
      form.resetFields();
      loadData();
    } catch (err) {
      message.error((err as Error).message || '创建失败');
    } finally {
      setCreating(false);
    }
  };

  const handleCancel = async (planId: number) => {
    try {
      await post(`/followup/plan/${planId}/cancel`);
      message.success('已取消');
      loadData();
    } catch (err) {
      message.error((err as Error).message || '操作失败');
    }
  };

  const handleViewDetail = (record: FollowupPlan) => {
    setCurrentPlan(record);
    setDetailModalVisible(true);
  };

  const columns: ColumnsType<FollowupPlan> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '标题', dataIndex: 'title', key: 'title', render: (text) => text || '随访问卷' },
    { title: '患者ID', dataIndex: 'patientId', key: 'patientId', width: 100 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = planStatusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '计划时间',
      dataIndex: 'scheduledAt',
      key: 'scheduledAt',
      render: (text) => formatDateTime(text),
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
      width: 150,
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)}>
            详情
          </Button>
          {record.status === 'PENDING' && (
            <Button type="link" size="small" danger onClick={() => handleCancel(record.id)}>
              取消
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
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
              { value: 'CANCELED', label: '已取消' },
            ]}
          />
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
          创建随访计划
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        locale={{ emptyText: '暂无随访计划' }}
        pagination={{ showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
      />

      <Modal
        title="创建随访计划"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        footer={null}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="patientId" label="患者ID" rules={[{ required: true, message: '请输入患者ID' }]}>
            <InputNumber style={{ width: '100%' }} placeholder="请输入患者ID" />
          </Form.Item>
          <Form.Item name="title" label="标题">
            <Input placeholder="随访问卷标题" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} placeholder="随访说明" />
          </Form.Item>
          <Form.Item name="scheduledAt" label="计划时间" rules={[{ required: true, message: '请选择计划时间' }]}>
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={creating}>
                创建
              </Button>
              <Button onClick={() => setCreateModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="随访计划详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={<Button onClick={() => setDetailModalVisible(false)}>关闭</Button>}
        width={600}
      >
        {currentPlan && (
          <div>
            <p><strong>ID：</strong>{currentPlan.id}</p>
            <p><strong>标题：</strong>{currentPlan.title || '随访问卷'}</p>
            <p><strong>患者ID：</strong>{currentPlan.patientId}</p>
            <p><strong>状态：</strong>
              <Tag color={planStatusConfig[currentPlan.status]?.color || 'default'}>
                {planStatusConfig[currentPlan.status]?.text || currentPlan.status}
              </Tag>
            </p>
            <p><strong>描述：</strong>{currentPlan.description || '无'}</p>
            <p><strong>计划时间：</strong>{formatDateTime(currentPlan.scheduledAt)}</p>
            <p><strong>创建时间：</strong>{formatDateTime(currentPlan.createdAt)}</p>
            {currentPlan.questions && currentPlan.questions.length > 0 && (
              <div>
                <p><strong>问卷问题：</strong></p>
                <ul>
                  {currentPlan.questions.map((q: any, index: number) => (
                    <li key={index}>{q.title || q.question || `问题 ${index + 1}`}</li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};


/**
 * 待审阅记录列表
 */
const PendingReviewList = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<FollowupRecord[]>([]);
  const [reviewModalVisible, setReviewModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<FollowupRecord | null>(null);
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await get<FollowupRecord[]>('/followup/record/pending-review');
      setData(response.data || []);
    } catch (err) {
      message.error((err as Error).message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleReview = (record: FollowupRecord) => {
    setCurrentRecord(record);
    setReviewModalVisible(true);
  };

  const handleSubmitReview = async (values: any) => {
    if (!currentRecord) return;
    setSubmitting(true);
    try {
      await post(`/followup/record/${currentRecord.id}/review`, null, {
        params: { comment: values.comment, nextAction: values.nextAction },
      });
      message.success('审阅完成');
      setReviewModalVisible(false);
      form.resetFields();
      loadData();
    } catch (err) {
      message.error((err as Error).message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  const columns: ColumnsType<FollowupRecord> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '计划ID', dataIndex: 'planId', key: 'planId', width: 100 },
    {
      title: '红旗征象',
      dataIndex: 'hasRedFlag',
      key: 'hasRedFlag',
      width: 100,
      render: (hasRedFlag: boolean) =>
        hasRedFlag ? <Tag color="error" icon={<FlagOutlined />}>有</Tag> : <Tag>无</Tag>,
    },
    { title: '症状描述', dataIndex: 'symptoms', key: 'symptoms', ellipsis: true },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button type="primary" size="small" icon={<CheckOutlined />} onClick={() => handleReview(record)}>
          审阅
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        locale={{ emptyText: '暂无待审阅记录' }}
        pagination={{ showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
      />

      <Modal
        title="审阅随访记录"
        open={reviewModalVisible}
        onCancel={() => setReviewModalVisible(false)}
        footer={null}
        destroyOnClose
      >
        {currentRecord && (
          <div style={{ marginBottom: 16 }}>
            <p><strong>症状描述：</strong>{currentRecord.symptoms || '无'}</p>
            <p><strong>问卷答案：</strong>{currentRecord.answers || '无'}</p>
          </div>
        )}
        <Form form={form} layout="vertical" onFinish={handleSubmitReview}>
          <Form.Item name="comment" label="审阅意见">
            <Input.TextArea rows={3} placeholder="请输入审阅意见" />
          </Form.Item>
          <Form.Item name="nextAction" label="后续建议">
            <Input.TextArea rows={2} placeholder="如需复诊、调整用药等" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={submitting}>
                提交审阅
              </Button>
              <Button onClick={() => setReviewModalVisible(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};


/**
 * 红旗征象记录列表
 */
const RedFlagList = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<FollowupRecord[]>([]);

  const loadData = async () => {
    setLoading(true);
    try {
      const response = await get<FollowupRecord[]>('/followup/record/red-flags');
      setData(response.data || []);
    } catch (err) {
      message.error((err as Error).message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const columns: ColumnsType<FollowupRecord> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '计划ID', dataIndex: 'planId', key: 'planId', width: 100 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => {
        const config = recordStatusConfig[status] || { color: 'default', text: status };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    { title: '症状描述', dataIndex: 'symptoms', key: 'symptoms', ellipsis: true },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '审阅时间',
      dataIndex: 'reviewedAt',
      key: 'reviewedAt',
      render: (text) => text ? formatDateTime(text) : '-',
    },
    { title: '审阅意见', dataIndex: 'reviewComment', key: 'reviewComment', ellipsis: true },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16, padding: 12, background: '#fff2f0', borderRadius: 4 }}>
        <FlagOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
        <span style={{ color: '#ff4d4f' }}>以下记录包含红旗征象，请优先关注处理</span>
      </div>
      <Table
        columns={columns}
        dataSource={data}
        rowKey="id"
        loading={loading}
        locale={{ emptyText: '暂无红旗征象记录' }}
        pagination={{ showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
      />
    </div>
  );
};

export default DoctorFollowupPage;
