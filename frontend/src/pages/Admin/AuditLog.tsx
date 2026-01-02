import { useState, useEffect } from 'react'
import { Card, Table, Input, Select, DatePicker, Button, Tag, Space, message } from 'antd'
import { SearchOutlined, ExportOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader'
import { auditService, type AuditLog, type AuditAction, type AuditModule } from '@/services/audit'
import { formatDateTime } from '@/utils/date'

const { RangePicker } = DatePicker

const actionColors: Record<string, string> = {
  CREATE: 'green',
  UPDATE: 'blue',
  DELETE: 'red',
  VIEW: 'purple',
  LOGIN: 'cyan',
  LOGOUT: 'default',
  EXPORT: 'orange',
  VIEW_STATS: 'purple',
  VIEW_RECORD: 'purple',
  REVIEW_PRESCRIPTION: 'blue',
  CREATE_PRESCRIPTION: 'green',
  REJECT_PRESCRIPTION: 'red',
  APPROVE_PRESCRIPTION: 'green',
  EXPORT_STATS: 'orange',
  CREATE_MDT: 'green',
  JOIN_MDT: 'blue',
  CREATE_REFERRAL: 'green',
  UPDATE_REFERRAL: 'blue',
  CREATE_CONSULTATION: 'green',
  UPDATE_CONSULTATION: 'blue',
  CREATE_FOLLOWUP: 'green',
  START_CONSULTATION: 'green',
  ACCEPT_CONSULTATION: 'blue',
  FINISH_CONSULTATION: 'purple',
}

const actionLabels: Record<string, string> = {
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  VIEW: '查看',
  LOGIN: '登录',
  LOGOUT: '登出',
  EXPORT: '导出',
  VIEW_STATS: '查看统计',
  VIEW_RECORD: '查看病历',
  REVIEW_PRESCRIPTION: '审核处方',
  CREATE_PRESCRIPTION: '创建处方',
  REJECT_PRESCRIPTION: '驳回处方',
  APPROVE_PRESCRIPTION: '通过处方',
  EXPORT_STATS: '导出统计',
  CREATE_MDT: '创建会诊',
  JOIN_MDT: '参与会诊',
  CREATE_REFERRAL: '创建转诊',
  UPDATE_REFERRAL: '更新转诊',
  CREATE_CONSULTATION: '创建问诊',
  UPDATE_CONSULTATION: '更新问诊',
  CREATE_FOLLOWUP: '创建随访',
  START_CONSULTATION: '开始问诊',
  ACCEPT_CONSULTATION: '接受问诊',
  FINISH_CONSULTATION: '结束问诊',
}

// 用于显示的标签映射（包含所有可能的值）
const moduleLabels: Record<string, string> = {
  AUTH: '认证',
  USER: '用户',
  CONSULTATION: '问诊',
  PRESCRIPTION: '处方',
  REFERRAL: '转诊',
  MDT: '会诊',
  REVIEW: '审方',
  auth: '认证',
  user: '用户',
  consultation: '问诊',
  prescription: '处方',
  referral: '转诊',
  mdt: '会诊',
  review: '审方',
  stats: '统计',
  medical_record: '病历',
  followup: '随访',
}

// 下拉框选项（去重，只保留实际使用的值）
const moduleOptions = [
  { value: 'CONSULTATION', label: '问诊' },
  { value: 'PRESCRIPTION', label: '处方' },
  { value: 'REFERRAL', label: '转诊' },
  { value: 'MDT', label: '会诊' },
  { value: 'REVIEW', label: '审方' },
  { value: 'AUTH', label: '认证' },
  { value: 'USER', label: '用户' },
  { value: 'stats', label: '统计' },
  { value: 'medical_record', label: '病历' },
  { value: 'followup', label: '随访' },
]

const actionOptions = [
  { value: 'CREATE_CONSULTATION', label: '创建问诊' },
  { value: 'START_CONSULTATION', label: '开始问诊' },
  { value: 'ACCEPT_CONSULTATION', label: '接受问诊' },
  { value: 'UPDATE_CONSULTATION', label: '更新问诊' },
  { value: 'FINISH_CONSULTATION', label: '结束问诊' },
  { value: 'CREATE_PRESCRIPTION', label: '创建处方' },
  { value: 'REVIEW_PRESCRIPTION', label: '审核处方' },
  { value: 'APPROVE_PRESCRIPTION', label: '通过处方' },
  { value: 'REJECT_PRESCRIPTION', label: '驳回处方' },
  { value: 'CREATE_REFERRAL', label: '创建转诊' },
  { value: 'UPDATE_REFERRAL', label: '更新转诊' },
  { value: 'CREATE_MDT', label: '创建会诊' },
  { value: 'JOIN_MDT', label: '参与会诊' },
  { value: 'CREATE_FOLLOWUP', label: '创建随访' },
  { value: 'VIEW_STATS', label: '查看统计' },
  { value: 'EXPORT_STATS', label: '导出统计' },
  { value: 'VIEW_RECORD', label: '查看病历' },
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'CREATE', label: '创建' },
  { value: 'UPDATE', label: '更新' },
  { value: 'DELETE', label: '删除' },
  { value: 'VIEW', label: '查看' },
  { value: 'EXPORT', label: '导出' },
]

