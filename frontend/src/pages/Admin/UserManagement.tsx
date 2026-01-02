import { useState, useEffect } from 'react'
import { Card, Table, Button, Modal, Form, Input, Select, Tag, Space, message, Popconfirm } from 'antd'
import { 
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import { adminService, type User, type UserRole, type UserStatus, type Department } from '@/services/admin'
import { formatDateTime } from '@/utils/date'

const roleColors: Record<string, string> = {
  PATIENT: 'blue',
  DOCTOR: 'green',
  DOCTOR_PRIMARY: 'green',
  DOCTOR_EXPERT: 'purple',
  PHARMACIST: 'orange',
  ADMIN: 'red',
}

const roleLabels: Record<string, string> = {
  PATIENT: '患者',
  DOCTOR_PRIMARY: '基层医生',
  DOCTOR_EXPERT: '专家医生',
  PHARMACIST: '药师',
  ADMIN: '管理员',
}

const statusColors: Record<UserStatus, string> = {
  ACTIVE: 'success',
  DISABLED: 'error',
  PENDING: 'warning',
}

const statusLabels: Record<UserStatus, string> = {
  ACTIVE: '正常',
  DISABLED: '禁用',
  PENDING: '待审核',
}

/**
 * 用户管理页面
 * _Requirements: 9.1, 9.2_
 */
const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([])
  const [departments, setDepartments] = useState<Department[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingUser, setEditingUser] = useState<User | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [filters, setFilters] = useState<{ keyword?: string; role?: UserRole; status?: UserStatus }>({})
  const [form] = Form.useForm()
  // 重置密码相关状态
  const [resetPasswordVisible, setResetPasswordVisible] = useState(false)
  const [resetPasswordUserId, setResetPasswordUserId] = useState<number | null>(null)
  const [resetPasswordSubmitting, setResetPasswordSubmitting] = useState(false)
  const [resetPasswordForm] = Form.useForm()


  useEffect(() => {
    fetchDepartments()
  }, [])

  useEffect(() => {
    fetchUsers()
  }, [page, pageSize])

  const fetchUsers = async () => {
    setLoading(true)
    try {
      const result = await adminService.getUserList({ page, pageSize, ...filters })
      setUsers(result.list)
      setTotal(result.total)
    } catch {
      message.error('获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchDepartments = async () => {
    try {
      const data = await adminService.getDepartments()
      setDepartments(data)
    } catch {
      // ignore
    }
  }

  const handleSearch = () => {
    setPage(1)
    fetchUsers()
  }

  const handleCreate = () => {
    setEditingUser(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (user: User) => {
    setEditingUser(user)
    form.setFieldsValue(user)
    setModalVisible(true)
  }

  const handleSubmit = async (values: Record<string, unknown>) => {
    setSubmitting(true)
    try {
      if (editingUser) {
        await adminService.updateUser(editingUser.id, values)
        message.success('用户已更新')
      } else {
        await adminService.createUser({
          username: values.username as string,
          password: values.password as string,
          realName: values.realName as string,
          phone: values.phone as string,
          role: values.role as UserRole,
          email: values.email as string | undefined,
          departmentId: values.departmentId as number | undefined,
          title: values.title as string | undefined,
        })
        message.success('用户已创建')
      }
      setModalVisible(false)
      fetchUsers()
    } catch (err) {
      message.error((err as Error).message || '操作失败')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await adminService.deleteUser(id)
      message.success('用户已删除')
      fetchUsers()
    } catch (err) {
      message.error((err as Error).message || '删除失败')
    }
  }

  const handleToggleStatus = async (user: User) => {
    const newStatus: UserStatus = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    try {
      await adminService.toggleUserStatus(user.id, newStatus)
      message.success(newStatus === 'ACTIVE' ? '用户已启用' : '用户已禁用')
      fetchUsers()
    } catch (err) {
      message.error((err as Error).message || '操作失败')
    }
  }

  const handleResetPassword = async (id: number) => {
    setResetPasswordUserId(id)
    resetPasswordForm.resetFields()
    setResetPasswordVisible(true)
  }

  const handleResetPasswordSubmit = async (values: { password: string }) => {
    if (!resetPasswordUserId) return
    setResetPasswordSubmitting(true)
    try {
      await adminService.resetPassword(resetPasswordUserId, values.password)
      message.success('密码重置成功')
      setResetPasswordVisible(false)
    } catch (err) {
      message.error((err as Error).message || '重置失败')
    } finally {
      setResetPasswordSubmitting(false)
    }
  }


  const columns: ColumnsType<User> = [
    { title: '用户名', dataIndex: 'username', key: 'username', width: 120 },
    { title: '姓名', dataIndex: 'realName', key: 'realName', width: 100 },
    { title: '手机号', dataIndex: 'phone', key: 'phone', width: 130 },
    { 
      title: '角色', 
      dataIndex: 'role', 
      key: 'role',
      width: 100,
      render: (role: string) => <Tag color={roleColors[role] || 'default'}>{roleLabels[role] || role}</Tag>
    },
    { title: '科室', dataIndex: 'departmentName', key: 'departmentName', width: 100 },
    { 
      title: '状态', 
      dataIndex: 'status', 
      key: 'status',
      width: 80,
      render: (status: UserStatus) => <Tag color={statusColors[status]}>{statusLabels[status]}</Tag>
    },
    { 
      title: '创建时间', 
      dataIndex: 'createdAt', 
      key: 'createdAt',
      width: 170,
      render: (text) => formatDateTime(text)
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button 
            type="link" 
            size="small" 
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === 'ACTIVE' ? '禁用' : '启用'}
          </Button>
          <Button type="link" size="small" icon={<LockOutlined />} onClick={() => handleResetPassword(record.id)}>
            重置密码
          </Button>
          <Popconfirm title="确定删除该用户？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="用户管理"
        breadcrumbs={[{ title: '管理后台' }, { title: '用户管理' }]}
      />

      <Card 
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            新增用户
          </Button>
        }
      >
        <Space style={{ marginBottom: 16 }} wrap>
          <Input
            placeholder="搜索用户名/姓名/手机"
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            style={{ width: 200 }}
            allowClear
          />
          <Select
            placeholder="角色"
            value={filters.role}
            onChange={(v) => setFilters({ ...filters, role: v })}
            style={{ width: 100 }}
            allowClear
            options={Object.entries(roleLabels).map(([value, label]) => ({ value, label }))}
          />
          <Select
            placeholder="状态"
            value={filters.status}
            onChange={(v) => setFilters({ ...filters, status: v })}
            style={{ width: 100 }}
            allowClear
            options={Object.entries(statusLabels).map(([value, label]) => ({ value, label }))}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
        </Space>

        <Table
          columns={columns}
          dataSource={users}
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
          scroll={{ x: 1100 }}
        />
      </Card>


      {/* 新增/编辑用户弹窗 */}
      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item 
            name="username" 
            label="用户名" 
            rules={[{ required: !editingUser, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入用户名" disabled={!!editingUser} />
          </Form.Item>
          {!editingUser && (
            <Form.Item 
              name="password" 
              label="密码" 
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
          )}
          <Form.Item 
            name="realName" 
            label="姓名" 
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item 
            name="phone" 
            label="手机号" 
            rules={[{ required: true, message: '请输入手机号' }]}
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item 
            name="role" 
            label="角色" 
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select
              placeholder="请选择角色"
              options={Object.entries(roleLabels).map(([value, label]) => ({ value, label }))}
            />
          </Form.Item>
          <Form.Item name="departmentId" label="科室">
            <Select
              placeholder="请选择科室"
              allowClear
              options={departments.map((d) => ({ value: d.id, label: d.name }))}
            />
          </Form.Item>
          <Form.Item name="title" label="职称">
            <Input placeholder="请输入职称（医生适用）" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              {editingUser ? '保存' : '创建'}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 重置密码弹窗 */}
      <Modal
        title="重置密码"
        open={resetPasswordVisible}
        onCancel={() => setResetPasswordVisible(false)}
        footer={null}
        width={400}
      >
        <Form form={resetPasswordForm} layout="vertical" onFinish={handleResetPasswordSubmit}>
          <Form.Item 
            name="password" 
            label="新密码" 
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码至少6位' }
            ]}
          >
            <Input.Password placeholder="请输入新密码" />
          </Form.Item>
          <Form.Item 
            name="confirmPassword" 
            label="确认密码" 
            dependencies={['password']}
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'))
                },
              }),
            ]}
          >
            <Input.Password placeholder="请再次输入新密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={resetPasswordSubmitting} block>
              确认重置
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default UserManagement
