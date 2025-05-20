import React from 'react';
import UserList from '../components/User/UserList';
import { Typography } from 'antd';

const { Title } = Typography;

const UsersPage = () => {
    return (
        <>
            <Title level={2}>Пользователи</Title>
            <UserList />
        </>
    );
};

export default UsersPage;