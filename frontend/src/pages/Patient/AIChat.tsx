import { useState, useRef, useEffect } from 'react'
import { Card, Input, Button, List, Avatar, Spin, message, Alert, Drawer, Empty, Typography } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined, HistoryOutlined, PlusOutlined, MessageOutlined } from '@ant-design/icons'
import api from '@/services/api'
import dayjs from 'dayjs'

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

interface Session {
  id: number
  sessionType: string
  status: string
  title?: string
  createdAt: string
  updatedAt: string
}

/**
 * AI 健康问答页面
 * _Requirements: 10.1_
 */
const AIChat = () => {
  const [messages, setMessages] = useState<Message[]>([])
  const [inputValue, setInputValue] = useState('')
  const [loading, setLoading] = useState(false)
  const [sessionId, setSessionId] = useState<number | null>(null)
  const [sessions, setSessions] = useState<Session[]>([])
  const [historyVisible, setHistoryVisible] = useState(false)
  const [loadingHistory, setLoadingHistory] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    initSession()
    loadSessions()
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // 加载历史会话列表
  const loadSessions = async () => {
    try {
      const res = await api.get('/ai/session/my')
      setSessions(res.data.data || [])
    } catch {
      console.error('加载历史会话失败')
    }
  }

  // 初始化新会话
  const initSession = async () => {
    try {
      const res = await api.post('/ai/session')
      setSessionId(res.data.data?.id)
      setMessages([])
    } catch {
      setSessionId(Date.now())
    }
  }

  // 创建新会话
  const createNewSession = async () => {
    await initSession()
    await loadSessions()
    message.success('已创建新会话')
  }

  // 加载历史会话消息
  const loadSessionMessages = async (sid: number) => {
    setLoadingHistory(true)
    try {
      const res = await api.get(`/ai/session/${sid}/messages`)
      const msgs = (res.data.data || []).map((m: any) => ({
        id: m.id,
        role: m.role === 'USER' ? 'user' : 'assistant',
        content: m.content,
        createdAt: m.createdAt
      }))
      setMessages(msgs)
      setSessionId(sid)
      setHistoryVisible(false)
    } catch {
      message.error('加载历史消息失败')
    } finally {
      setLoadingHistory(false)
    }
  }

  const handleSend = async () => {
    if (!inputValue.trim() || loading || !sessionId) return

    const userMessage: Message = {
      id: Date.now(),
      role: 'user',
      content: inputValue,
      createdAt: new Date().toISOString()
    }
    
    setMessages(prev => [...prev, userMessage])
    const question = inputValue
    setInputValue('')
    setLoading(true)

    try {
      const res = await api.post(`/ai/session/${sessionId}/chat`, { question })
      
      const assistantMessage: Message = {
        id: Date.now() + 1,
        role: 'assistant',
        content: res.data.data?.answer || '抱歉，我暂时无法回答这个问题。',
        createdAt: new Date().toISOString()
      }
      
      setMessages(prev => [...prev, assistantMessage])
      loadSessions() // 刷新会话列表
    } catch {
      message.error('发送失败，请重试')
      setMessages(prev => prev.filter(m => m.id !== userMessage.id))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ padding: 24, height: 'calc(100vh - 64px)', display: 'flex', flexDirection: 'column' }}>
      <Alert
        message="健康提示"
        description="AI 健康问答仅供参考，不能替代专业医生的诊断和治疗建议。如有不适，请及时就医。"
        type="warning"
        showIcon
        style={{ marginBottom: 16 }}
      />
      
      <Card 
        title={<><RobotOutlined /> AI 健康助手</>}
        extra={
          <div>
            <Button icon={<PlusOutlined />} onClick={createNewSession} style={{ marginRight: 8 }}>
              新会话
            </Button>
            <Button icon={<HistoryOutlined />} onClick={() => setHistoryVisible(true)}>
              历史记录
            </Button>
          </div>
        }
        style={{ flex: 1, display: 'flex', flexDirection: 'column' }}
        styles={{ body: { flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' } }}
      >
        <div style={{ flex: 1, overflow: 'auto', marginBottom: 16 }}>
          {messages.length === 0 ? (
            <div style={{ textAlign: 'center', color: '#999', padding: 40 }}>
              <RobotOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <p>您好！我是耳康云诊 AI 健康助手。</p>
              <p>您可以向我咨询耳鼻喉相关的健康问题。</p>
            </div>
          ) : (
            <List
              dataSource={messages}
              renderItem={(msg) => (
                <List.Item style={{ 
                  justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
                  border: 'none',
                  padding: '8px 0'
                }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', maxWidth: '80%' }}>
                    {msg.role === 'assistant' && (
                      <Avatar icon={<RobotOutlined />} style={{ marginRight: 8, background: '#1890ff' }} />
                    )}
                    <div style={{ 
                      background: msg.role === 'user' ? '#1890ff' : '#f0f0f0',
                      color: msg.role === 'user' ? '#fff' : '#000',
                      padding: '12px 16px',
                      borderRadius: 12,
                      whiteSpace: 'pre-wrap'
                    }}>
                      {msg.content}
                    </div>
                    {msg.role === 'user' && (
                      <Avatar icon={<UserOutlined />} style={{ marginLeft: 8 }} />
                    )}
                  </div>
                </List.Item>
              )}
            />
          )}
          {loading && (
            <div style={{ textAlign: 'center', padding: 16 }}>
              <Spin tip="AI 正在思考..." />
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
        
        <Input.Group compact style={{ display: 'flex' }}>
          <Input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onPressEnter={handleSend}
            placeholder="请输入您的健康问题..."
            style={{ flex: 1 }}
            disabled={loading}
          />
          <Button 
            type="primary" 
            icon={<SendOutlined />} 
            onClick={handleSend}
            loading={loading}
          >
            发送
          </Button>
        </Input.Group>
      </Card>

      {/* 历史会话抽屉 */}
      <Drawer
        title="历史会话"
        placement="right"
        onClose={() => setHistoryVisible(false)}
        open={historyVisible}
        width={360}
      >
        <Spin spinning={loadingHistory}>
          {sessions.length === 0 ? (
            <Empty description="暂无历史会话" />
          ) : (
            <List
              dataSource={sessions}
              renderItem={(session) => (
                <List.Item
                  style={{ 
                    cursor: 'pointer',
                    background: session.id === sessionId ? '#e6f7ff' : 'transparent',
                    borderRadius: 8,
                    marginBottom: 8,
                    padding: '12px'
                  }}
                  onClick={() => loadSessionMessages(session.id)}
                >
                  <List.Item.Meta
                    avatar={<Avatar icon={<MessageOutlined />} style={{ background: '#1890ff' }} />}
                    title={session.title || '新会话'}
                    description={
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {dayjs(session.updatedAt || session.createdAt).format('YYYY-MM-DD HH:mm')}
                      </Typography.Text>
                    }
                  />
                </List.Item>
              )}
            />
          )}
        </Spin>
      </Drawer>
    </div>
  )
}

export default AIChat
