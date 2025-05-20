import React from 'react';
import FoodList from '../components/Food/FoodList';
import { Typography } from 'antd';

const { Title } = Typography;

const FoodsPage = () => {
    return (
        <>
            <Title level={2}>Продукты</Title>
            <FoodList />
        </>
    );
};

export default FoodsPage;