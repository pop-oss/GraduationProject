/**
 * 视频房间布局组件
 * _Requirements: 4.7_
 */

import React from 'react';
import { Layout } from 'antd';

const { Header, Content, Sider } = Layout;

export interface RoomLayoutProps {
  /** 头部额外内容 */
  headerExtra?: React.ReactNode;
  /** 右侧面板 */
  rightPanel?: React.ReactNode;
  /** 视频区域 */
  videoArea?: React.ReactNode;
  /** 控制栏 */
  controls?: React.ReactNode;
  /** 头部标题 */
  title?: string;
}

/**
 * 视频房间布局组件
 * _Requirements: 4.7_
 */
export const RoomLayout: React.FC<RoomLayoutProps> = ({
  headerExtra,
  rightPanel,
  videoArea,
  controls,
  title = '视频问诊',
}) => {
  return (
    <Layout style={{ height: '100vh', background: '#1a1a1a' }}>
      {/* 头部 */}
      <Header
        style={{
          background: '#262626',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '1px solid #333',
        }}
      >
        <div style={{ color: '#fff', fontSize: 18, fontWeight: 500 }}>{title}</div>
        <div>{headerExtra}</div>
      </Header>

      <Layout>
        {/* 主内容区 - 视频 */}
        <Content
          style={{
            display: 'flex',
            flexDirection: 'column',
            position: 'relative',
          }}
        >
          {/* 视频区域 */}
          <div style={{ flex: 1, position: 'relative' }}>{videoArea}</div>

          {/* 控制栏 */}
          {controls && (
            <div
              style={{
                height: 80,
                background: '#262626',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                borderTop: '1px solid #333',
              }}
            >
              {controls}
            </div>
          )}
        </Content>

        {/* 右侧面板 */}
        {rightPanel && (
          <Sider
            width={320}
            style={{
              background: '#262626',
              borderLeft: '1px solid #333',
            }}
          >
            {rightPanel}
          </Sider>
        )}
      </Layout>
    </Layout>
  );
};

export default RoomLayout;
