import React, { useState, useEffect, useCallback } from 'react';
import { Table, Button, Popconfirm, message, Spin, Alert, Input, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { getAllFood, createFood, updateFood, deleteFood } from '../../services/foodService';
import FoodForm from './FoodForm';

const FoodList = () => {
    const [foods, setFoods] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingFood, setEditingFood] = useState(null);
    const [searchText, setSearchText] = useState('');

    const fetchFoods = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getAllFood();
            setFoods(data || []);
        } catch (err) {
            setError('Не удалось загрузить продукты.');
            console.error(err);
            setFoods([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchFoods();
    }, [fetchFoods]);

    const handleAdd = () => {
        setEditingFood(null);
        setIsModalVisible(true);
    };

    const handleEdit = (food) => {
        setEditingFood(food);
        setIsModalVisible(true);
    };

    const handleDelete = async (foodId) => {
        try {
            await deleteFood(foodId);
            message.success('Продукт успешно удален.');
            await fetchFoods();
        } catch (err) {
            if (err.response && err.response.status === 409) {
                message.error('Нельзя удалить продукт, так как он используется в дневных рационах.');
            } else {
                message.error('Ошибка при удалении продукта.');
            }
            console.error(err);
        }
    };

    const handleCreateFood = async (values) => {
        try {
            await createFood(values);
            message.success('Продукт успешно создан.');
            setIsModalVisible(false);
            await fetchFoods();
        } catch (err) {
            message.error('Ошибка при создании продукта.');
            console.error(err);
        }
    };

    const handleUpdateFood = async (id, values) => {
        try {
            await updateFood(id, values);
            message.success('Продукт успешно обновлен.');
            setIsModalVisible(false);
            setEditingFood(null);
            await fetchFoods();
        } catch (err) {
            if (err.response && err.response.status === 409) {
                message.error('Продукт с таким названием уже существует.');
            } else {
                message.error('Ошибка при обновлении продукта.');
            }
            console.error(err);
        }
    };

    const handleModalCancel = () => {
        setIsModalVisible(false);
        setEditingFood(null);
    };

    const filteredFoods = foods.filter(food =>
        (food.name?.toLowerCase() || '').includes(searchText.toLowerCase())
    );

    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id', sorter: (a,b) => a.id - b.id },
        { title: 'Название', dataIndex: 'name', key: 'name', sorter: (a,b) => a.name.localeCompare(b.name) },
        { title: 'Калории', dataIndex: 'calories', key: 'calories', sorter: (a,b) => a.calories - b.calories },
        { title: 'Белки (г)', dataIndex: 'protein', key: 'protein', sorter: (a,b) => a.protein - b.protein },
        { title: 'Жиры (г)', dataIndex: 'fats', key: 'fats', sorter: (a,b) => a.fats - b.fats },
        { title: 'Углеводы (г)', dataIndex: 'carbs', key: 'carbs', sorter: (a,b) => a.carbs - b.carbs },
        {
            title: 'Действия',
            key: 'actions',
            render: (_, record) => (
                <Space size="middle" className="action-buttons-container">
                    <Button icon={<EditOutlined />} onClick={() => handleEdit(record)} size="small">
                        Ред.
                    </Button>
                    <Popconfirm
                        title="Удалить этот продукт?"
                        onConfirm={() => handleDelete(record.id)}
                        okText="Да"
                        cancelText="Нет"
                    >
                        <Button icon={<DeleteOutlined />} danger size="small">
                            Удалить
                        </Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    if (loading) return <Spin tip="Загрузка продуктов..." />;
    if (error && foods.length === 0) return <Alert message={error} type="error" showIcon />;

    return (
        <div>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                <Input
                    placeholder="Поиск по названию"
                    prefix={<SearchOutlined />}
                    style={{ width: 300 }}
                    onChange={e => setSearchText(e.target.value)}
                />
                <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                    Добавить продукт
                </Button>
            </div>
            {error && <Alert message={error} type="warning" showIcon style={{ marginBottom: 16 }} />}
            <Table columns={columns} dataSource={filteredFoods} rowKey="id" pagination={{ pageSize: 10 }} />
            <FoodForm
                visible={isModalVisible}
                onCreate={handleCreateFood}
                onUpdate={handleUpdateFood}
                onCancel={handleModalCancel}
                editingFood={editingFood}
            />
        </div>
    );
};

export default FoodList;