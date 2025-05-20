import React, { useEffect } from 'react';
import { Modal, Form, Input, InputNumber, Button, message } from 'antd';

const FoodForm = ({ visible, onCreate, onUpdate, onCancel, editingFood }) => {
    const [form] = Form.useForm();

    useEffect(() => {
        if (editingFood) {
            form.setFieldsValue(editingFood);
        } else {
            form.resetFields();
        }
    }, [editingFood, form, visible]);

    const handleOk = () => {
        form
            .validateFields()
            .then((values) => {
                if (editingFood) {
                    onUpdate(editingFood.id, values);
                } else {
                    onCreate(values);
                }
                form.resetFields();
            })
            .catch((info) => {
                console.log('Validate Failed:', info);
                message.error('Пожалуйста, заполните все обязательные поля корректно.');
            });
    };

    const modalTitle = editingFood ? "Редактировать продукт" : "Добавить продукт";

    return (
        <Modal
            open={visible}
            title={modalTitle}
            okText={editingFood ? "Сохранить" : "Создать"}
            cancelText="Отмена"
            onCancel={onCancel}
            onOk={handleOk}
            destroyOnHidden
        >
            <Form form={form} layout="vertical" name="food_form">
                <Form.Item
                    name="name"
                    label="Название продукта"
                    rules={[{ required: true, message: 'Пожалуйста, введите название продукта!' }]}
                >
                    <Input />
                </Form.Item>
                <Form.Item
                    name="calories"
                    label="Калории (на 100г)"
                    rules={[{ required: true, message: 'Пожалуйста, введите калорийность!' }, { type: 'number', min: 0 }]}
                >
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item
                    name="protein"
                    label="Белки (г на 100г)"
                    rules={[{ required: true, message: 'Пожалуйста, введите содержание белков!' }, { type: 'number', min: 0 }]}
                >
                    <InputNumber style={{ width: '100%' }} step={0.1} />
                </Form.Item>
                <Form.Item
                    name="fats"
                    label="Жиры (г на 100г)"
                    rules={[{ required: true, message: 'Пожалуйста, введите содержание жиров!' }, { type: 'number', min: 0 }]}
                >
                    <InputNumber style={{ width: '100%' }} step={0.1} />
                </Form.Item>
                <Form.Item
                    name="carbs"
                    label="Углеводы (г на 100г)"
                    rules={[{ required: true, message: 'Пожалуйста, введите содержание углеводов!' }, { type: 'number', min: 0 }]}
                >
                    <InputNumber style={{ width: '100%' }} step={0.1} />
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default FoodForm;