/**
 * 审计日志页面
 * _Requirements: 9.3, 9.4_
 */
const AuditLogPage = () => {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(20)
  const [filters, setFilters] = useState<{
    realName?: string
    module?: AuditModule
    action?: AuditAction
    dateRange?: [Dayjs, Dayjs] | null
  }>({})
  const [exporting, setExporting] = useState(false)

  useEffect(() => { 
    fetchLogs() 
  }, [page, pageSize])

  const fetchLogs = async () => {
    setLoading(true)
    try {
      const query: Record<string, unknown> = { page, pageSize }
      if (filters.realName) query.realName = filters.realName
      if (filters.module) query.module = filters.module
      if (filters.action) query.action = filters.action
      if (filters.dateRange) {
        query.startDate = filters.dateRange[0].format('YYYY-MM-DD')
        query.endDate = filters.dateRange[1].format('YYYY-MM-DD')
      }
      const result = await auditService.getList(query)
      setLogs(result.list)
      setTotal(result.total)
    } catch { 
      message.error('获取日志失败') 
    } finally { 
      setLoading(false) 
    }
  }

  const handleSearch = () => { 
    setPage(1)
    fetchLogs() 
  }

  const handleReset = () => {
    setFilters({})
    setPage(1)
    fetchLogs()
  }

  const handleExport = async () => {
    setExporting(true)
    try {
      const query: Record<string, unknown> = {}
      if (filters.realName) query.realName = filters.realName
      if (filters.module) query.module = filters.module
      if (filters.action) query.action = filters.action
      if (filters.dateRange) {
        query.startDate = filters.dateRange[0].format('YYYY-MM-DD')
        query.endDate = filters.dateRange[1].format('YYYY-MM-DD')
      }
      const blob = await auditService.exportLogs(query)
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `audit-log-${dayjs().format('YYYYMMDD')}.xlsx`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      message.success('导出成功')
    } catch { 
      message.error('导出失败') 
    } finally {
      setExporting(false)
    }
  }


  const columns: ColumnsType<AuditLog> = [
    { 
      title: '时间', 
      dataIndex: 'createdAt', 
      key: 'createdAt', 
      width: 180,
      render: (text) => formatDateTime(text)
    },
    { 
      title: '用户', 
      dataIndex: 'username', 
      key: 'username', 
      width: 150,
      render: (username: string, record: AuditLog) => (
        <div>
          <div>{record.realName || username}</div>
          {record.phone && <div style={{ fontSize: 12, color: '#999' }}>{record.phone}</div>}
        </div>
      )
    },
    { 
      title: '模块', 
      dataIndex: 'module', 
      key: 'module', 
      width: 80,
      render: (module: string) => moduleLabels[module] || module
    },
    { 
      title: '操作', 
      dataIndex: 'action', 
      key: 'action', 
      width: 100,
      render: (action: string) => (
        <Tag color={actionColors[action] || 'default'}>
          {actionLabels[action] || action}
        </Tag>
      )
    },
    { title: '目标ID', dataIndex: 'targetId', key: 'targetId', width: 100 },
    { title: '详情', dataIndex: 'detail', key: 'detail', ellipsis: true },
    { title: 'IP', dataIndex: 'ip', key: 'ip', width: 130 },
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="审计日志"
        breadcrumbs={[{ title: '管理后台', href: '/admin' }, { title: '审计日志' }]}
        extra={
          <Button 
            icon={<ExportOutlined />} 
            onClick={handleExport}
            loading={exporting}
          >
            导出
          </Button>
        }
      />

      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Input 
            placeholder="真实姓名" 
            value={filters.realName} 
            onChange={e => setFilters({...filters, realName: e.target.value})} 
            style={{ width: 120 }} 
            allowClear
          />
          <Select 
            placeholder="模块" 
            value={filters.module} 
            onChange={v => setFilters({...filters, module: v})} 
            style={{ width: 100 }} 
            allowClear
            options={moduleOptions}
          />
          <Select 
            placeholder="操作" 
            value={filters.action} 
            onChange={v => setFilters({...filters, action: v})} 
            style={{ width: 120 }} 
            allowClear
            options={actionOptions}
          />
          <RangePicker 
            value={filters.dateRange} 
            onChange={v => setFilters({...filters, dateRange: v as [Dayjs, Dayjs] | null})} 
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>

        <Table 
          columns={columns} 
          dataSource={logs} 
          rowKey="id" 
          loading={loading}
          pagination={{ 
            current: page, 
            total, 
            pageSize,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, ps) => {
              setPage(p)
              setPageSize(ps)
            }
          }}
          scroll={{ x: 900 }}
        />
      </Card>
    </div>
  )
}

export default AuditLogPage
