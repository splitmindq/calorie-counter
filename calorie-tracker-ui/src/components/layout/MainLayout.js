import React, { useState } from 'react';
import { Layout, Menu, Typography } from 'antd';
import { UserOutlined, AppleOutlined, ForkOutlined } from '@ant-design/icons';
import { Link, useLocation } from 'react-router-dom';

const { Header, Content, Footer, Sider } = Layout;
const { Title } = Typography;

const MainLayout = ({ children }) => {
    const [collapsed, setCollapsed] = useState(false);
    const location = useLocation();

    const menuItems = [
        {
            key: '/users',
            icon: <UserOutlined />,
            label: <Link to="/users">Пользователи</Link>,
        },
        {
            key: '/foods',
            icon: <AppleOutlined />,
            label: <Link to="/foods">Продукты</Link>,
        },
    ];

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider collapsible collapsed={collapsed} onCollapse={(value) => setCollapsed(value)} theme="dark">
                <div style={{ height: '32px', margin: '16px', background: 'rgba(255, 255, 255, 0.2)', borderRadius: '6px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    {!collapsed && <ForkOutlined style={{ fontSize: '20px', color: '#fff' }} />}
                </div>
                <Menu theme="dark" selectedKeys={[location.pathname]} mode="inline" items={menuItems} />
            </Sider>
            <Layout>
                <Header style={{ padding: '0 16px', background: '#fff', display: 'flex', alignItems: 'center' }}>
                    <Title level={3} style={{ margin: 0, color: '#1677ff' }}>Трекер Калорий</Title>
                </Header>
                <Content style={{ margin: '24px 16px 0', overflow: 'initial' }}>
                    <div style={{ padding: 24, background: '#fff', minHeight: 360 }}>
                        {children}
                    </div>
                </Content>
                <Footer style={{ textAlign: 'center', background: '#f0f2f5' }}>
                    Calorie Tracker UI ©{new Date().getFullYear()} Created with Ant Design
                </Footer>
            </Layout>
        </Layout>
    );
};

export default MainLayout;