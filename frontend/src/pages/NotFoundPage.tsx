/**
 * 404 页面不存在
 * _Requirements: 1.7_
 */

import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { getDefaultHomePath } from '@/routes/menu';

/**
 * 404 页面不存在
 * _Requirements: 1.7_
 */
const NotFoundPage: React.FC = () => {
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
        status="404"
        title="404"
        subTitle="抱歉，您访问的页面不存在"
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

export default NotFoundPage;
