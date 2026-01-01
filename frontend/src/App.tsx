import { Suspense, useEffect } from 'react'
import { useRoutes } from 'react-router-dom'
import { Spin, ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { useAuthStore } from '@/store/useAuthStore'
import routes from '@/routes'

/**
 * 全局加载组件
 */
const GlobalLoading = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh',
    background: '#f5f5f5',
  }}>
    <Spin size="large" tip="加载中..." />
  </div>
)

/**
 * 应用入口组件
 * _Requirements: 1.1-1.7_
 */
function App() {
  const { bootstrap } = useAuthStore()
  const element = useRoutes(routes)

  // 应用启动时恢复认证状态
  useEffect(() => {
    bootstrap()
  }, [bootstrap])

  return (
    <ConfigProvider locale={zhCN}>
      <Suspense fallback={<GlobalLoading />}>
        {element}
      </Suspense>
    </ConfigProvider>
  )
}

export default App
