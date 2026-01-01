import { useState, useEffect } from 'react'
import { Card, Row, Col, Statistic } from 'antd'
import { 
  UserOutlined, 
  TeamOutlined, 
  FileTextOutlined,
  MedicineBoxOutlined,
} from '@ant-design/icons'
import PageHeader from '@/components/PageHeader'
import { adminService, type SystemStats } from '@/services/admin'

/**
 * 管理后台首页 - 系统概览
 * _Requirements: 9.1_
 */
const AdminDashboard = () => {
  const [stats, setStats] = useState<SystemStats | null>(null)

  useEffect(() => {
    fetchStats()
  }, [])

  const fetchStats = async () => {
    try {
      const data = await adminService.getStats()
      setStats(data)
    } catch {
      // ignore
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="系统概览"
        breadcrumbs={[{ title: '管理后台' }, { title: '控制台' }]}
      />

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic 
              title="注册用户" 
              value={stats?.totalUsers || 0} 
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="在线医生" 
              value={stats?.onlineDoctors || 0} 
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="累计问诊" 
              value={stats?.totalConsultations || 0} 
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="累计处方" 
              value={stats?.totalPrescriptions || 0} 
              prefix={<MedicineBoxOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 更多统计信息 */}
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic 
              title="医生总数" 
              value={stats?.totalDoctors || 0} 
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="患者总数" 
              value={stats?.totalPatients || 0} 
              prefix={<UserOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="药师总数" 
              value={stats?.totalPharmacists || 0} 
              prefix={<MedicineBoxOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic 
              title="今日问诊" 
              value={stats?.todayConsultations || 0} 
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default AdminDashboard
