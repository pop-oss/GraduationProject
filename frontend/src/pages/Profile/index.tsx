import { useState, useEffect } from 'react'
import { Card, Form, Input, Button, Avatar, Upload, message, Descriptions, Tabs, Space } from 'antd'
import { UserOutlined, CameraOutlined, LockOutlined, SaveOutlined } from '@ant-design/icons'
import type { UploadChangeParam } from 'antd/es/upload'
import PageHeader from '@/components/PageHeader'
import { useAuthStore } from '@/store/useAuthStore'
import { authService } from '@/services/auth'
import { formatDateTime } from '@/utils/date'

const { TabPane } = Tabs

// 角色中文映射
const roleLabels: Record<string, string> = {
  ADMIN: '系统管理员',
  PATIENT: '患者',
  DOCTOR_PRIMARY: '主治医师',
  DOCTOR_EXPERT: '专家',
  PHARMACIST: '药师',
}

/**
 * 个人中心页面
 */
const ProfilePage = () => {
  const { me, setMe } = useAuthStore()
  const [form] = Form.useForm()
  const [passwordForm] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [passwordLoading, setPasswordLoading] = useState(false)

  useEffect(() => {
    if (me) {
      form.setFieldsValue({
        realName: me.realName,
        phone: me.phone,
        email: me.email,
      })
    }
  }, [me, form])

  const handleUpdateProfile = async (values: { realName?: string; phone?: string; email?: string }) => {
    setLoading(true)
    try {
      await authService.updateProfile(values)
      // 更新本地状态
      if (me) {
        setMe({ ...me, ...values })
      }
      message.success('个人信息更新成功')
    } catch {
      message.error('更新失败')
    } finally {
      setLoading(false)
    }
  }

  const handleChangePassword = async (values: { oldPassword: string; newPassword: string }) => {
    setPasswordLoading(true)
    try {
      await authService.changePassword(values.oldPassword, values.newPassword)
      message.success('密码修改成功')
      passwordForm.resetFields()
    } catch {
      message.error('密码修改失败')
    } finally {
      setPasswordLoading(false)
    }
  }

  const handleAvatarChange = (info: UploadChangeParam) => {
    if (info.file.status === 'done') {
      message.success('头像上传成功')
      // 更新头像URL
      if (info.file.response?.data?.url && me) {
        setMe({ ...me, avatar: info.file.response.data.url })
      }
    } else if (info.file.status === 'error') {
      message.error('头像上传失败')
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="个人中心"
        breadcrumbs={[{ title: '个人中心' }]}
      />

      <Card>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 24 }}>
          <Upload
            name="file"
            action="/api/file/upload"
            showUploadList={false}
            onChange={handleAvatarChange}
            accept="image/*"
          >
            <div style={{ position: 'relative', cursor: 'pointer' }}>
              <Avatar size={80} icon={<UserOutlined />} src={me?.avatar} />
              <div
                style={{
                  position: 'absolute',
                  bottom: 0,
                  right: 0,
                  background: '#1890ff',
                  borderRadius: '50%',
                  padding: 4,
                  color: '#fff',
                }}
              >
                <CameraOutlined />
              </div>
            </div>
          </Upload>
          <div style={{ marginLeft: 24 }}>
            <h2 style={{ margin: 0 }}>{me?.realName || me?.username}</h2>
            <Space style={{ marginTop: 8 }}>
              {me?.roles?.map(role => (
                <span key={role} style={{ color: '#1890ff' }}>
                  {roleLabels[role] || role}
                </span>
              ))}
            </Space>
          </div>
        </div>

        <Tabs defaultActiveKey="info">
          <TabPane tab="基本信息" key="info">
            <Descriptions column={2} bordered style={{ marginBottom: 24 }}>
              <Descriptions.Item label="用户名">{me?.username}</Descriptions.Item>
              <Descriptions.Item label="用户ID">{me?.id}</Descriptions.Item>
              <Descriptions.Item label="角色">
                {me?.roles?.map(role => roleLabels[role] || role).join('、')}
              </Descriptions.Item>
              <Descriptions.Item label="注册时间">
                {me?.createdAt ? formatDateTime(me.createdAt) : '-'}
              </Descriptions.Item>
            </Descriptions>

            <Form
              form={form}
              layout="vertical"
              onFinish={handleUpdateProfile}
              style={{ maxWidth: 400 }}
            >
              <Form.Item
                name="realName"
                label="真实姓名"
                rules={[{ required: true, message: '请输入真实姓名' }]}
              >
                <Input placeholder="请输入真实姓名" />
              </Form.Item>
              <Form.Item
                name="phone"
                label="手机号"
                rules={[
                  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
                ]}
              >
                <Input placeholder="请输入手机号" />
              </Form.Item>
              <Form.Item
                name="email"
                label="邮箱"
                rules={[
                  { type: 'email', message: '请输入正确的邮箱' }
                ]}
              >
                <Input placeholder="请输入邮箱" />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading} icon={<SaveOutlined />}>
                  保存修改
                </Button>
              </Form.Item>
            </Form>
          </TabPane>

          <TabPane tab="修改密码" key="password">
            <Form
              form={passwordForm}
              layout="vertical"
              onFinish={handleChangePassword}
              style={{ maxWidth: 400 }}
            >
              <Form.Item
                name="oldPassword"
                label="当前密码"
                rules={[{ required: true, message: '请输入当前密码' }]}
              >
                <Input.Password placeholder="请输入当前密码" prefix={<LockOutlined />} />
              </Form.Item>
              <Form.Item
                name="newPassword"
                label="新密码"
                rules={[
                  { required: true, message: '请输入新密码' },
                  { min: 6, message: '密码至少6位' }
                ]}
              >
                <Input.Password placeholder="请输入新密码" prefix={<LockOutlined />} />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="确认新密码"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: '请确认新密码' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve()
                      }
                      return Promise.reject(new Error('两次输入的密码不一致'))
                    },
                  }),
                ]}
              >
                <Input.Password placeholder="请再次输入新密码" prefix={<LockOutlined />} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" loading={passwordLoading} icon={<LockOutlined />}>
                  修改密码
                </Button>
              </Form.Item>
            </Form>
          </TabPane>
        </Tabs>
      </Card>
    </div>
  )
}

export default ProfilePage
