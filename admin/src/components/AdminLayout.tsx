import React from 'react';
import { Layout, Menu, theme, ConfigProvider } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
    DashboardOutlined,
    DollarOutlined,
    ShoppingOutlined,
    OrderedListOutlined,
    UserOutlined,
    PlayCircleOutlined,
} from '@ant-design/icons';

const { Header, Content, Footer, Sider } = Layout;

// VoltUp Design Tokens
const voltUpTheme = {
    token: {
        colorPrimary: '#C8FF00', // Volt Green
        colorTextBase: '#1A1A1A',
        colorBgLayout: '#F5F5F5',
        fontFamily: 'Pretendard, -apple-system, BlinkMacSystemFont, system-ui, Roboto, sans-serif',
    },
    components: {
        Layout: {
            siderBg: '#1A1A1A',
            triggerBg: '#2C2C2C',
        },
        Menu: {
            darkItemBg: '#1A1A1A',
            darkItemColor: '#B0B0B0',
            darkItemSelectedBg: '#C8FF00',
            darkItemSelectedColor: '#1A1A1A',
        },
    },
};

const items = [
    { key: '/', icon: <DashboardOutlined />, label: '대시보드' },
    { key: '/budget', icon: <DollarOutlined />, label: '예산 관리' },
    { key: '/roulette', icon: <PlayCircleOutlined />, label: '룰렛 관리' },
    { key: '/products', icon: <ShoppingOutlined />, label: '상품 관리' },
    { key: '/orders', icon: <OrderedListOutlined />, label: '주문 내역' },
];

const AdminLayout: React.FC = () => {
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken();

    const navigate = useNavigate();
    const location = useLocation();

    return (
        <ConfigProvider theme={voltUpTheme}>
            <Layout style={{ minHeight: '100vh' }}>
                <Sider breakpoint="lg" collapsedWidth="0" theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <h1 style={{ color: '#C8FF00', margin: 0, fontSize: '24px', fontWeight: 'bold' }}>VoltUp</h1>
                        <p style={{ color: '#fff', fontSize: '12px', opacity: 0.7 }}>Point Roulette Admin</p>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={[location.pathname]}
                        items={items}
                        onClick={({ key }) => navigate(key)}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: 0, background: colorBgContainer, display: 'flex', alignItems: 'center', justifyContent: 'flex-end', paddingRight: '20px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <UserOutlined />
                            <span>Admin User</span>
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px 0' }}>
                        <div
                            style={{
                                padding: 24,
                                minHeight: 360,
                                background: colorBgContainer,
                                borderRadius: borderRadiusLG,
                            }}
                        >
                            <Outlet />
                        </div>
                    </Content>
                    <Footer style={{ textAlign: 'center', background: 'transparent' }}>
                        VoltUp Project ©{new Date().getFullYear()} Created by MSK226
                    </Footer>
                </Layout>
            </Layout>
        </ConfigProvider>
    );
};

export default AdminLayout;
