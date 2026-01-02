import { useState, useEffect } from 'react'
import { Card, Table, Tree, Button, Modal, message, Tag, Spin } from 'antd'
import { SafetyOutlined, SaveOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import PageHeader from '@/components/PageHeader'
import ErrorState from '@/components/ErrorState'
import { adminService, type Role, type Permission } from '@/services/admin'
import { convertToTreeData, getAllPermissionIds } from './rolePermission.utils'

// 角色颜色映射
const roleColors: Record<string, string> = {
  ADMIN: 'red',
  DOCTOR_PRIMARY: 'blue',
  DOCTOR_EXPERT: 'purple',
  PHARMACIST: 'green',
  PATIENT: 'default',
}

// 导出工具函数供测试使用
export { convertToTreeData, getAllPermissionIds } from './rolePermission.utils'


/**
 * 权限管理页面
 * _Requirements: 1.1-1.5, 2.1-2.5, 3.1-3.9_
 */
const RolePermissionPage = () => {
  const [roles, setRoles] = useState<Role[]>([])
  const [permissions, setPermissions] = useState<Permission[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [selectedRole, setSelectedRole] = useState<Role | null>(null)
  const [checkedKeys, setCheckedKeys] = useState<number[]>([])
  const [originalCheckedKeys, setOriginalCheckedKeys] = useState<number[]>([])
  const [modalVisible, setModalVisible] = useState(false)
  const [saving, setSaving] = useState(false)
  const [permissionLoading, setPermissionLoading] = useState(false)

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    setLoadError(null)
    try {
      const [rolesData, permsData] = await Promise.all([
        adminService.getRoles(),
        adminService.getPermissionTree(),
      ])
      setRoles(rolesData)
      setPermissions(permsData)
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : '获取数据失败'
      setLoadError(errorMsg)
      message.error('获取数据失败')
    } finally {
      setLoading(false)
    }
  }

  const handleEditPermissions = async (role: Role) => {
    setSelectedRole(role)
    setPermissionLoading(true)
    setModalVisible(true)
    try {
      const detail = await adminService.getRoleDetail(role.id)
      const permIds = detail.permissionIds || []
      setCheckedKeys(permIds)
      setOriginalCheckedKeys(permIds)
    } catch {
      message.error('获取角色权限失败')
      setModalVisible(false)
    } finally {
      setPermissionLoading(false)
    }
  }

  const handleSavePermissions = async () => {
    if (!selectedRole) return
    
    // 检查是否有变更 _Requirements: 3.9_
    const hasChanges = JSON.stringify([...checkedKeys].sort()) !== 
                       JSON.stringify([...originalCheckedKeys].sort())
    
    if (!hasChanges) {
      message.info('权限未发生变更')
      setModalVisible(false)
      return
    }

    // 保存确认提示 _Requirements: 3.9_
    Modal.confirm({
      title: '确认保存',
      icon: <ExclamationCircleOutlined />,
      content: `确定要更新角色「${selectedRole.roleName}」的权限吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        setSaving(true)
        try {
          await adminService.updateRolePermissions(selectedRole.id, checkedKeys)
          message.success('权限保存成功')
          setModalVisible(false)
          fetchData()
        } catch (err) {
          const errorMsg = err instanceof Error ? err.message : '保存失败'
          message.error(errorMsg)
        } finally {
          setSaving(false)
        }
      },
    })
  }

  const handleModalClose = () => {
    // 检查是否有未保存的变更
    const hasChanges = JSON.stringify([...checkedKeys].sort()) !== 
                       JSON.stringify([...originalCheckedKeys].sort())
    
    if (hasChanges) {
      Modal.confirm({
        title: '确认关闭',
        icon: <ExclamationCircleOutlined />,
        content: '您有未保存的更改，确定要关闭吗？',
        okText: '确认关闭',
        cancelText: '继续编辑',
        onOk: () => setModalVisible(false),
      })
    } else {
      setModalVisible(false)
    }
  }

  const columns: ColumnsType<Role> = [
    {
      title: '角色编码',
      dataIndex: 'roleCode',
      key: 'roleCode',
      render: (code: string) => (
        <Tag color={roleColors[code] || 'default'}>{code}</Tag>
      ),
    },
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          type="link"
          icon={<SafetyOutlined />}
          onClick={() => handleEditPermissions(record)}
        >
          配置权限
        </Button>
      ),
    },
  ]

  const treeData = convertToTreeData(permissions)

  // 加载失败时显示 ErrorState _Requirements: 1.5_
  if (loadError && !loading) {
    return (
      <div style={{ padding: 24 }}>
        <PageHeader
          title="权限管理"
          breadcrumbs={[
            { title: '管理后台', href: '/admin' },
            { title: '权限管理' },
          ]}
        />
        <Card>
          <ErrorState
            title="加载失败"
            message={loadError}
            onRetry={fetchData}
          />
        </Card>
      </div>
    )
  }

  return (
    <div style={{ padding: 24 }}>
      <PageHeader
        title="权限管理"
        breadcrumbs={[
          { title: '管理后台', href: '/admin' },
          { title: '权限管理' },
        ]}
      />

      <Card>
        <Table
          columns={columns}
          dataSource={roles}
          rowKey="id"
          loading={loading}
          pagination={false}
        />
      </Card>

      <Modal
        title={`配置权限 - ${selectedRole?.roleName}`}
        open={modalVisible}
        onCancel={handleModalClose}
        width={600}
        footer={[
          <Button key="cancel" onClick={handleModalClose}>
            取消
          </Button>,
          <Button
            key="selectAll"
            onClick={() => setCheckedKeys(getAllPermissionIds(permissions))}
            disabled={permissionLoading}
          >
            全选
          </Button>,
          <Button
            key="clearAll"
            onClick={() => setCheckedKeys([])}
            disabled={permissionLoading}
          >
            清空
          </Button>,
          <Button
            key="save"
            type="primary"
            icon={<SaveOutlined />}
            loading={saving}
            disabled={permissionLoading}
            onClick={handleSavePermissions}
          >
            保存
          </Button>,
        ]}
      >
        {permissionLoading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>
            <Spin tip="加载权限中..." />
          </div>
        ) : permissions.length > 0 ? (
          <Tree
            checkable
            defaultExpandAll
            checkedKeys={checkedKeys}
            onCheck={(checked) => {
              if (Array.isArray(checked)) {
                setCheckedKeys(checked as number[])
              } else {
                setCheckedKeys(checked.checked as number[])
              }
            }}
            treeData={treeData}
            style={{ maxHeight: 400, overflow: 'auto' }}
          />
        ) : (
          <ErrorState
            title="暂无权限数据"
            message="系统中没有配置权限项"
          />
        )}
      </Modal>
    </div>
  )
}

export default RolePermissionPage
