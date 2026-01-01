import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Button, Typography, Space, message, Spin } from 'antd'
import { 
  UserOutlined, 
  CheckCircleOutlined, 
  ClockCircleOutlined,
  FileTextOutlined 
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/useAuthStore'
import { consultationService } from '@/services/consultation'
import DataTable from '@/components/DataTable'
import ConsultationStatusTag from '@/components/ConsultationStatusTag'
import { formatDateTime } from '@/utils/date'
import type { ConsultationDetail } from '@/types'
import type { ColumnsType } from 'antd/es/table'

const { Title } = Typography

/**
 * 医生工作台
 * _Requirements: 6.1, 6.2_
 */
const DoctorWorkbench = () => {
  const navigate = useNavigate()
  const { user } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({ waiting: 0, inProgress: 0, finished: 0 })
  const [waitingList, setWaitingList] = useState<ConsultationDetail[]>([])
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [accepting, setAccepting] = useState<string | number | null>(null)

  const loadData = async () => {
    setLoading(true)
    try {
      const [statsData, listData] = await Promise.all([
        consultationService.getTodayStats(),
        consultationService.getWaitingList({ page, pageSize }),
      ])
      setStats(statsData)
      setWaitingList(listData.list)
      setTotal(listData.total)
    } catch (err) {
      message.error((err as Error).message || '加载失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [page, pageSize])

  const handleAccept = async (id: string | number) => {
    setAccepting(id)
    try {
      await consultationService.accept(id)
      message.success('接诊成功')
      navigate(`/doctor/consultation/${id}`)
    } catch (err) {
      message.error((err as Error).message || '接诊失败')
    } finally {
      setAccepting(null)
    }
  }

  const columns: ColumnsType<ConsultationDetail> = [
    {
      title: '问诊编号',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '患者',
      dataIndex: ['patient', 'name'],
      key: 'patientName',
      render: (_, record) => record.patient?.name || '-',
    },
    {
      title: '症状描述',
      dataIndex: 'symptoms',
      key: 'symptoms',
      ellipsis: true,
    },
    {
      title: '预约时间',
      dataIndex: 'scheduledAt',
      key: 'scheduledAt',
      render: (text) => formatDateTime(text),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => <ConsultationStatusTag status={status} />,
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/doctor/consultation/${record.id}`)}>
            查看
          </Button>
          {record.status === 'WAITING' && (
            <Button 
              type="primary" 
              size="small"
              loading={accepting === record.id}
              onClick={() => handleAccept(record.id)}
            >
              接诊
            </Button>
          )}
        </Space>
      ),
    },
  ]

  if (loading && waitingList.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div style={{ padding: 24 }}>
      <Title level={4}>医生工作台 - {user?.realName}</Title>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic 
              title="今日问诊" 
              value={stats.waiting + stats.inProgress + stats.finished} 
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="已完成" 
              value={stats.finished} 
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="待接诊" 
              value={stats.waiting} 
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="进行中" 
              value={stats.inProgress} 
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="待接诊列表">
        <DataTable
          columns={columns}
          dataSource={waitingList}
          rowKey="id"
          loading={loading}
          page={page}
          pageSize={pageSize}
          total={total}
          onPageChange={(p, ps) => {
            setPage(p)
            setPageSize(ps)
          }}
          emptyText="暂无待接诊患者"
        />
      </Card>
    </div>
  )
}

export default DoctorWorkbench
