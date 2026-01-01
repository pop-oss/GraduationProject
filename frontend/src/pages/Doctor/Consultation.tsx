import { useState, useEffect, useRef } from 'react'
import { Card, Row, Col, Input, Button, List, Form, Tabs, Descriptions, message, Spin, Space, Timeline } from 'antd'
import { SendOutlined, SaveOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import VideoRoom from '@/components/VideoRoom'
import PageHeader from '@/components/PageHeader'
import ConsultationStatusTag from '@/components/ConsultationStatusTag'
import AttachmentList from '@/components/AttachmentList'
import { useWebSocket } from '@/hooks/useWebSocket'
import { consultationService } from '@/services/consultation'
import { medicalRecordService } from '@/services/medicalRecord'
import { useAuthStore } from '@/store/useAuthStore'
import { formatDateTime } from '@/utils/date'
import type { ConsultationDetail } from '@/types'

const { TextArea } = Input

interface ChatMessage {
  id: number
  senderId: number
  senderName: string
  content: string
  type: 'TEXT' | 'IMAGE' | 'FILE'
  createdAt: string
}

/**
 * 医生问诊详情页面
 * _Requirements: 6.5, 6.6, 6.7_
 */
const DoctorConsultation = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [inputValue, setInputValue] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState<ConsultationDetail | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const [form] = Form.useForm()
  
  const { sendMessage, lastMessage } = useWebSocket()

  useEffect(() => {
    fetchData()
  }, [id])

  useEffect(() => {
    if (lastMessage?.type === 'CHAT_MESSAGE') {
      const msgData = lastMessage.data as ChatMessage
      if (msgData) {
        setMessages(prev => [...prev, msgData])
      }
    }
  }, [lastMessage, id])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const fetchData = async () => {
    if (!id) return
    try {
      const consultData = await consultationService.getDetail(id)
      setDetail(consultData)
      
      // 加载病历数据（如果存在）
      const record = await medicalRecordService.getByConsultation(id)
      if (record) {
        form.setFieldsValue(record)
      }
    } catch {
      message.error('获取问诊信息失败')
    } finally {
      setLoading(false)
    }
  }

  const handleSend = () => {
    if (!inputValue.trim()) return
    
    sendMessage({
      type: 'CHAT_MESSAGE',
      consultationId: Number(id),
      content: inputValue,
      messageType: 'TEXT'
    })
    
    setMessages(prev => [...prev, {
      id: Date.now(),
      senderId: Number(user?.id) || 0,
      senderName: user?.realName || '',
      content: inputValue,
      type: 'TEXT',
      createdAt: new Date().toISOString()
    }])
    
    setInputValue('')
  }

  const handleSaveMedicalRecord = async (values: Record<string, unknown>) => {
    if (!id) return
    setSaving(true)
    try {
      await medicalRecordService.save({
        consultationId: id,
        ...values,
      })
      message.success('病历保存成功')
    } catch {
      message.error('保存失败')
    } finally {
      setSaving(false)
    }
  }

  const handleEndConsultation = async () => {
    if (!id) return
    try {
      await consultationService.finish(id)
      message.success('问诊已结束')
      navigate('/doctor')
    } catch {
      message.error('操作失败')
    }
  }

  if (loading) {
    return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  }

  if (!detail) {
    return <div style={{ textAlign: 'center', padding: 100 }}>问诊不存在</div>
  }

  const tabItems = [
    {
      key: 'video',
      label: '视频问诊',
      children: (
        <VideoRoom consultationId={Number(id)} />
      )
    },
    {
      key: 'record',
      label: '病历填写',
      children: (
        <Form form={form} layout="vertical" onFinish={handleSaveMedicalRecord}>
          <Form.Item name="chiefComplaint" label="主诉" rules={[{ required: true, message: '请填写主诉' }]}>
            <TextArea rows={2} placeholder="患者主要症状和持续时间" />
          </Form.Item>
          <Form.Item name="presentIllness" label="现病史" rules={[{ required: true, message: '请填写现病史' }]}>
            <TextArea rows={3} placeholder="疾病发生、发展过程" />
          </Form.Item>
          <Form.Item name="pastHistory" label="既往史">
            <TextArea rows={2} placeholder="既往疾病、手术、过敏史等" />
          </Form.Item>
          <Form.Item name="physicalExam" label="体格检查">
            <TextArea rows={2} placeholder="检查结果" />
          </Form.Item>
          <Form.Item name="diagnosis" label="诊断" rules={[{ required: true, message: '请填写诊断' }]}>
            <TextArea rows={2} placeholder="诊断结论" />
          </Form.Item>
          <Form.Item name="treatment" label="治疗方案" rules={[{ required: true, message: '请填写治疗方案' }]}>
            <TextArea rows={3} placeholder="治疗建议和方案" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />} loading={saving}>
                保存病历
              </Button>
              <Button onClick={() => navigate(`/doctor/prescription/${id}`)}>
                开具处方
              </Button>
              <Button danger onClick={handleEndConsultation}>
                结束问诊
              </Button>
            </Space>
          </Form.Item>
        </Form>
      )
    }
  ]

  return (
    <div style={{ padding: 24, height: 'calc(100vh - 64px)' }}>
      <PageHeader
        title={`问诊详情 #${id}`}
        breadcrumbs={[
          { title: '工作台', href: '/doctor' },
          { title: '问诊详情' },
        ]}
        extra={<ConsultationStatusTag status={detail.status} />}
      />
      
      <Row gutter={16} style={{ height: 'calc(100% - 80px)' }}>
        <Col xs={24} md={16}>
          <Card style={{ height: '100%' }}>
            <Tabs items={tabItems} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          {/* 患者信息 */}
          <Card title="患者信息" size="small" style={{ marginBottom: 16 }}>
            {detail.patient && (
              <Descriptions column={1} size="small">
                <Descriptions.Item label="姓名">{detail.patient.name}</Descriptions.Item>
                <Descriptions.Item label="性别">
                  {detail.patient.gender === 'MALE' ? '男' : detail.patient.gender === 'FEMALE' ? '女' : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="年龄">{detail.patient.age ? `${detail.patient.age}岁` : '-'}</Descriptions.Item>
                {detail.patient.phoneMasked && (
                  <Descriptions.Item label="手机号">{detail.patient.phoneMasked}</Descriptions.Item>
                )}
              </Descriptions>
            )}
          </Card>

          {/* 症状描述 */}
          <Card title="症状描述" size="small" style={{ marginBottom: 16 }}>
            <p style={{ margin: 0 }}>{detail.symptoms || '无'}</p>
          </Card>

          {/* 附件 */}
          {detail.attachments && detail.attachments.length > 0 && (
            <Card title="附件" size="small" style={{ marginBottom: 16 }}>
              <AttachmentList attachments={detail.attachments} showPreview showDownload />
            </Card>
          )}

          {/* 状态时间线 */}
          {detail.timeline && detail.timeline.length > 0 && (
            <Card title="时间线" size="small" style={{ marginBottom: 16 }}>
              <Timeline
                items={detail.timeline.map((item) => ({
                  children: (
                    <div>
                      <div style={{ fontSize: 13 }}>{item.title}</div>
                      <div style={{ color: '#999', fontSize: 12 }}>{formatDateTime(item.time)}</div>
                    </div>
                  ),
                }))}
              />
            </Card>
          )}

          {/* 消息 */}
          <Card 
            title="消息" 
            size="small"
            style={{ height: 300 }}
            styles={{ body: { height: 'calc(100% - 40px)', display: 'flex', flexDirection: 'column' } }}
          >
            <div style={{ flex: 1, overflow: 'auto', marginBottom: 8 }}>
              <List
                size="small"
                dataSource={messages}
                renderItem={(msg) => (
                  <List.Item style={{ 
                    justifyContent: msg.senderId === user?.id ? 'flex-end' : 'flex-start',
                    border: 'none', padding: '4px 0'
                  }}>
                    <div style={{ 
                      maxWidth: '85%',
                      background: msg.senderId === user?.id ? '#1890ff' : '#f0f0f0',
                      color: msg.senderId === user?.id ? '#fff' : '#000',
                      padding: '6px 10px', borderRadius: 6, fontSize: 13
                    }}>
                      {msg.content}
                    </div>
                  </List.Item>
                )}
              />
              <div ref={messagesEndRef} />
            </div>
            <Space.Compact style={{ display: 'flex' }}>
              <Input
                size="small"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onPressEnter={handleSend}
                placeholder="输入消息..."
                style={{ flex: 1 }}
              />
              <Button size="small" type="primary" icon={<SendOutlined />} onClick={handleSend} />
            </Space.Compact>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default DoctorConsultation
