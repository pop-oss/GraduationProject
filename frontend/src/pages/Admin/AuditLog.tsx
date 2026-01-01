import { useState, useEffect } from 'react'
import { Card, Table, Input, Select, DatePicker, Button, Tag, Space, message } from 'antd'
import { SearchOutlined, ExportOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import dayjs from 'dayjs'
import api from '@/services/api'

const { RangePicker } = DatePicker

interface AuditLog {
  id: number
  userId: number
  username: string
  action: string
  module: string
  targetId: string
  detail: string
  ip: string
  createdAt: string
}

/**
 * 审计日志页面
 * _Requirements: 13.4_
 */
const AuditLogPage = () => {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [filters, setFilters] = useState({ username: '', module: '', action: '', dateRange: null as any })

  useEffect(() => { fetchLogs() }, [page])

  const fetchLogs = async () => {
    setLoading(true)
    try {
      const params: any = { page, size: 20 }
      if (filters.username) params.username = filters.username
      if (filters.module) params.module = filters.module
      if (filters.action) params.action = filters.action
      if (filters.dateRange) {
        params.startDate = filters.dateRange[0].format('YYYY-MM-DD')
        params.endDate = filters.dateRange[1].format('YYYY-MM-DD')
      }
      const res = await api.get('/audit-logs', { params })
      setLogs(res.data.data?.records || [])
      setTotal(res.data.data?.total || 0)
    } catch { message.error('获取日志失败') }
    finally { setLoading(false) }
  }

  const handleSearch = () => { setPage(1); fetchLogs() }

  const handleExport = async () => {
    try {
      const res = await api.get('/audit-logs/export', { responseType: 'blob' })
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `audit-log-${dayjs().format('YYYYMMDD')}.xlsx`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch { message.error('导出失败') }
  }

  const actionColors: Record<string, string> = {
    CREATE: 'green', UPDATE: 'blue', DELETE: 'red', LOGIN: 'cyan', LOGOUT: 'default', VIEW: 'purple'
  }

  const columns: ColumnsType<AuditLog> = [
    { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 180,
      render: (text) => new Date(text).toLocaleString() },
    { title: '用户', dataIndex: 'username', key: 'username', width: 100 },
    { title: '模块', dataIndex: 'module', key: 'module', width: 100 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 80,
      render: (action) => <Tag color={actionColors[action] || 'default'}>{action}</Tag> },
    { title: '目标ID', dataIndex: 'targetId', key: 'targetId', width: 100 },
    { title: '详情', dataIndex: 'detail', key: 'detail', ellipsis: true },
    { title: 'IP', dataIndex: 'ip', key: 'ip', width: 120 },
  ]

  return (
    <div style={{ padding: 24 }}>
      <Card title="审计日志" extra={<Button icon={<ExportOutlined />} onClick={handleExport}>导出</Button>}>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input placeholder="用户名" value={filters.username} onChange={e => setFilters({...filters, username: e.target.value})} style={{ width: 120 }} />
          <Select placeholder="模块" value={filters.module || undefined} onChange={v => setFilters({...filters, module: v})} style={{ width: 120 }} allowClear
            options={[{ value: 'AUTH', label: '认证' }, { value: 'CONSULTATION', label: '问诊' }, { value: 'PRESCRIPTION', label: '处方' }, { value: 'REFERRAL', label: '转诊' }]} />
          <Select placeholder="操作" value={filters.action || undefined} onChange={v => setFilters({...filters, action: v})} style={{ width: 100 }} allowClear
            options={[{ value: 'CREATE', label: '创建' }, { value: 'UPDATE', label: '更新' }, { value: 'DELETE', label: '删除' }, { value: 'VIEW', label: '查看' }]} />
          <RangePicker value={filters.dateRange} onChange={v => setFilters({...filters, dateRange: v})} />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>搜索</Button>
        </Space>
        <Table columns={columns} dataSource={logs} rowKey="id" loading={loading}
          pagination={{ current: page, total, pageSize: 20, onChange: setPage }} />
      </Card>
    </div>
  )
}

export default AuditLogPage
