import React, { useState, useEffect } from 'react';
import { Select, InputNumber, Button, Table, Popconfirm, message, Typography, Empty } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { getAllFoods } from '../../services/foodService';

const { Option } = Select;
const { Text } = Typography;

const DailyIntakeFoodManager = ({ value = [], onChange }) => {
    const [allFoods, setAllFoods] = useState([]);
    const [selectedFoodId, setSelectedFoodId] = useState(null);
    const [currentWeight, setCurrentWeight] = useState(100); // Default weight

    useEffect(() => {
        const fetchFoods = async () => {
            try {
                const response = await getAllFoods();
                setAllFoods(response.data || []);
            } catch (error) {
                message.error('Не удалось загрузить список продуктов.');
                console.error("Error fetching foods for manager:", error);
            }
        };
        fetchFoods();
    }, []);

    const handleAddFoodEntry = () => {
        if (!selectedFoodId || currentWeight <= 0) {
            message.warning('Выберите продукт и укажите корректный вес.');
            return;
        }

        const foodToAdd = allFoods.find(f => f.id === selectedFoodId);
        if (!foodToAdd) {
            message.error('Выбранный продукт не найден.');
            return;
        }

        const existingEntryIndex = value.findIndex(entry => entry.foodId === selectedFoodId);

        let newEntries;
        if (existingEntryIndex > -1) {
            newEntries = [...value];
            newEntries[existingEntryIndex] = {
                ...newEntries[existingEntryIndex],
                weight: newEntries[existingEntryIndex].weight + currentWeight,
                food: newEntries[existingEntryIndex].food || foodToAdd
            };
            message.info(`Вес продукта "${foodToAdd.name}" обновлен.`);
        } else {
            newEntries = [
                ...value,
                {
                    key: foodToAdd.id,
                    foodId: foodToAdd.id,
                    weight: currentWeight,
                    food: foodToAdd,
                },
            ];
        }

        onChange?.(newEntries);
        setSelectedFoodId(null);
        setCurrentWeight(100);
    };

    const handleRemoveFoodEntry = (foodIdToRemove) => {
        const newEntries = value.filter(entry => entry.foodId !== foodIdToRemove);
        onChange?.(newEntries);
    };

    const handleWeightChange = (foodIdToUpdate, newWeight) => {
        if (newWeight === null || newWeight <= 0) {
            message.warning("Вес должен быть больше нуля. Для удаления используйте кнопку 'Удалить'.");
            return;
        }
        const newEntries = value.map(entry =>
            entry.foodId === foodIdToUpdate ? { ...entry, weight: newWeight } : entry
        );
        onChange?.(newEntries);
    };

    const columns = [
        {
            title: 'Продукт',
            dataIndex: ['food', 'name'],
            key: 'name',
            render: (name, record) => name || allFoods.find(f => f.id === record.foodId)?.name || 'Загрузка...'
        },
        {
            title: 'Вес (г)',
            dataIndex: 'weight',
            key: 'weight',
            width: '150px',
            render: (weight, record) => (
                <InputNumber
                    min={0.1}
                    value={weight}
                    onChange={(val) => handleWeightChange(record.foodId, val)}
                    style={{ width: '100px' }}
                    parser={value => (value ? parseFloat(value) : null)} // Предотвращаем некорректный ввод
                />
            ),
        },
        {
            title: 'Ккал',
            key: 'calories',
            render: (_, record) => {
                const foodDetails = record.food || allFoods.find(f => f.id === record.foodId);
                return foodDetails ? ((foodDetails.calories * record.weight) / 100).toFixed(1) : 'N/A';
            }
        },
        {
            title: 'Действие',
            key: 'action',
            width: '100px',
            render: (_, record) => (
                <Popconfirm title="Удалить этот продукт из рациона?" onConfirm={() => handleRemoveFoodEntry(record.foodId)}>
                    <Button type="link" danger icon={<DeleteOutlined />} />
                </Popconfirm>
            ),
        },
    ];

    return (
        <div>
            <div style={{ display: 'flex', marginBottom: 16, gap: '8px', alignItems: 'center' }}>
                <Select
                    showSearch
                    style={{ flexGrow: 1 }}
                    placeholder="Выберите продукт"
                    optionFilterProp="children"
                    value={selectedFoodId}
                    onChange={setSelectedFoodId}
                    filterOption={(input, option) =>
                        option.children.toLowerCase().includes(input.toLowerCase())
                    }
                >
                    {allFoods.map(food => (
                        <Option key={food.id} value={food.id}>{food.name}</Option>
                    ))}
                </Select>
                <InputNumber
                    min={0.1}
                    value={currentWeight}
                    onChange={(val) => setCurrentWeight(val || 100)} // Устанавливаем 100, если поле пустое
                    addonAfter="г"
                    style={{ width: '120px' }}
                />
                <Button type="primary" onClick={handleAddFoodEntry} icon={<PlusOutlined />}>
                    Добавить
                </Button>
            </div>
            <Table
                columns={columns}
                dataSource={value}
                rowKey={record => record.foodId}
                pagination={false}
                size="small"
                locale={{ emptyText: <Empty description="Продукты не добавлены в рацион" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
            />
            {value.length > 0 && (
                <div style={{ marginTop: '16px', textAlign: 'right', borderTop: '1px solid #f0f0f0', paddingTop: '10px' }}>
                    <Typography.Title level={5}>Итого за прием пищи:</Typography.Title>
                    <Text>
                        Калории: {value.reduce((acc, item) => {
                        const food = item.food || allFoods.find(f => f.id === item.foodId);
                        return acc + (food ? (food.calories * item.weight) / 100 : 0);
                    }, 0).toFixed(1)} ккал
                    </Text><br/>
                    <Text>
                        Белки: {value.reduce((acc, item) => {
                        const food = item.food || allFoods.find(f => f.id === item.foodId);
                        return acc + (food ? (food.protein * item.weight) / 100 : 0);
                    }, 0).toFixed(1)} г
                    </Text><br/>
                    <Text>
                        Жиры: {value.reduce((acc, item) => {
                        const food = item.food || allFoods.find(f => f.id === item.foodId);
                        return acc + (food ? (food.fats * item.weight) / 100 : 0);
                    }, 0).toFixed(1)} г
                    </Text><br/>
                    <Text>
                        Углеводы: {value.reduce((acc, item) => {
                        const food = item.food || allFoods.find(f => f.id === item.foodId);
                        return acc + (food ? (food.carbs * item.weight) / 100 : 0);
                    }, 0).toFixed(1)} г
                    </Text>
                </div>
            )}
        </div>
    );
};

export default DailyIntakeFoodManager;