import { useState, useEffect } from 'react'
import { Card, Table, Tag, Button, DatePicker, Select, Space, message } from 'antd'
import { EyeOutlined, SearchOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import PageHeader from '@/components/PageHeader'
import { reviewService, type ReviewPrescription, type ReviewStatus, type RiskLevel } from '@/services/review'
import { formatDateTime } from '@/utils/date'

const { RangePicker } = DatePicker

const riskColors: Record<RiskLevel, string> = {
  LOW: 'green',
  MEDIUM: 'orange',
  HIGH: 'red',
}

const riskLabels: Record<RiskLevel, string> = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
}

/**
 * 审方历史页面
 * _Requirements: 8.5_
 */
const ReviewHistory = () => {
  const navigate = useNavigate()
  const [prescriptions, setPrescriptions] = useState<ReviewPrescription[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [filters, setFilters] = useState<{
    status?: ReviewStatus
    dateRange?: [Dayjs, Dayjs] | null
  }>({})

  // 用于触发数据刷新
  const [fetchTrigger, setFetchTrigger] = useState(0)
  // 用于存储实际请求时使用的筛选条件
  const [appliedFilters, setAppliedFilters] = useState<{
    status?: ReviewStatus
    dateRange?: [Dayjs, Dayjs] | null
  }>({})

  useEffect(() => {
    const fetchHistory = async () => {
      setLoading(true)
      try {
        const query: Record<string, unknown> = { page, pageSize }
        if (appliedFilters.status) query.status = appliedFilters.status
        if (appliedFilters.dateRange) {
          query.startDate = appliedFilters.dateRange[0].format('YYYY-MM-DD')
          query.endDate = appliedFilters.dateRange[1].format('YYYY-MM-DD')
        }
        const result = await reviewService.getHistory(query)
        setPrescriptions(result.list)
        setTotal(result.total)
      } catch {
        message.error('获取历史记录失败')
      } finally {
        setLoading(false)
      }
    }
    fetchHistory()
  }, [page, pageSize, fetchTrigger, appliedFilters])

  const handleSearch = () => {
    // 应用当前筛选条件
    setAppliedFilters({ ...filters })
    setPage(1)
    setFetchTrigger(prev => prev + 1)
  }

  const handleReset = () => {
    setFilters({})
    setAppliedFilters({})
    setPage(1)
    setFetchTrigger(prev => prev + 1)
  }

  const columns: ColumnsType<ReviewPrescription> = [
    { title: '处方编号', dataIndex: 'prescriptionNo', key: 'prescriptionNo', width: 150 },
    { title: '患者', dataIndex: 'patientName', key: 'patientName', width: 100 },
    { title: '开方医生', dataIndex: 'doctorName', key: 'doctorName', width: 100 },
    { title: '药品数量', dataIndex: 'drugCount', key: 'drugCount', width: 80, align: 'center' },
    { 
      title: '开方时间', 
      dataIndex: 'createdAt', 
      key: 'createdAt',
      width: 170,
      render: (text) => formatDateTime(text)
    },
    { 
      title: '风险等级', 
      dataIndex: 'riskLevel', 
      key: 'riskLevel',
      width: 100,
      render: (level: RiskLevel) => (
        <Tag color={riskColors[level]}>{riskLabels[level] || level}</Tag>
      )
    },
    { 
      title: '审核状态', 
      dataIndex: 'reviewResult', 
      key: 'reviewResult',
      width: 100,
      render: (result: string) => {
        const colors: Record<string, string> = {
          APPROVED: 'success',
          REJECTED: 'error',
        }
        const labels: Record<string, string> = {
          APPROVED: '已通过',
          REJECTED: '已驳回',
        }
        return <Tag color={colors[result] || 'default'}>{labels[result] || result}</Tag>
      }
    },
    {
      title: '操作',
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Button 
          type="link" 
          icon={<EyeOutlined />} 
          onClick={() => navigate(`/pharmacist/review/${record.id}`)}
        >
          查看
        </Button>
      )
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="审方历史"
        breadcrumbs={[
          { title: '药师工作台', href: '/pharmacist' },
          { title: '审方历史' }
        ]}
        extra={
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/pharmacist')}>
            返回待审列表
          </Button>
        }
      />
      
      <Card>
        <Space style={{ marginBottom: 16 }} wrap>
          <Select
            placeholder="审核状态"
            value={filters.status}
            onChange={(v) => setFilters({ ...filters, status: v })}
            style={{ width: 120 }}
            allowClear
            options={[
              { value: 'APPROVED', label: '已通过' },
              { value: 'REJECTED', label: '已驳回' },
            ]}
          />
          <RangePicker
            value={filters.dateRange}
            onChange={(v) => setFilters({ ...filters, dateRange: v as [Dayjs, Dayjs] | null })}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>

        <Table 
          columns={columns}
          dataSource={prescriptions} 
          rowKey="id"
          loading={loading}
          pagination={{
            current: page,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (p, ps) => {
              setPage(p)
              setPageSize(ps)
            },
          }}
        />
      </Card>
    </div>
  )
}

export default ReviewHistory
