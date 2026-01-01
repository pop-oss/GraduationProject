import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Card, Form, Input, Button, Typography, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/store/useAuthStore';
import { authService, LoginParams } from '@/services/auth';
import { getDefaultHomePath } from '@/routes/menu';

const { Title, Text } = Typography;

/**
 * 登录页面
 * _Requirements: 1.2, 1.3_
 */
const Login = () => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { setToken, setMe } = useAuthStore();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname;

  const onFinish = async (values: LoginParams) => {
    setLoading(true);
    try {
      const result = await authService.login(values);
      // 存储 token 和用户信息
      setToken(result.accessToken, result.refreshToken);
      setMe(result.user);
      message.success('登录成功');

      // 根据角色跳转到对应首页
      const targetPath = from || getDefaultHomePath(result.user.roles);
      navigate(targetPath, { replace: true });
    } catch (error: unknown) {
      const err = error as Error;
      message.error(err.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card style={{ width: 400, borderRadius: 8 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ marginBottom: 8, color: '#1890ff' }}>
            耳康云诊
          </Title>
          <Text type="secondary">耳鼻喉科远程医疗系统</Text>
        </div>

        <Form name="login" onFinish={onFinish} autoComplete="off" size="large">
          <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input prefix={<UserOutlined />} placeholder="用户名" />
          </Form.Item>

          <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="密码" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center' }}>
          <Text type="secondary" style={{ fontSize: 12 }}>
            © 2024 耳康云诊 - 专业耳鼻喉科远程医疗平台
          </Text>
        </div>
      </Card>
    </div>
  );
};

export default Login;
