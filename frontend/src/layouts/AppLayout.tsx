/**
 * 全局布局组件
 * _Requirements: 1.6_
 */

import React, { useState, useMemo } from 'react';
import { Layout, Menu, Avatar, Dropdown, Space, Typography } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  HomeOutlined,
  MedicineBoxOutlined,
  ExperimentOutlined,
  SettingOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { filterMenuByRoles, MenuItem } from '@/routes/menu';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

// 图标映射
const iconMap: Record<string, React.ReactNode> = {
  UserOutlined: <UserOutlined />,
  MedicineBoxOutlined: <MedicineBoxOutlined />,
  ExperimentOutlined: <ExperimentOutlined />,
  SettingOutlined: <SettingOutlined />,
  BarChartOutlined: <BarChartOutlined />,
  HomeOutlined: <HomeOutlined />,
};

/**
 * 将菜单配置转换为 Ant Design Menu 格式
 */
const convertToAntdMenu = (items: MenuItem[]): MenuProps['items'] => {
  return items.map((item) => ({
    key: item.key,
    icon: item.icon ? iconMap[item.icon] : undefined,
    label: item.label,
    children: item.children ? convertToAntdMenu(item.children) : undefined,
  }));
};

/**
 * 获取菜单项对应的路径
 */
const getMenuPath = (items: MenuItem[], key: string): string | undefined => {
  for (const item of items) {
    if (item.key === key && item.path) {
      return item.path;
    }
    if (item.children) {
      const path = getMenuPath(item.children, key);
      if (path) return path;
    }
  }
  return undefined;
};

/**
 * 根据路径获取选中的菜单项
 */
const getSelectedKeys = (items: MenuItem[], pathname: string): string[] => {
  const keys: string[] = [];
  const findKey = (menuItems: MenuItem[]) => {
    for (const item of menuItems) {
      if (item.path === pathname) {
        keys.push(item.key);
        return true;
      }
      if (item.children) {
        if (findKey(item.children)) {
          keys.push(item.key);
          return true;
        }
      }
    }
    return false;
  };
  findKey(items);
  return keys;
};

/**
 * 全局布局组件
 * 实现侧边菜单与顶部栏
 * _Requirements: 1.6_
 */
const AppLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { me, logout } = useAuthStore();

  // 根据用户角色过滤菜单
  const filteredMenu = useMemo(() => {
    return filterMenuByRoles(me?.roles || []);
  }, [me?.roles]);

  // 转换为 Ant Design Menu 格式
  const menuItems = useMemo(() => convertToAntdMenu(filteredMenu), [filteredMenu]);

  // 获取选中的菜单项
  const selectedKeys = useMemo(
    () => getSelectedKeys(filteredMenu, location.pathname),
    [filteredMenu, location.pathname]
  );

  // 菜单点击处理
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    const path = getMenuPath(filteredMenu, key);
    if (path) {
      navigate(path);
    }
  };

  // 用户下拉菜单
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
    },
  ];

  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'logout') {
      logout();
      navigate('/login', { replace: true });
    } else if (key === 'profile') {
      navigate('/profile');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 侧边栏 */}
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        theme="light"
        style={{
          boxShadow: '2px 0 8px rgba(0,0,0,0.05)',
        }}
      >
        {/* Logo */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          <Text strong style={{ fontSize: collapsed ? 14 : 18, color: '#1890ff' }}>
            {collapsed ? '耳康' : '耳康云诊'}
          </Text>
        </div>

        {/* 菜单 */}
        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          defaultOpenKeys={selectedKeys}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ borderRight: 0 }}
        />
      </Sider>

      <Layout>
        {/* 顶部栏 */}
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
          }}
        >
          {/* 折叠按钮 */}
          <div
            onClick={() => setCollapsed(!collapsed)}
            style={{ cursor: 'pointer', fontSize: 18 }}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>

          {/* 用户信息 */}
          <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }}>
            <Space style={{ cursor: 'pointer' }}>
              <Avatar icon={<UserOutlined />} src={me?.avatar} />
              <Text>{me?.realName || me?.username || '用户'}</Text>
            </Space>
          </Dropdown>
        </Header>

        {/* 内容区 */}
        <Content
          style={{
            margin: 16,
            padding: 24,
            background: '#f5f5f5',
            minHeight: 280,
            overflow: 'auto',
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AppLayout;
