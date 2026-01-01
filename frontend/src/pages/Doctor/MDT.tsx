import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Form, Input, Select, Tag, Avatar, message, Tooltip, Descriptions } from 'antd'
import { PlusOutlined, UserOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import { mdtService, type MDT, type MDTDetail, type MDTStatus } from '@/services/mdt'
import { get } from '@/services/http'
import { formatDateTime } from '@/utils/date'

const { TextArea } = Input

interface Doctor {
  id: number
  realName: string
  title: string
  departmentName: string
}

const statusMap: Record<MDTStatus, { color: string; text: string }> = {
  PENDING: { color: 'default', text: '待开始' },
  IN_PROGRESS: { color: 'processing', text: '进行中' },
  COMPLETED: { color: 'success', text: '已完成' },
  CANCELED: { color: 'error', text: '已取消' },
}

/**
 * MDT 会诊管理页面
 * _Requirements: 7.3, 7.4, 7.5_
 */
const MDTPage = () => {
  const [cases, setCases] = useState<MDT[]>([])
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [loading, setLoading] = useState(true)
  const [modalVisible, setModalVisible] = useState(false)
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [conclusionModalVisible, setConclusionModalVisible] = useState(false)
  const [selectedMDT, setSelectedMDT] = useState<MDTDetail | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [form] = Form.useForm()
  const [conclusionForm] = Form.useForm()

  useEffect(() => { 
    fetchCases() 
  }, [page, pageSize])

  useEffect(() => {
    fetchDoctors()
  }, [])

  const fetchCases = async () => {
    setLoading(true)
    try {
      const result = await mdtService.getList({ page, pageSize })
      setCases(result.list)
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
      await mdtService.create({
        consultationId: values.consultationId as string,
        title: values.title as string,
        description: values.description as string,
        memberIds: values.expertIds as string[],
      })
      message.success('会诊已发起')
      setModalVisible(false)
      form.resetFields()
      fetchCases()
    } catch (err) { 
      message.error((err as Error).message || '提交失败') 
    } finally {
      setSubmitting(false)
    }
  }

  const handleViewDetail = async (id: string | number) => {
    try {
      const detail = await mdtService.getDetail(id)
      setSelectedMDT(detail)
      setDetailModalVisible(true)
    } catch (err) {
      message.error((err as Error).message || '获取详情失败')
    }
  }

  const handleArchive = async (values: Record<string, unknown>) => {
    if (!selectedMDT) return
    setSubmitting(true)
    try {
      await mdtService.archive(selectedMDT.id, values.conclusion as string)
      message.success('结论已归档')
      setConclusionModalVisible(false)
      setDetailModalVisible(false)
      conclusionForm.resetFields()
      fetchCases()
    } catch (err) {
      message.error((err as Error).message || '归档失败')
    } finally {
      setSubmitting(false)
    }
  }

  const columns: ColumnsType<MDT> = [
    { title: 'MDT编号', dataIndex: 'id', key: 'id', width: 100 },
    { title: '会诊主题', dataIndex: 'title', key: 'title', ellipsis: true },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      width: 100,
      render: (status: MDTStatus) => {
        const config = statusMap[status] || { color: 'default', text: status }
        return <Tag color={config.color}>{config.text}</Tag>
      }
    },
    { 
      title: '计划时间', 
      dataIndex: 'scheduledAt', 
      key: 'scheduledAt',
      width: 180,
      render: (text) => text ? formatDateTime(text) : '-'
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
      width: 100,
      render: (_, record) => (
        <Button type="link" onClick={() => handleViewDetail(record.id)}>查看详情</Button>
      )
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="MDT 多学科会诊"
        breadcrumbs={[{ title: '工作台', href: '/doctor' }, { title: 'MDT会诊' }]}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
            发起会诊
          </Button>
        }
      />
      
      <Card>
        <Table 
          columns={columns} 
          dataSource={cases} 
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

      {/* 创建 MDT 弹窗 */}
      <Modal title="发起 MDT 会诊" open={modalVisible} onCancel={() => setModalVisible(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="consultationId" label="问诊编号" rules={[{ required: true, message: '请输入问诊编号' }]}>
            <Input placeholder="请输入关联的问诊编号" />
          </Form.Item>
          <Form.Item name="title" label="会诊主题" rules={[{ required: true, message: '请输入会诊主题' }]}>
            <Input placeholder="请输入会诊主题" />
          </Form.Item>
          <Form.Item name="description" label="病情描述" rules={[{ required: true, message: '请描述病情' }]}>
            <TextArea rows={4} placeholder="请详细描述患者病情..." />
          </Form.Item>
          <Form.Item name="expertIds" label="邀请专家" rules={[{ required: true, message: '请选择参与专家' }]}>
            <Select 
              mode="multiple" 
              placeholder="选择参与会诊的专家"
              options={doctors.map(d => ({
                value: d.id,
                label: `${d.realName} - ${d.title} (${d.departmentName})`
              }))}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>提交</Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* MDT 详情弹窗 */}
      <Modal 
        title="MDT 会诊详情" 
        open={detailModalVisible} 
        onCancel={() => setDetailModalVisible(false)}
        footer={
          selectedMDT?.status === 'IN_PROGRESS' ? (
            <Button type="primary" onClick={() => setConclusionModalVisible(true)}>
              归档结论
            </Button>
          ) : null
        }
        width={600}
      >
        {selectedMDT && (
          <>
            <Descriptions column={2}>
              <Descriptions.Item label="会诊主题" span={2}>{selectedMDT.title}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusMap[selectedMDT.status]?.color}>
                  {statusMap[selectedMDT.status]?.text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="创建时间">{formatDateTime(selectedMDT.createdAt)}</Descriptions.Item>
              {selectedMDT.description && (
                <Descriptions.Item label="病情描述" span={2}>{selectedMDT.description}</Descriptions.Item>
              )}
              {selectedMDT.conclusion && (
                <Descriptions.Item label="会诊结论" span={2}>{selectedMDT.conclusion}</Descriptions.Item>
              )}
            </Descriptions>
            
            <div style={{ marginTop: 16 }}>
              <h4>参与成员</h4>
              <Avatar.Group>
                {selectedMDT.members?.map((m) => (
                  <Tooltip key={m.id} title={`${m.doctorName} (${m.role === 'ORGANIZER' ? '发起人' : '参与者'})`}>
                    <Avatar icon={<UserOutlined />} />
                  </Tooltip>
                ))}
              </Avatar.Group>
            </div>
          </>
        )}
      </Modal>

      {/* 归档结论弹窗 */}
      <Modal 
        title="归档会诊结论" 
        open={conclusionModalVisible} 
        onCancel={() => setConclusionModalVisible(false)}
        footer={null}
      >
        <Form form={conclusionForm} layout="vertical" onFinish={handleArchive}>
          <Form.Item 
            name="conclusion" 
            label="会诊结论" 
            rules={[{ required: true, message: '请输入会诊结论' }]}
          >
            <TextArea rows={6} placeholder="请输入会诊结论..." />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              确认归档
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default MDTPage
