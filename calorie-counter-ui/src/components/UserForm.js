import React, { useEffect } from 'react';
import {Modal, Form, Input, InputNumber, Select, Col, Row} from 'antd';

const UserForm = ({ visible, onCreate, onCancel, initialData }) => {
    const [form] = Form.useForm();

    // src/components/UserForm.js
  useEffect(() => {
      if (visible) {
          if (initialData) {
              console.log("UserForm (UF) initialData:", JSON.parse(JSON.stringify(initialData)));
              form.setFieldsValue(initialData); // Это пока не будет работать
          } else {
              console.log("UF: No initialData, resetting fields.");
              form.resetFields();
          }
      }
  }, [visible, initialData, form]);

    return (
        <Modal
            open={visible}
            title={initialData ? "Редактировать пользователя" : "Создать пользователя"}
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
            <Form form={form} layout="vertical" name="user_form_in_modal">
                {/* Ряд для Имени и Фамилии */}
                <Row gutter={16}> {/* gutter - отступ между колонками */}
                    <Col xs={24} sm={12}> {/* На мобильных - полная ширина, на десктопах - половина */}
                        <Form.Item
                            name="firstName" // <--- ДОЛЖНО БЫТЬ ТОЧНО "firstName" (с учетом регистра)
                            label="Имя"
                            rules={[{ required: true, message: 'Пожалуйста, введите имя!' }]}
                            style={{ marginBottom: 12 }} // Уменьшаем нижний отступ
                        >
                            <Input
                                size="middle"
                                defaultValue={initialData ? initialData.firstName : ""}
                            />
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                        <Form.Item
                            name="lastName"
                            label="Фамилия"
                            rules={[{ required: true, message: 'Пожалуйста, введите фамилию!' }]}
                            style={{ marginBottom: 12 }}
                        >
                            <Input size="middle" />
                        </Form.Item>
                    </Col>
                </Row>

                {/* Email - занимает всю ширину */}
                <Form.Item
                    name="email"
                    label="Email"
                    rules={[{ required: true, type: 'email', message: 'Пожалуйста, введите корректный email!' }]}
                    style={{ marginBottom: 12 }}
                >
                    <Input size="middle" />
                </Form.Item>

                {/* Ряд для Возраста и Пола */}
                <Row gutter={16}>
                    <Col xs={24} sm={12}>
                        <Form.Item
                            name="age"
                            label="Возраст"
                            rules={[{ type: 'number', min: 0, message: 'Возраст должен быть положительным числом' }]}
                            style={{ marginBottom: 12 }}
                        >
                            <InputNumber size="middle" style={{ width: '100%' }} />
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                        <Form.Item
                            name="gender"
                            label="Пол"
                            rules={[{ required: true, message: 'Пожалуйста, укажите пол!'}]}
                            style={{ marginBottom: 12 }}
                        >
                            <Select size="middle" placeholder="Выберите пол">
                                <Select.Option value="Male">Мужской</Select.Option>
                                <Select.Option value="Female">Женский</Select.Option>
                                <Select.Option value="Other">Другой</Select.Option>
                            </Select>
                        </Form.Item>
                    </Col>
                </Row>

                {/* Ряд для Веса и Роста */}
                <Row gutter={16}>
                    <Col xs={24} sm={12}>
                        <Form.Item
                            name="weight"
                            label="Вес (кг)"
                            rules={[{ type: 'number', min: 0, message: 'Вес должен быть положительным числом' }]}
                            style={{ marginBottom: 12 }}
                        >
                            <InputNumber size="middle" style={{ width: '100%' }} />
                        </Form.Item>
                    </Col>
                    <Col xs={24} sm={12}>
                        <Form.Item
                            name="height"
                            label="Рост (см)"
                            rules={[{ type: 'number', min: 0, message: 'Рост должен быть положительным числом' }]}
                            // Для последнего элемента можно убрать marginBottom, если он не нужен
                            // style={{ marginBottom: 0 }} или оставить style={{ marginBottom: 12 }}
                            style={{ marginBottom: 12 }}
                        >
                            <InputNumber size="middle" style={{ width: '100%' }} />
                        </Form.Item>
                    </Col>
                </Row>
            </Form>
        </Modal>
    );
};

export default UserForm;