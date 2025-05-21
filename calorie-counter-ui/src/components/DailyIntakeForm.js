import React, { useEffect, useState } from 'react';
import { Modal, Form, Select, Button, InputNumber, Space, Row, Col, message, Input } from 'antd'; // Добавил Input для скрытого поля userId
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { getFoods } from '../api';

const DailyIntakeForm = ({ visible, onCreate, onCancel, initialData, userIdForNewIntake }) => {
    const [form] = Form.useForm();
    const [foods, setFoods] = useState([]);
    const [loadingFoods, setLoadingFoods] = useState(false);

    useEffect(() => {
        const fetchFoodsData = async () => {
            if (visible) {
                setLoadingFoods(true);
                try {
                    const foodsResponse = await getFoods();
                    setFoods(foodsResponse.data || []);
                } catch (error) {
                    message.error('Ошибка загрузки продуктов');
                    console.error("Error fetching foods", error);
                } finally {
                    setLoadingFoods(false);
                }
            }
        };
        fetchFoodsData();
    }, [visible]);

    useEffect(() => {
        if (visible) {
            if (initialData && initialData.id) { // Убедимся, что initialData существует и имеет id (редактирование)
                const foodEntries = initialData.dailyIntakeFoods && Array.isArray(initialData.dailyIntakeFoods)
                    ? initialData.dailyIntakeFoods
                        .filter(dif => dif && dif.food)
                        .map(dif => ({
                            foodId: dif.food.id,
                            weight: dif.weight
                        }))
                    : [];

                // userId для редактируемого рациона берется из initialData.user.id
                // initialData приходит из UsersPage, где он берется из record.dailyIntakes,
                // который должен содержать user (согласно вашей структуре JSON от /users)
                const currentUserId = initialData.user ? initialData.user.id : null;


                form.setFieldsValue({
                    userId: currentUserId,
                    foodEntries: foodEntries.length > 0 ? foodEntries : [{ foodId: undefined, weight: undefined }],
                });

                if (!currentUserId) { // Если user не пришел с initialData для рациона
                    message.warning("Данные пользователя для этого рациона не были загружены с рационом. Проверьте структуру данных от API /users.");
                    console.warn("InitialData for editing intake is missing user object:", initialData);
                }
                if (initialData.dailyIntakeFoods && foodEntries.length !== initialData.dailyIntakeFoods.length) {
                    message.warning("Некоторые продукты в рационе не были загружены корректно. Пожалуйста, проверьте ответ API.");
                }

            } else { // Создание нового рациона
                form.resetFields();
                form.setFieldsValue({
                    userId: userIdForNewIntake,
                    foodEntries: [{ foodId: undefined, weight: undefined }]
                });
            }
        }
    }, [initialData, form, visible, userIdForNewIntake]);


    const handleFormSubmit = () => {
        form.validateFields()
            .then(values => {
                if (!values.foodEntries || values.foodEntries.length === 0) {
                    message.error('Добавьте хотя бы один продукт в рацион.');
                    return;
                }
                const allEntriesValid = values.foodEntries.every(entry => entry && entry.foodId && entry.weight > 0);
                if (!allEntriesValid) {
                    message.error('Все продукты должны быть выбраны и иметь вес больше 0.');
                    return;
                }
                // console.log("Submitting values:", values); // Для отладки
                onCreate(values);
                // form.resetFields(); // Сбрасываем после успешной отправки
                //  if (!initialData) {
                //     form.setFieldsValue({
                //         userId: userIdForNewIntake,
                //         foodEntries: [{ foodId: undefined, weight: undefined }]
                //     });
                // }
                // Сброс формы и закрытие модального окна управляется из UsersPage после успешного onCreate
            })
            .catch(info => {
                console.log('Validate Failed:', info);
            });
    };

    return (
        <Modal
            open={visible}
            title={initialData ? "Редактировать рацион" : "Создать рацион"}
            okText={initialData ? "Сохранить" : "Создать"}
            cancelText="Отмена"
            width={800}
            onCancel={() => {
                // form.resetFields(); // Можно сбрасывать здесь или при закрытии модалки из UsersPage
                onCancel();
            }}
            onOk={handleFormSubmit}
            destroyOnClose // Важно для корректного сброса состояния формы при повторном открытии
        >
            <Form form={form} layout="vertical" name="daily_intake_form_modal" initialValues={{ foodEntries: [{ foodId: undefined, weight: undefined }] }}>
                <Form.Item name="userId" hidden>
                    {/* Используем Input, а не InputNumber, т.к. это просто ID */}
                    <Input />
                </Form.Item>

                {initialData && initialData.user && (
                    <p><strong>Пользователь:</strong> {initialData.user.firstName} {initialData.user.lastName} (нельзя изменить)</p>
                )}
                {!initialData && userIdForNewIntake && form.getFieldValue('userId') && ( // Проверяем, что userId установлен
                    <p><strong>Рацион для пользователя ID:</strong> {form.getFieldValue('userId')}</p>
                )}

                <Form.List name="foodEntries">
                    {(fields, { add, remove }) => (
                        <>
                            {fields.map(({ key, name, ...restField }) => (
                                <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                                    <Row gutter={8} style={{ width: '100%' }}>
                                        <Col flex="auto">
                                            <Form.Item
                                                {...restField}
                                                name={[name, 'foodId']}
                                                rules={[{ required: true, message: 'Выберите продукт' }]}
                                                style={{ minWidth: '300px' }}
                                            >
                                                <Select
                                                    loading={loadingFoods}
                                                    showSearch
                                                    placeholder="Продукт"
                                                    optionFilterProp="children"
                                                    filterOption={(input, option) =>
                                                        (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                                                    }
                                                    options={foods.map(food => ({ value: food.id, label: food.name }))}
                                                />
                                            </Form.Item>
                                        </Col>
                                        <Col flex="150px">
                                            <Form.Item
                                                {...restField}
                                                name={[name, 'weight']}
                                                rules={[{ required: true, type: 'number', min: 0.1, message: 'Введите вес (>0)' }]}
                                            >
                                                <InputNumber placeholder="Вес (г)" style={{ width: '100%' }} />
                                            </Form.Item>
                                        </Col>
                                        <Col>
                                            {fields.length > 1 ? (
                                                <MinusCircleOutlined onClick={() => remove(name)} style={{ fontSize: '18px', color: 'red' }} />
                                            ) : null}
                                        </Col>
                                    </Row>
                                </Space>
                            ))}
                            <Form.Item>
                                <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                                    Добавить продукт
                                </Button>
                            </Form.Item>
                        </>
                    )}
                </Form.List>
            </Form>
        </Modal>
    );
};

export default DailyIntakeForm;