import { useState, useEffect } from 'react';
import { Card, Row, Col, Typography, Statistic, List, Spin, Button, Space } from 'antd';
import {
  CalendarOutlined,
  FileTextOutlined,
  RobotOutlined,
  MedicineBoxOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { patientService, PatientDashboard } from '@/services/patient';
import { consultationService } from '@/services/consultation';
import { maskPhone, maskIdNo } from '@/utils/mask';
import { formatDateTime } from '@/utils/date';
import EmptyState from '@/components/EmptyState';
import ErrorState from '@/components/ErrorState';
import ConsultationStatusTag from '@/components/ConsultationStatusTag';
import type { Consultation } from '@/types';

const { Title, Text } = Typography;

/**
 * 患者首页
 * _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
 */
const PatientHome = () => {
  const navigate = useNavigate();
  const { me } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dashboard, setDashboard] = useState<PatientDashboard | null>(null);
  const [upcomingConsultations, setUpcomingConsultations] = useState<Consultation[]>([]);

  // 加载数据
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [dashboardData, consultationsData] = await Promise.all([
          patientService.getDashboard(),
          consultationService.getList({
            page: 1,
            pageSize: 5,
            status: 'WAITING',
          }),
        ]);
        setDashboard(dashboardData);
        // 兼容后端返回 records 或 list，确保返回数组
        let consultationList: Consultation[] = [];
        const consultData = consultationsData as any;
        if (consultData) {
          if (Array.isArray(consultData.records)) {
            consultationList = consultData.records;
          } else if (Array.isArray(consultData.list)) {
            consultationList = consultData.list;
          } else if (Array.isArray(consultData)) {
            consultationList = consultData;
          }
        }
        setUpcomingConsultations(consultationList);
      } catch (err) {
        setError((err as Error).message || '加载失败');
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  // 快捷入口
  // _Requirements: 3.3_
  const menuItems = [
    {
      icon: <CalendarOutlined />,
      title: '预约问诊',
      desc: '在线预约专家',
      path: '/patient/appointment',
    },
    {
      icon: <FileTextOutlined />,
      title: '问诊记录',
      desc: '查看历史记录',
      path: '/patient/records',
    },
    {
      icon: <MedicineBoxOutlined />,
      title: '我的处方',
      desc: '查看处方记录',
      path: '/patient/prescriptions',
    },
    {
      icon: <ClockCircleOutlined />,
      title: '随访计划',
      desc: '查看随访任务',
      path: '/patient/followups',
    },
    {
      icon: <RobotOutlined />,
      title: 'AI问答',
      desc: '智能健康咨询',
      path: '/patient/ai-chat',
    },
  ];

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return <ErrorState message={error} onRetry={() => window.location.reload()} />;
  }

  return (
    <div>
      {/* 个人信息卡片 */}
      {/* _Requirements: 3.1 */}
      <Card style={{ marginBottom: 24 }}>
        <Row gutter={24} align="middle">
          <Col flex="auto">
            <Title level={4} style={{ marginBottom: 8 }}>
              欢迎回来，{dashboard?.profile?.name || me?.realName || '用户'}
            </Title>
            <Space size="large">
              {dashboard?.profile?.phoneMasked && (
                <Text type="secondary">手机：{maskPhone(dashboard.profile.phoneMasked)}</Text>
              )}
              {dashboard?.profile?.idNoMasked && (
                <Text type="secondary">身份证：{maskIdNo(dashboard.profile.idNoMasked)}</Text>
              )}
            </Space>
          </Col>
          <Col>
            <Button type="primary" onClick={() => navigate('/patient/appointment')}>
              立即预约
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 统计数据 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="待进行问诊"
              value={dashboard?.upcomingConsultations || 0}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="历史问诊"
              value={dashboard?.totalConsultations || 0}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="有效处方"
              value={dashboard?.activePrescriptions || 0}
              prefix={<MedicineBoxOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title="待完成随访"
              value={dashboard?.pendingFollowups || 0}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={24}>
        {/* 待进行问诊列表 */}
        {/* _Requirements: 3.2 */}
        <Col xs={24} lg={12}>
          <Card
            title="待进行问诊"
            extra={
              <Button type="link" onClick={() => navigate('/patient/records')}>
                查看全部
              </Button>
            }
          >
            {upcomingConsultations.length > 0 ? (
              <List
                dataSource={upcomingConsultations}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <Button
                        type="link"
                        key="view"
                        onClick={() => navigate(`/patient/consultation/${item.id}`)}
                      >
                        查看
                      </Button>,
                    ]}
                  >
                    <List.Item.Meta
                      title={
                        <Space>
                          <span>问诊 #{item.id}</span>
                          <ConsultationStatusTag status={item.status} />
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={0}>
                          <Text type="secondary">
                            预约时间：{formatDateTime(item.scheduledAt)}
                          </Text>
                          {item.symptoms && (
                            <Text type="secondary" ellipsis>
                              症状：{item.symptoms}
                            </Text>
                          )}
                        </Space>
                      }
                    />
                  </List.Item>
                )}
              />
            ) : (
              <EmptyState
                title="暂无待进行问诊"
                description="您可以预约新的问诊"
                actionText="立即预约"
                onAction={() => navigate('/patient/appointment')}
              />
            )}
          </Card>
        </Col>

        {/* 快捷入口 */}
        {/* _Requirements: 3.3 */}
        <Col xs={24} lg={12}>
          <Card title="快捷入口">
            <Row gutter={[16, 16]}>
              {menuItems.map((item, index) => (
                <Col xs={12} sm={8} key={index}>
                  <Card
                    hoverable
                    size="small"
                    onClick={() => navigate(item.path)}
                    style={{ textAlign: 'center' }}
                  >
                    <div style={{ fontSize: 32, color: '#1890ff', marginBottom: 8 }}>
                      {item.icon}
                    </div>
                    <Text strong>{item.title}</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {item.desc}
                    </Text>
                  </Card>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default PatientHome;
