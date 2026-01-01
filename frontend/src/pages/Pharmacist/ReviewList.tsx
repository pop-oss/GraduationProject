import { useState, useEffect } from 'react'
import { Card, Table, Tag, Button, Space, message, Row, Col, Statistic } from 'antd'
import { EyeOutlined, CheckOutlined, CloseOutlined, FileTextOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import { reviewService, type ReviewPrescription, type RiskLevel } from '@/services/review'
import { formatDateTime } from '@/utils/date'

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
 * 药师待审列表
 * _Requirements: 8.1_
 */
const PharmacistReviewList = () => {
  const navigate = useNavigate()
  const [prescriptions, setPrescriptions] = useState<ReviewPrescription[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [stats, setStats] = useState({ pending: 0, approvedToday: 0, rejectedToday: 0 })
  const [actionLoading, setActionLoading] = useState<number | null>(null)

  useEffect(() => {
    fetchList()
    fetchStats()
  }, [page, pageSize])

  const fetchList = async () => {
    setLoading(true)
    try {
      const result = await reviewService.getPendingList({ page, pageSize })
      setPrescriptions(result.list)
      setTotal(result.total)
    } catch {
      message.error('获取待审列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchStats = async () => {
    try {
      const data = await reviewService.getStats()
      setStats(data)
    } catch {
      // ignore
    }
  }

  const handleQuickApprove = async (id: number) => {
    setActionLoading(id)
    try {
      await reviewService.approve(id)
      message.success('处方已通过')
      fetchList()
      fetchStats()
    } catch (err) {
      message.error((err as Error).message || '操作失败')
    } finally {
      setActionLoading(null)
    }
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
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button 
            type="link" 
            icon={<EyeOutlined />} 
            onClick={() => navigate(`/pharmacist/review/${record.id}`)}
          >
            查看
          </Button>
          <Button 
            type="link" 
            icon={<CheckOutlined />} 
            style={{ color: '#52c41a' }}
            loading={actionLoading === record.id}
            onClick={() => handleQuickApprove(record.id)}
          >
            通过
          </Button>
          <Button 
            type="link" 
            icon={<CloseOutlined />} 
            danger
            onClick={() => navigate(`/pharmacist/review/${record.id}?action=reject`)}
          >
            驳回
          </Button>
        </Space>
      )
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="待审处方"
        breadcrumbs={[{ title: '药师工作台' }, { title: '待审处方' }]}
        extra={
          <Button onClick={() => navigate('/pharmacist/history')}>审方历史</Button>
        }
      />

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Card>
            <Statistic 
              title="待审处方" 
              value={stats.pending} 
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="今日通过" 
              value={stats.approvedToday} 
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic 
              title="今日驳回" 
              value={stats.rejectedToday} 
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
      </Row>
      
      <Card>
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

export default PharmacistReviewList
