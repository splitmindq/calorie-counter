import React, { useState, useEffect, useCallback } from 'react';
import { Table, Button, Popconfirm, message, Space, Typography } from 'antd';
import { getFoods, createFood, updateFood, deleteFood } from '../api';
import FoodForm from '../components/FoodForm';

const { Title } = Typography;

const FoodsPage = () => {
    const [foods, setFoods] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingFood, setEditingFood] = useState(null);

    const fetchFoods = useCallback(async () => {
        setLoading(true);
        try {
            const response = await getFoods();
            setFoods(response.data || []);
        } catch (error) {
            message.error('Ошибка загрузки продуктов');
            console.error("Error fetching foods", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchFoods();
    }, [fetchFoods]);

    const handleCreate = async (values) => {
        try {
            await createFood(values);
            message.success('Продукт успешно создан');
            setIsModalVisible(false);
            fetchFoods();
        } catch (error) {
            message.error(error.response?.data || 'Ошибка создания продукта');
            console.error("Error creating food", error);
        }
    };

    const handleUpdate = async (values) => {
        try {
            await updateFood(editingFood.id, values);
            message.success('Продукт успешно обновлен');
            setIsModalVisible(false);
            setEditingFood(null);
            fetchFoods();
        } catch (error) {
            message.error(error.response?.data || 'Ошибка обновления продукта');
            console.error("Error updating food", error);
        }
    };

    const handleDelete = async (id) => {
        try {
            await deleteFood(id);
            message.success('Продукт успешно удален');
            fetchFoods();
        } catch (error) {
            message.error(error.response?.data || 'Ошибка удаления продукта');
            console.error("Error deleting food", error);
        }
    };

    const columns = [
        { title: 'ID',align:"center", dataIndex: 'id', key: 'id', sorter: (a,b) => a.id - b.id },
        { title: 'Название',align:"center", dataIndex: 'name', key: 'name', sorter: (a,b) => a.name.localeCompare(b.name) },
        { title: 'Калории (100г)',align:"center", dataIndex: 'calories', key: 'calories', sorter: (a,b) => a.calories - b.calories },
        { title: 'Белки (100г)',align:"center", dataIndex: 'protein', key: 'protein', sorter: (a,b) => a.protein - b.protein },
        { title: 'Жиры (100г)',align:"center", dataIndex: 'fats', key: 'fats', sorter: (a,b) => a.fats - b.fats },
        { title: 'Углеводы (100г)',align:"center", dataIndex: 'carbs', key: 'carbs', sorter: (a,b) => a.carbs - b.carbs },
        {
            title: 'Действия',
            key: 'actions',
            align: 'center',
            width: 260,
// <--- ДОБАВЬТЕ ЭТО
            render: (_, record) => (
                <Space size="middle">
                    <Button type="link" onClick={() => { setEditingFood(record); setIsModalVisible(true); }}>Редактировать</Button>
                    <Popconfirm title="Удалить этот продукт?" onConfirm={() => handleDelete(record.id)} okText="Да" cancelText="Нет">
                        <Button type="link" danger>Удалить</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    return (
        <div>
            <Title level={2}>Продукты</Title>
            <Button type="primary" onClick={() => { setEditingFood(null); setIsModalVisible(true); }} style={{ marginBottom: 16 }}>
                Добавить продукт
            </Button>
            <Table
                columns={columns}
                dataSource={foods.map(f => ({ ...f, key: f.id }))}
                loading={loading}
                rowKey="id"
            />
            <FoodForm
                visible={isModalVisible}
                onCreate={editingFood ? handleUpdate : handleCreate}
                onCancel={() => { setIsModalVisible(false); setEditingFood(null); }}
                initialData={editingFood}
            />
        </div>
    );
};

export default FoodsPage;