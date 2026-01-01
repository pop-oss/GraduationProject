/**
 * 搜索筛选栏组件
 * _Requirements: 2.5_
 */

import React from 'react';
import { Form, Button, Space, Row, Col } from 'antd';
import { SearchOutlined, ReloadOutlined } from '@ant-design/icons';

export interface SearchFilterBarProps {
  /** 初始值 */
  initialValues?: Record<string, unknown>;
  /** 搜索回调 */
  onSearch: (values: Record<string, unknown>) => void;
  /** 重置回调 */
  onReset?: () => void;
  /** 额外操作区 */
  extra?: React.ReactNode;
  /** 筛选控件 */
  children: React.ReactNode;
  /** 是否显示搜索按钮 */
  showSearchButton?: boolean;
  /** 是否显示重置按钮 */
  showResetButton?: boolean;
  /** 搜索按钮文字 */
  searchText?: string;
  /** 重置按钮文字 */
  resetText?: string;
  /** 是否加载中 */
  loading?: boolean;
}

/**
 * 搜索筛选栏组件
 * 支持自定义筛选控件，实现查询/重置功能
 * _Requirements: 2.5_
 */
export const SearchFilterBar: React.FC<SearchFilterBarProps> = ({
  initialValues,
  onSearch,
  onReset,
  extra,
  children,
  showSearchButton = true,
  showResetButton = true,
  searchText = '查询',
  resetText = '重置',
  loading = false,
}) => {
  const [form] = Form.useForm();

  const handleSearch = () => {
    const values = form.getFieldsValue();
    onSearch(values);
  };

  const handleReset = () => {
    form.resetFields();
    if (onReset) {
      onReset();
    } else {
      onSearch({});
    }
  };

  return (
    <div
      style={{
        padding: '16px 24px',
        background: '#fff',
        marginBottom: 16,
        borderRadius: 8,
      }}
    >
      <Form form={form} initialValues={initialValues} layout="inline">
        <Row gutter={[16, 16]} style={{ width: '100%' }}>
          <Col flex="auto">
            <Space wrap>{children}</Space>
          </Col>
          <Col>
            <Space>
              {showSearchButton && (
                <Button
                  type="primary"
                  icon={<SearchOutlined />}
                  onClick={handleSearch}
                  loading={loading}
                >
                  {searchText}
                </Button>
              )}
              {showResetButton && (
                <Button icon={<ReloadOutlined />} onClick={handleReset}>
                  {resetText}
                </Button>
              )}
              {extra}
            </Space>
          </Col>
        </Row>
      </Form>
    </div>
  );
};

export default SearchFilterBar;
