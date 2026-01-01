import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Form, Input, Select, Tag, message, Space } from 'antd'
import { PlusOutlined, SendOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import { referralService, type Referral, type ReferralStatus } from '@/services/referral'
import { get } from '@/services/http'
import { formatDateTime } from '@/utils/date'

const { TextArea } = Input

interface Doctor {
  id: number
  realName: string
  title: string
  departmentName: string
}

const statusMap: Record<ReferralStatus, { color: string; text: string }> = {
  PENDING: { color: 'processing', text: '待处理' },
  ACCEPTED: { color: 'success', text: '已接受' },
  REJECTED: { color: 'error', text: '已拒绝' },
  COMPLETED: { color: 'default', text: '已完成' },
  CANCELED: { color: 'default', text: '已取消' },
}

/**
 * 转诊管理页面
 * _Requirements: 7.1, 7.2_
 */
const ReferralPage = () => {
  const [referrals, setReferrals] = useState<Referral[]>([])
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [loading, setLoading] = useState(true)
  const [modalVisible, setModalVisible] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [statusFilter, setStatusFilter] = useState<ReferralStatus | undefined>()
  const [form] = Form.useForm()

  useEffect(() => {
    fetchData()
  }, [page, pageSize, statusFilter])

  useEffect(() => {
    fetchDoctors()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    try {
      const result = await referralService.getList({ page, pageSize, status: statusFilter })
      setReferrals(result.list)
      setTotal(result.total)
    } catch {
      message.error('获取数据失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchDoctors = async () => {
    try {
      const res = await get<any>('/doctors/experts')
      // 兼容分页响应格式，确保返回数组
      let doctorList: Doctor[] = []
      if (res?.data) {
        if (Array.isArray(res.data.records)) {
          doctorList = res.data.records
        } else if (Array.isArray(res.data)) {
          doctorList = res.data
        }
      }
      setDoctors(doctorList)
    } catch {
      setDoctors([])
    }
  }

  const handleCreate = async (values: Record<string, unknown>) => {
    setSubmitting(true)
    try {
      await referralService.create({
        consultationId: values.consultationId as string,
        toDoctorId: values.toDoctorId as string,
        summary: values.medicalSummary as string,
        description: values.reason as string,
      })
      message.success('转诊申请已发送')
      setModalVisible(false)
      form.resetFields()
      fetchData()
    } catch (err) {
      message.error((err as Error).message || '提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleAccept = async (id: number | string) => {
    try {
      await referralService.accept(id)
      message.success('已接受转诊')
      fetchData()
    } catch (err) {
      message.error((err as Error).message || '操作失败')
    }
  }

  const handleReject = async (id: number | string) => {
    try {
      await referralService.reject(id, '拒绝转诊')
      message.success('已拒绝转诊')
      fetchData()
    } catch (err) {
      message.error((err as Error).message || '操作失败')
    }
  }

  const columns: ColumnsType<Referral> = [
    { title: '转诊编号', dataIndex: 'id', key: 'id', width: 100 },
    { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      width: 100,
      render: (status: ReferralStatus) => {
        const config = statusMap[status] || { color: 'default', text: status }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    { 
      title: '创建时间', 
      dataIndex: 'createdAt', 
      key: 'createdAt',
      width: 180,
      render: (text) => formatDateTime(text)
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space>
          {record.status === 'PENDING' && (
            <>
              <Button type="link" size="small" onClick={() => handleAccept(record.id)}>接受</Button>
              <Button type="link" size="small" danger onClick={() => handleReject(record.id)}>拒绝</Button>
            </>
          )}
        </Space>
      )
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="转诊管理"
        breadcrumbs={[{ title: '工作台', href: '/doctor' }, { title: '转诊管理' }]}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
            发起转诊
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
              value={statusFilter}
              onChange={setStatusFilter}
              options={[
                { value: 'PENDING', label: '待处理' },
                { value: 'ACCEPTED', label: '已接受' },
                { value: 'REJECTED', label: '已拒绝' },
                { value: 'COMPLETED', label: '已完成' },
              ]}
            />
          </Space>
        </div>
        
        <Table 
          columns={columns} 
          dataSource={referrals} 
          rowKey="id" 
          loading={loading}
          pagination={{
            current: page,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
            onChange: (p, ps) => {
              setPage(p)
              setPageSize(ps)
            },
          }}
        />
      </Card>

      <Modal
        title="发起转诊"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="consultationId" label="问诊编号" rules={[{ required: true, message: '请输入问诊编号' }]}>
            <Input placeholder="请输入要转诊的问诊编号" />
          </Form.Item>
          <Form.Item name="toDoctorId" label="转诊至" rules={[{ required: true, message: '请选择接诊专家' }]}>
            <Select
              placeholder="选择接诊专家"
              options={doctors.map(d => ({ 
                value: d.id, 
                label: `${d.realName} - ${d.title} (${d.departmentName})` 
              }))}
            />
          </Form.Item>
          <Form.Item name="reason" label="转诊原因">
            <TextArea rows={3} placeholder="请说明转诊原因..." />
          </Form.Item>
          <Form.Item name="medicalSummary" label="病历摘要" rules={[{ required: true, message: '请提供病历摘要' }]}>
            <TextArea rows={4} placeholder="请提供病历摘要..." />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<SendOutlined />} loading={submitting} block>
              提交转诊申请
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ReferralPage
