import React, { useEffect } from 'react';
import { Modal, Form, Input, InputNumber, Select, Button, message } from 'antd';

const { Option } = Select;

const UserForm = ({ visible, onCreate, onUpdate, onCancel, editingUser }) => {
    const [form] = Form.useForm();

    useEffect(() => {
        if (editingUser) {
            form.setFieldsValue(editingUser);
        } else {
            form.resetFields();
        }
    }, [editingUser, form, visible]);

    const handleOk = () => {
        form
            .validateFields()
            .then((values) => {
                if (editingUser) {
                    onUpdate(editingUser.id, values);
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

    const modalTitle = editingUser ? "Редактировать пользователя" : "Добавить пользователя";

    return (
        <Modal
            open={visible}
            title={modalTitle}
            okText={editingUser ? "Сохранить" : "Создать"}
            cancelText="Отмена"
            onCancel={onCancel}
            onOk={handleOk}
            destroyOnHidden
        >
            <Form form={form} layout="vertical" name="user_form">
                <Form.Item
                    name="firstName"
                    label="Имя"
                    rules={[{ required: true, message: 'Пожалуйста, введите имя!' }]}
                >
                    <Input />
                </Form.Item>
                <Form.Item
                    name="lastName"
                    label="Фамилия"
                    rules={[{ required: true, message: 'Пожалуйста, введите фамилию!' }]}
                >
                    <Input />
                </Form.Item>
                <Form.Item
                    name="email"
                    label="Email"
                    rules={[
                        { required: true, message: 'Пожалуйста, введите email!' },
                        { type: 'email', message: 'Некорректный формат email!' }
                    ]}
                >
                    <Input />
                </Form.Item>
                <Form.Item name="age" label="Возраст" rules={[{ type: 'number', min: 0, max: 150 }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="gender" label="Пол">
                    <Select placeholder="Выберите пол">
                        <Option value="MALE">Мужской</Option>
                        <Option value="FEMALE">Женский</Option>
                        <Option value="OTHER">Другой</Option>
                    </Select>
                </Form.Item>
                <Form.Item name="weight" label="Вес (кг)" rules={[{ type: 'number', min: 0 }]}>
                    <InputNumber style={{ width: '100%' }} step={0.1} />
                </Form.Item>
                <Form.Item name="height" label="Рост (см)" rules={[{ type: 'number', min: 0 }]}>
                    <InputNumber style={{ width: '100%' }} />
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default UserForm;