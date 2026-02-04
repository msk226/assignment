import { Layout, Menu, Typography } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
  AppstoreOutlined,
  DollarOutlined,
  ShoppingOutlined,
  ProfileOutlined
} from '@ant-design/icons'

const { Header, Sider, Content } = Layout

const menuItems = [
  { key: '/', icon: <AppstoreOutlined />, label: '대시보드' },
  { key: '/budget', icon: <DollarOutlined />, label: '예산 관리' },
  { key: '/products', icon: <ShoppingOutlined />, label: '상품 관리' },
  { key: '/orders', icon: <ProfileOutlined />, label: '주문 내역' }
]

export default function AppLayout() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible theme="light">
        <div style={{ padding: '16px', fontWeight: 700 }}>Point Roulette</div>
        <Menu
          mode="inline"
          items={menuItems}
          selectedKeys={[location.pathname === '/' ? '/' : location.pathname]}
          onClick={(item) => navigate(item.key)}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: '#fff',
            borderBottom: '1px solid #f0f0f0',
            padding: '0 24px'
          }}
        >
          <Typography.Title level={4} style={{ margin: 0 }}>
            어드민
          </Typography.Title>
        </Header>
        <Content style={{ padding: '24px' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
