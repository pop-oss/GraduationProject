/**
 * 403 禁止访问页面
 * _Requirements: 1.7_
 */

import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { getDefaultHomePath } from '@/routes/menu';

/**
 * 403 禁止访问页面
 * _Requirements: 1.7_
 */
const ForbiddenPage: React.FC = () => {
  const navigate = useNavigate();
  const { me } = useAuthStore();

  const handleGoHome = () => {
    const homePath = me?.roles ? getDefaultHomePath(me.roles) : '/login';
    navigate(homePath, { replace: true });
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: '#f5f5f5',
      }}
    >
      <Result
        status="403"
        title="403"
        subTitle="抱歉，您没有权限访问此页面"
        extra={[
          <Button type="primary" key="home" onClick={handleGoHome}>
            返回首页
          </Button>,
          <Button key="back" onClick={handleGoBack}>
            返回上一页
          </Button>,
        ]}
      />
    </div>
  );
};

export default ForbiddenPage;
