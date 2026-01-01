/**
 * 页面头部组件
 * _Requirements: 2.3_
 */

import React from 'react';
import { Breadcrumb, Typography, Space } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';

const { Title, Text } = Typography;

export interface BreadcrumbItem {
  title: string;
  href?: string;
}

export interface PageHeaderProps {
  /** 页面标题 */
  title: string;
  /** 副标题 */
  subtitle?: string;
  /** 面包屑导航 */
  breadcrumbs?: BreadcrumbItem[];
  /** 操作区域 */
  extra?: React.ReactNode;
  /** 返回按钮点击回调 */
  onBack?: () => void;
  /** 自定义样式 */
  style?: React.CSSProperties;
}

/**
 * 页面头部组件
 * 支持标题、副标题、面包屑、操作区
 * _Requirements: 2.3_
 */
export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  subtitle,
  breadcrumbs,
  extra,
  onBack,
  style,
}) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate(-1);
    }
  };

  return (
    <div
      style={{
        padding: '16px 24px',
        background: '#fff',
        marginBottom: 16,
        borderRadius: 8,
        ...style,
      }}
    >
      {/* 面包屑 */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <Breadcrumb
          style={{ marginBottom: 12 }}
          items={breadcrumbs.map((item, index) => ({
            key: index,
            title: item.href ? <Link to={item.href}>{item.title}</Link> : item.title,
          }))}
        />
      )}

      {/* 标题行 */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Space align="center">
          {onBack && (
            <ArrowLeftOutlined
              onClick={handleBack}
              style={{ fontSize: 16, cursor: 'pointer', marginRight: 8 }}
            />
          )}
          <div>
            <Title level={4} style={{ margin: 0 }}>
              {title}
            </Title>
            {subtitle && (
              <Text type="secondary" style={{ marginTop: 4, display: 'block' }}>
                {subtitle}
              </Text>
            )}
          </div>
        </Space>

        {/* 操作区 */}
        {extra && <div>{extra}</div>}
      </div>
    </div>
  );
};

export default PageHeader;
