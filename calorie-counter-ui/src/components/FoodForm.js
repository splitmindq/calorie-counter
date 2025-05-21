import React, { useEffect } from 'react';
import { Modal, Form, Input, InputNumber } from 'antd';

const FoodForm = ({ visible, onCreate, onCancel, initialData }) => {
    const [form] = Form.useForm();

    useEffect(() => {
        if (initialData) {
            form.setFieldsValue(initialData);
        } else {
            form.resetFields();
        }
    }, [initialData, form, visible]);

    return (
        <Modal
            open={visible}
            title={initialData ? "Редактировать продукт" : "Создать продукт"}
            okText={initialData ? "Сохранить" : "Создать"}
            cancelText="Отмена"
            onCancel={onCancel}
            onOk={() => {
                form.validateFields()
                    .then(values => {
                        onCreate(values);
                        form.resetFields();
                    })
                    .catch(info => {
                        console.log('Validate Failed:', info);
                    });
            }}
        >
            <Form form={form} layout="vertical" name="food_form">
                <Form.Item name="name" label="Название" rules={[{ required: true,message: 'Пожалуйста, введите название продукта!' }]}>
                    <Input />
                </Form.Item>
                <Form.Item name="calories" label="Калории (на 100г)" rules={[{ required: true, type: 'number', min: 0, message: 'Калории должны быть положительным числом' }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="protein" label="Белки (г на 100г)" rules={[{ required: true, type: 'number', min: 0, message: 'Белки должны быть положительным числом' }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="fats" label="Жиры (г на 100г)" rules={[{ required: true, type: 'number', min: 0, message: 'Жиры должны быть положительным числом' }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="carbs" label="Углеводы (г на 100г)" rules={[{ required: true, type: 'number', min: 0, message: 'Углеводы должны быть положительным числом' }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default FoodForm;