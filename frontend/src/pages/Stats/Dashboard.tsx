import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic, Table, DatePicker, Space, Progress, message } from 'antd'
import { 
  TeamOutlined, 
  FileTextOutlined,
  MedicineBoxOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  UserOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import PageHeader from '@/components/PageHeader'
import { 
  statsService, 
  type OverviewStats, 
  type ConsultationTrend, 
  type DepartmentStats, 
  type DoctorRanking,
  type PrescriptionStats,
} from '@/services/stats'

const { RangePicker } = DatePicker

/**
 * 统计分析看板
 * _Requirements: 10.1, 10.2, 10.3_
 */
const StatsDashboard = () => {
  const [overview, setOverview] = useState<OverviewStats | null>(null)
  const [trend, setTrend] = useState<ConsultationTrend[]>([])
  const [departments, setDepartments] = useState<DepartmentStats[]>([])
  const [doctorRanking, setDoctorRanking] = useState<DoctorRanking[]>([])
  const [prescriptionStats, setPrescriptionStats] = useState<PrescriptionStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [dateRange, setDateRange] = useState<[Dayjs, Dayjs]>([
    dayjs().subtract(30, 'day'),
    dayjs(),
  ])

  useEffect(() => {
    fetchAllStats()
  }, [dateRange])

  const fetchAllStats = async () => {
    setLoading(true)
    const query = {
      startDate: dateRange[0].format('YYYY-MM-DD'),
      endDate: dateRange[1].format('YYYY-MM-DD'),
    }
    try {
      const [overviewData, trendData, deptData, rankingData, rxStats] = await Promise.all([
        statsService.getOverview(query),
        statsService.getConsultationTrend(query),
        statsService.getDepartmentStats(query),
        statsService.getDoctorRanking(query),
        statsService.getPrescriptionStats(query),
      ])
      setOverview(overviewData)
      setTrend(trendData)
      setDepartments(deptData)
      setDoctorRanking(rankingData)
      setPrescriptionStats(rxStats)
    } catch {
      message.error('获取统计数据失败')
    } finally {
      setLoading(false)
    }
  }

  const departmentColumns: ColumnsType<DepartmentStats> = [
    { title: '科室', dataIndex: 'departmentName', key: 'departmentName' },
    { title: '问诊数', dataIndex: 'consultationCount', key: 'consultationCount', align: 'right' },
    { title: '处方数', dataIndex: 'prescriptionCount', key: 'prescriptionCount', align: 'right' },
    { 
      title: '平均评分', 
      dataIndex: 'avgRating', 
      key: 'avgRating',
      align: 'right',
      render: (val) => val?.toFixed(1) || '-'
    },
  ]

  const doctorColumns: ColumnsType<DoctorRanking> = [
    { 
      title: '排名', 
      key: 'rank', 
      width: 60,
      render: (_, __, index) => index + 1
    },
    { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
    { title: '科室', dataIndex: 'departmentName', key: 'departmentName' },
    { title: '问诊数', dataIndex: 'consultationCount', key: 'consultationCount', align: 'right' },
    { 
      title: '评分', 
      dataIndex: 'avgRating', 
      key: 'avgRating',
      align: 'right',
      render: (val) => val?.toFixed(1) || '-'
    },
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="统计分析"
        breadcrumbs={[{ title: '数据中心' }, { title: '统计看板' }]}
        extra={
          <Space>
            <span>时间范围：</span>
            <RangePicker
              value={dateRange}
              onChange={(v) => v && setDateRange(v as [Dayjs, Dayjs])}
              allowClear={false}
            />
          </Space>
        }
      />

      {/* 概览指标 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="总问诊数" 
              value={overview?.totalConsultations || 0} 
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="总处方数" 
              value={overview?.totalPrescriptions || 0} 
              prefix={<MedicineBoxOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="患者数" 
              value={overview?.totalPatients || 0} 
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="医生数" 
              value={overview?.totalDoctors || 0} 
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="平均问诊时长" 
              value={overview?.avgConsultationDuration || 0} 
              suffix="分钟"
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic 
              title="处方通过率" 
              value={overview?.prescriptionApprovalRate || 0} 
              suffix="%"
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        {/* 问诊趋势 */}
        <Col span={16}>
          <Card title="问诊趋势" loading={loading}>
            <div style={{ height: 300, overflow: 'hidden' }}>
              {trend.length > 0 ? (
                <div style={{ display: 'flex', alignItems: 'flex-end', height: 'calc(100% - 40px)', gap: 4, paddingBottom: 40 }}>
                  {trend.map((item, index) => (
                    <div 
                      key={index} 
                      style={{ 
                        flex: 1, 
                        display: 'flex', 
                        flexDirection: 'column', 
                        alignItems: 'center',
                        height: '100%',
                        justifyContent: 'flex-end',
                        minWidth: 0,
                      }}
                    >
                      <div 
                        style={{ 
                          width: '80%', 
                          maxWidth: 30,
                          backgroundColor: '#1890ff',
                          height: `${Math.max((item.count / Math.max(...trend.map(t => t.count), 1)) * 200, 4)}px`,
                          borderRadius: 2,
                        }} 
                        title={`${item.date}: ${item.count}次`}
                      />
                      <div style={{ 
                        fontSize: 10, 
                        color: '#999', 
                        marginTop: 4, 
                        transform: 'rotate(-45deg)',
                        whiteSpace: 'nowrap',
                        position: 'absolute',
                        bottom: 5,
                      }}>
                        {dayjs(item.date).format('MM-DD')}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                  暂无数据
                </div>
              )}
            </div>
          </Card>
        </Col>

        {/* 处方审核统计 */}
        <Col span={8}>
          <Card title="处方审核统计" loading={loading}>
            <div style={{ padding: '20px 0' }}>
              <div style={{ marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                  <span>通过率</span>
                  <span>{prescriptionStats?.approvalRate?.toFixed(1) || 0}%</span>
                </div>
                <Progress 
                  percent={prescriptionStats?.approvalRate || 0} 
                  showInfo={false}
                  strokeColor="#52c41a"
                />
              </div>
              <Row gutter={16}>
                <Col span={8}>
                  <Statistic 
                    title="已通过" 
                    value={prescriptionStats?.approvedCount || 0}
                    valueStyle={{ color: '#52c41a', fontSize: 20 }}
                  />
                </Col>
                <Col span={8}>
                  <Statistic 
                    title="已驳回" 
                    value={prescriptionStats?.rejectedCount || 0}
                    valueStyle={{ color: '#ff4d4f', fontSize: 20 }}
                  />
                </Col>
                <Col span={8}>
                  <Statistic 
                    title="待审核" 
                    value={prescriptionStats?.pendingCount || 0}
                    valueStyle={{ color: '#faad14', fontSize: 20 }}
                  />
                </Col>
              </Row>
              <div style={{ marginTop: 16, color: '#666' }}>
                平均审核时长: {prescriptionStats?.avgReviewTime?.toFixed(1) || 0} 分钟
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        {/* 科室统计 */}
        <Col span={12}>
          <Card title="科室统计" loading={loading}>
            <Table
              columns={departmentColumns}
              dataSource={departments}
              rowKey="departmentId"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>

        {/* 医生排行 */}
        <Col span={12}>
          <Card title="医生问诊排行 TOP10" loading={loading}>
            <Table
              columns={doctorColumns}
              dataSource={doctorRanking}
              rowKey="doctorId"
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default StatsDashboard
