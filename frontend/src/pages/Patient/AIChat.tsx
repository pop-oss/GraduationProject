import { useState, useRef, useEffect } from 'react'
import { Card, Input, Button, List, Avatar, Spin, message, Alert } from 'antd'
import { SendOutlined, RobotOutlined, UserOutlined } from '@ant-design/icons'
import api from '@/services/api'

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
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
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    initSession()
  }, [])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const initSession = async () => {
    try {
      const res = await api.post('/ai/sessions')
      setSessionId(res.data.data?.id)
    } catch {
      // 使用临时 session
      setSessionId(Date.now())
    }
  }

  const handleSend = async () => {
    if (!inputValue.trim() || loading) return

    const userMessage: Message = {
      id: Date.now(),
      role: 'user',
      content: inputValue,
      createdAt: new Date().toISOString()
    }
    
    setMessages(prev => [...prev, userMessage])
    setInputValue('')
    setLoading(true)

    try {
      const res = await api.post('/ai/chat', {
        sessionId,
        message: inputValue
      })
      
      const assistantMessage: Message = {
        id: Date.now() + 1,
        role: 'assistant',
        content: res.data.data?.reply || '抱歉，我暂时无法回答这个问题。',
        createdAt: new Date().toISOString()
      }
      
      setMessages(prev => [...prev, assistantMessage])
    } catch {
      message.error('发送失败，请重试')
      // 移除用户消息
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
        style={{ flex: 1, display: 'flex', flexDirection: 'column' }}
        bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}
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
    </div>
  )
}

export default AIChat
