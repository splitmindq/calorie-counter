import React, { useState, useEffect } from 'react';
import { Modal, Form, Select, InputNumber, Button, Table, message, Alert } from 'antd';
import { getAllFood } from '../../services/foodService';
import { updateDailyIntake, createDailyIntake } from '../../services/dailyIntakeService';

const DailyIntakeForm = ({ open, onClose, userId, editingIntake, userEmail, initialDate }) => {
    const [form] = Form.useForm();
    const [foods, setFoods] = useState([]);
    const [selectedFoods, setSelectedFoods] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Загрузка списка продуктов
    useEffect(() => {
        const fetchFoods = async () => {
            setLoading(true);
            setError(null);
            try {
                const response = await getAllFood();
                console.log('API Response:', response.data);
                const foodData = Array.isArray(response) ? response : [];
                if (foodData.length === 0) {
                    setError('Список продуктов пуст. Добавьте продукты в систему.');
                } else {
                    setFoods(foodData);
                }
            } catch (err) {
                console.error('Error fetching foods:', err.message, err.response?.data, err.response?.status);
                setError('Не удалось загрузить продукты. Проверьте подключение к серверу.');
                message.error('Не удалось загрузить продукты.');
            } finally {
                setLoading(false);
            }
        };
        fetchFoods();
    }, []);

    // Инициализация формы для редактирования
    useEffect(() => {
        if (editingIntake) {
            const initialFoods = editingIntake.dailyIntakeFoods.map(item => ({
                foodId: item.food.id,
                weight: item.weight,
                key: item.id || Math.random(),
            }));
            setSelectedFoods(initialFoods);
            form.setFieldsValue({ foods: initialFoods });
        } else {
            setSelectedFoods([]);
            form.resetFields();
        }
    }, [editingIntake, form]);

    // Добавление продукта в список
    const handleAddFood = () => {
        form.validateFields(['foodId', 'weight']).then(values => {
            const newFood = {
                foodId: values.foodId,
                weight: values.weight,
                key: Math.random(),
            };
            const isDuplicate = selectedFoods.some(
                food => food.foodId === newFood.foodId && food.weight === newFood.weight
            );
            if (isDuplicate) {
                message.warning('Этот продукт с таким весом уже добавлен.');
                return;
            }
            setSelectedFoods([...selectedFoods, newFood]);
            form.resetFields(['foodId', 'weight']);
        }).catch(() => {
            message.error('Пожалуйста, заполните все поля.');
        });
    };

    // Удаление продукта из списка
    const handleRemoveFood = (key) => {
        setSelectedFoods(selectedFoods.filter(food => food.key !== key));
    };

    // Сохранение рациона
    const handleSubmit = async () => {
        if (selectedFoods.length === 0) {
            message.error('Нельзя сохранить пустой рацион. Добавьте хотя бы один продукт.');
            return;
        }
        setLoading(true);
        setError(null); // Сбрасываем предыдущие ошибки формы
        try {
            if (editingIntake && editingIntake.id) {
                // ******** ИСПРАВЛЕНИЕ ДЛЯ UPDATE ***********
                const updatePayload = {
                    foodIds: selectedFoods.map(item => item.foodId),
                    weights: selectedFoods.map(item => item.weight)
                };
                console.log('Обновление рациона ID:', editingIntake.id, 'Payload:', updatePayload);
                await updateDailyIntake(editingIntake.id, updatePayload);
                message.success('Рацион успешно обновлён.');
                // ******** КОНЕЦ ИСПРАВЛЕНИЯ ДЛЯ UPDATE ***********
            } else {
                // Создание нового рациона
                if (!userId) {
                    message.error("ID пользователя не определен.");
                    setLoading(false);
                    return;
                }
                const createPayload = {
                    userId: userId,
                    // Если бэкенд сам ставит текущую дату (Сценарий 1 из предыдущих ответов):
                    foodEntries: selectedFoods.map(item => ({
                        foodId: item.foodId,
                        weight: item.weight
                    })),
                    // Если бэкенд ТРЕБУЕТ дату от фронтенда (Сценарий 2):
                    // date: (initialDate ? dayjs(initialDate) : dayjs()).format('YYYY-MM-DD'),
                    // foodEntries: selectedFoodItems.map(item => ({ foodId: item.foodId, weight: item.weight })),
                };
                console.log('Создание рациона, Payload:', createPayload);
                await createDailyIntake(createPayload);
                message.success('Рацион успешно создан.');
            }
            onClose(true); // Закрыть и обновить список
        } catch (err) {
            const errorMsg = err.response?.data?.message || err.response?.data || err.message || 'Ошибка при сохранении рациона.';
            setError(errorMsg); // Показать ошибку в Alert
            message.error(errorMsg); // Показать ошибку в message.error
            console.error('Error saving intake:', err.response || err);
        } finally {
            setLoading(false);
        }
    };

    const columns = [
        {
            title: 'Продукт',
            dataIndex: 'foodId',
            key: 'foodId',
            render: (foodId) => foods.find(food => food.id === foodId)?.name || 'N/A',
        },
        {
            title: 'Вес (г)',
            dataIndex: 'weight',
            key: 'weight',
            render: (weight) => weight.toFixed(1),
        },
        {
            title: 'Ккал',
            key: 'calories',
            render: (_, record) => {
                const food = foods.find(f => f.id === record.foodId);
                return food && record.weight ? ((food.calories * record.weight) / 100).toFixed(1) : '0.0';
            },
        },
        {
            title: 'Действие',
            key: 'action',
            render: (_, record) => (
                <Button type="link" onClick={() => handleRemoveFood(record.key)}>
                    Удалить
                </Button>
            ),
        },
    ];

    const totals = selectedFoods.reduce(
        (acc, item) => {
            const food = foods.find(f => f.id === item.foodId);
            if (food && item.weight) {
                acc.calories += (food.calories * item.weight) / 100;
                acc.protein += (food.protein * item.weight) / 100;
                acc.fats += (food.fats * item.weight) / 100;
                acc.carbs += (food.carbs * item.weight) / 100;
            }
            return acc;
        },
        { calories: 0, protein: 0, fats: 0, carbs: 0 }
    );

    return (
        <Modal
            title={editingIntake ? 'Редактировать рацион' : 'Добавить рацион'}
            open={open}
            onOk={handleSubmit}
            onCancel={() => onClose(false)}
            confirmLoading={loading}
            okText={editingIntake ? 'Сохранить' : 'Создать'}
            cancelText="Отмена"
        >
            {error && <Alert message={error} type="error" showIcon style={{ marginBottom: 16 }} />}
            <Form form={form} layout="vertical">
                <Form.Item
                    name="foodId"
                    label="Выберите продукт"
                    rules={[{ required: true, message: 'Пожалуйста, выберите продукт' }]}
                >
                    <Select
                        placeholder="Выберите продукт"
                        options={foods.map(food => ({ value: food.id, label: food.name }))}
                        loading={loading}
                        disabled={loading || foods.length === 0}
                    />
                </Form.Item>
                <Form.Item
                    name="weight"
                    label="Вес (г)"
                    rules={[{ required: true, message: 'Пожалуйста, укажите вес' }]}
                >
                    <InputNumber min={1} placeholder="Вес в граммах" style={{ width: '100%' }} />
                </Form.Item>
                <Button type="primary" onClick={handleAddFood} disabled={loading || foods.length === 0}>
                    Добавить продукт
                </Button>
            </Form>
            <Table
                dataSource={selectedFoods}
                columns={columns}
                pagination={false}
                rowKey="key"
                style={{ marginTop: 16 }}
                locale={{ emptyText: 'Нет добавленных продуктов' }}
            />
            <div style={{ marginTop: 16 }}>
                <strong>Итого за приём пищи:</strong>
                <p>Калории: {totals.calories.toFixed(1)} ккал</p>
                <p>Белки: {totals.protein.toFixed(1)} г</p>
                <p>Жиры: {totals.fats.toFixed(1)} г</p>
                <p>Углеводы: {totals.carbs.toFixed(1)} г</p>
            </div>
        </Modal>
    );
};

export default DailyIntakeForm;