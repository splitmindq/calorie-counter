import React, { useState, useEffect, useCallback } from 'react';
import {
    Table, Button, Popconfirm, message, Space, Typography, List, Card, Tag,
    DatePicker, Row, Col, Empty, Divider
} from 'antd';
import {
    getUsers, createUser, updateUser, deleteUser, getUserById,
    createDailyIntake, updateDailyIntake, deleteDailyIntake,
    getDailyNutrition, getNutritionForIntake
} from '../api';
import UserForm from '../components/UserForm';
import DailyIntakeForm from '../components/DailyIntakeForm';
import dayjs from 'dayjs';
import 'dayjs/locale/ru'; // для локализации дат
dayjs.locale('ru');


const { Title, Text } = Typography;

const UsersPage = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [isUserModalVisible, setIsUserModalVisible] = useState(false);
    const [editingUser, setEditingUser] = useState(null);

    // Состояния для рационов
    const [isDailyIntakeModalVisible, setIsDailyIntakeModalVisible] = useState(false);
    const [editingDailyIntake, setEditingDailyIntake] = useState(null);
    const [currentUserIdForIntake, setCurrentUserIdForIntake] = useState(null); // ID пользователя для нового рациона

    // Состояния для отображения и фильтрации рационов
    const [expandedRowKeys, setExpandedRowKeys] = useState([]);
    const [selectedUserIntakes, setSelectedUserIntakes] = useState([]); // Рационы выбранного пользователя
    const [intakeFilterDate, setIntakeFilterDate] = useState(null); // Дата для фильтрации рационов
    const [dailyNutritionForUser, setDailyNutritionForUser] = useState(null);
    const [loadingUserNutrition, setLoadingUserNutrition] = useState(false);
    const [expandedIntakeNutrition, setExpandedIntakeNutrition] = useState({}); // Нутриенты для отдельного рациона

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        try {
            const response = await getUsers();
            setUsers(response.data || []);
        } catch (error) {
            message.error('Ошибка загрузки пользователей');
            console.error("Error fetching users", error);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    // Обновление данных конкретного пользователя (например, после CRUD операции с его рационом)
    const refreshUserData = async (userId) => {
        try {
            const response = await getUserById(userId);
            const updatedUser = response.data;
            setUsers(prevUsers => prevUsers.map(u => u.id === userId ? updatedUser : u));
            // Если этот пользователь был раскрыт, обновить его рационы в selectedUserIntakes
            if (expandedRowKeys.includes(userId)) {
                let intakesToDisplay = updatedUser.dailyIntakes || [];
                if (intakeFilterDate && updatedUser.id === currentUserIdForIntake) { // currentUserIdForIntake должен быть userId раскрытого юзера
                    const formattedFilterDate = dayjs(intakeFilterDate).format('YYYY-MM-DD');
                    intakesToDisplay = intakesToDisplay.filter(intake => dayjs(intake.creationDate).format('YYYY-MM-DD') === formattedFilterDate);
                }
                setSelectedUserIntakes(intakesToDisplay);
                // Также обновить дневную пищевую ценность, если дата выбрана
                if (intakeFilterDate && updatedUser.email && updatedUser.id === currentUserIdForIntake) {
                    fetchDailyNutritionForSelectedUser(updatedUser.email, intakeFilterDate, updatedUser.id);
                }
            }
        } catch (error) {
            message.error(`Ошибка обновления данных пользователя ID ${userId}`);
        }
    };


    // --- CRUD для Пользователей ---
    const handleUserCreate = async (values) => {
        try {
            await createUser(values);
            message.success('Пользователь успешно создан');
            setIsUserModalVisible(false);
            fetchUsers();
        } catch (error) {
            message.error(error.response?.data || 'Ошибка создания пользователя');
        }
    };

    const handleUserUpdate = async (values) => {
        try {
            await updateUser(editingUser.id, values);
            message.success('Пользователь успешно обновлен');
            setIsUserModalVisible(false);
            setEditingUser(null);
            fetchUsers(); // Обновляем весь список или только измененного пользователя (refreshUserData)
        } catch (error) {
            message.error(error.response?.data || 'Ошибка обновления пользователя');
        }
    };

    const handleUserDelete = async (id) => {
        try {
            await deleteUser(id);
            message.success('Пользователь успешно удален');
            fetchUsers();
            if (expandedRowKeys.includes(id)) { // Если удаленный юзер был раскрыт
                setExpandedRowKeys(prev => prev.filter(key => key !== id));
                setSelectedUserIntakes([]);
                setDailyNutritionForUser(null);
            }
        } catch (error) {
            message.error(error.response?.data || 'Ошибка удаления пользователя');
        }
    };

    // --- CRUD для Рационов ---
    const handleDailyIntakeCreate = async (values) => {
        // values уже содержит userId из формы
        const payload = {
            userId: values.userId,
            foodEntries: values.foodEntries.map(entry => ({
                foodId: entry.foodId,
                weight: entry.weight,
            })),
        };
        try {
            await createDailyIntake(payload);
            message.success('Рацион успешно создан');
            setIsDailyIntakeModalVisible(false);
            if (payload.userId) {
                refreshUserData(payload.userId); // Обновляем данные пользователя, которому добавили рацион
            }
        } catch (error) {
            message.error(error.response?.data || 'Ошибка создания рациона');
        }
    };

    const handleDailyIntakeUpdate = async (values) => {
        const payload = {
            foodIds: values.foodEntries.map(entry => entry.foodId),
            weights: values.foodEntries.map(entry => entry.weight),
        };
        try {
            await updateDailyIntake(editingDailyIntake.id, payload);
            message.success('Рацион успешно обновлен');
            setIsDailyIntakeModalVisible(false);
            setEditingDailyIntake(null);
            if (currentUserIdForIntake) { // currentUserIdForIntake должен быть ID пользователя этого рациона
                refreshUserData(currentUserIdForIntake);
            }
        } catch (error) {
            message.error(error.response?.data || 'Ошибка обновления рациона');
        }
    };

    const handleDailyIntakeDelete = async (intakeId, userId) => {
        try {
            await deleteDailyIntake(intakeId);
            message.success('Рацион успешно удален');
            if (userId) {
                refreshUserData(userId);
            }
        } catch (error) {
            message.error(error.response?.data || 'Ошибка удаления рациона');
        }
    };

    // --- Загрузка и отображение нутриентов ---
    const fetchDailyNutritionForSelectedUser = async (email, date, userId) => {
        if (!email || !date || !userId) {
            setDailyNutritionForUser(null);
            return;
        }
        // Убедимся, что запрос делается для текущего раскрытого пользователя
        if (userId !== currentUserIdForIntake) {
            setDailyNutritionForUser(null); // Сбрасываем, если пользователь сменился
            return;
        }

        setLoadingUserNutrition(true);
        try {
            const response = await getDailyNutrition({ email, date: dayjs(date).format('YYYY-MM-DD') });
            setDailyNutritionForUser(response.data);
        } catch (error) {
            if (error.response && error.response.status === 404) {
                setDailyNutritionForUser({ empty: true });
            } else {
                message.error('Ошибка загрузки дневной пищевой ценности');
                setDailyNutritionForUser(null);
            }
        } finally {
            setLoadingUserNutrition(false);
        }
    };

    const fetchIntakeNutritionInfo = async (intakeId) => {
        if (expandedIntakeNutrition[intakeId]) {
            setExpandedIntakeNutrition(prev => ({ ...prev, [intakeId]: null }));
            return;
        }
        try {
            const response = await getNutritionForIntake(intakeId);
            setExpandedIntakeNutrition(prev => ({ ...prev, [intakeId]: response.data }));
        } catch (error) {
            message.error(`Ошибка загрузки нутриентов для рациона ID ${intakeId}`);
            setExpandedIntakeNutrition(prev => ({ ...prev, [intakeId]: { error: true } }));
        }
    };


    // --- Управление раскрытием строк и фильтрацией рационов ---
    const handleExpandUser = (expanded, record) => {
        const newExpandedRowKeys = expanded ? [record.id] : []; // Только одна строка может быть раскрыта
        setExpandedRowKeys(newExpandedRowKeys);
        setIntakeFilterDate(null); // Сбрасываем фильтр по дате при раскрытии/сворачивании нового пользователя
        setDailyNutritionForUser(null);

        if (expanded) {
            setCurrentUserIdForIntake(record.id); // Устанавливаем ID текущего пользователя
            setSelectedUserIntakes(record.dailyIntakes || []);
        } else {
            setCurrentUserIdForIntake(null);
            setSelectedUserIntakes([]);
        }
    };

    const handleIntakeDateFilterChange = (date, dateString, userRecord) => {
        setIntakeFilterDate(date);
        let intakesToDisplay = userRecord.dailyIntakes || [];
        if (date) {
            const formattedFilterDate = dayjs(date).format('YYYY-MM-DD');
            intakesToDisplay = intakesToDisplay.filter(intake => dayjs(intake.creationDate).format('YYYY-MM-DD') === formattedFilterDate);
            fetchDailyNutritionForSelectedUser(userRecord.email, date, userRecord.id);
        } else {
            setDailyNutritionForUser(null); // Сбрасываем пищевую ценность, если дата не выбрана
        }
        setSelectedUserIntakes(intakesToDisplay);
    };


    const userColumns = [
        // Колонки для пользователей остаются почти такими же
        { title: 'ID', dataIndex: 'id', align: 'center', // <--- ЦЕНТРИРОВАНИЕ
            key: 'id', sorter: (a, b) => a.id - b.id },
        { title: 'Имя', dataIndex: 'firstName',             align: 'center', // <--- ЦЕНТРИРОВАНИЕ
            key: 'firstName', sorter: (a, b) => a.firstName.localeCompare(b.firstName) },
        { title: 'Фамилия', dataIndex: 'lastName',            align: 'center', // <--- ЦЕНТРИРОВАНИЕ
            key: 'lastName', sorter: (a, b) => a.lastName.localeCompare(b.lastName) },
        { title: 'Email',             align: 'center', // <--- ЦЕНТРИРОВАНИЕ
            dataIndex: 'email', key: 'email' },
        {
            title: 'Действия',
            key: 'actions',
            align: 'center', // <--- ДОБАВЬТЕ ЭТО
            width: 260,
            render: (_, record) => (
                <Space size="middle">
                    <Button type="link" onClick={() => { setEditingUser(record); setIsUserModalVisible(true); }}>Редактировать</Button>
                    <Popconfirm title="Удалить этого пользователя и все его рационы?" onConfirm={() => handleUserDelete(record.id)} okText="Да" cancelText="Нет">
                        <Button type="link" danger>Удалить</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ];

    // Компонент для рендеринга раскрытой строки пользователя
    const expandedUserRowRender = (userRecord) => {
        const intakesForDisplay = selectedUserIntakes; // Уже отфильтрованные (или все) рационы

        return (
            <Card style={{ margin: '10px 0' }}>
                <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
                    <Col>
                        <Space>
                            <Text strong>Фильтр рационов по дате:</Text>
                            <DatePicker
                                value={intakeFilterDate}
                                onChange={(date, dateString) => handleIntakeDateFilterChange(date, dateString, userRecord)}
                                placeholder="Выберите дату"
                                format="DD.MM.YYYY"
                            />
                            {intakeFilterDate && (
                                <Button onClick={() => handleIntakeDateFilterChange(null, '', userRecord)}>Сбросить дату</Button>
                            )}
                        </Space>
                    </Col>
                    <Col>
                        <Button
                            type="primary"
                            onClick={() => {
                                setEditingDailyIntake(null);
                                setCurrentUserIdForIntake(userRecord.id); // Передаем ID пользователя для нового рациона
                                setIsDailyIntakeModalVisible(true);
                            }}
                        >
                            Добавить рацион для {userRecord.firstName}
                        </Button>
                    </Col>
                </Row>

                {dailyNutritionForUser && intakeFilterDate && (
                    <Card title={`Суммарная пищевая ценность за ${dayjs(intakeFilterDate).format('DD.MM.YYYY')}`} style={{ marginBottom: 16 }} loading={loadingUserNutrition}>
                        {dailyNutritionForUser.empty ? (
                            <Text>Данные о пищевой ценности за этот день отсутствуют.</Text>
                        ) : (
                            <Space wrap>
                                <Tag color="blue">Калории: {dailyNutritionForUser.calories?.toFixed(2)}</Tag>
                                <Tag color="green">Белки: {dailyNutritionForUser.protein?.toFixed(2)} г</Tag>
                                <Tag color="orange">Жиры: {dailyNutritionForUser.fats?.toFixed(2)} г</Tag>
                                <Tag color="purple">Углеводы: {dailyNutritionForUser.carbs?.toFixed(2)} г</Tag>
                            </Space>
                        )}
                    </Card>
                )}

                <Divider orientation="left">Рационы пользователя ({intakesForDisplay.length})</Divider>

                {intakesForDisplay.length > 0 ? (
                    <List
                        itemLayout="vertical"
                        dataSource={intakesForDisplay.sort((a,b) => dayjs(b.creationDate).unix() - dayjs(a.creationDate).unix())} // Сортировка по дате, новые сверху
                        renderItem={intake => (
                            <List.Item
                                key={intake.id}
                                actions={[
                                    <Button type="link" onClick={() => {
                                        setEditingDailyIntake(intake); // Передаем весь объект intake
                                        setCurrentUserIdForIntake(userRecord.id); // ID пользователя, которому принадлежит рацион
                                        setIsDailyIntakeModalVisible(true);
                                    }}>
                                        Редактировать
                                    </Button>,
                                    <Popconfirm title="Удалить этот рацион?" onConfirm={() => handleDailyIntakeDelete(intake.id, userRecord.id)} okText="Да" cancelText="Нет">
                                        <Button type="link" danger>Удалить</Button>
                                    </Popconfirm>,
                                    <Button type="link" onClick={() => fetchIntakeNutritionInfo(intake.id)}>
                                        {expandedIntakeNutrition[intake.id] && !expandedIntakeNutrition[intake.id]?.error ? "Скрыть нутриенты" : "Нутриенты рациона"}
                                    </Button>
                                ]}
                            >
                                <List.Item.Meta
                                    title={`Рацион от ${dayjs(intake.creationDate).format('DD MMMM YYYY')}`}
                                    description={
                                        <>
                                            <List
                                                size="small"
                                                dataSource={intake.dailyIntakeFoods}
                                                renderItem={dif => (
                                                    <List.Item style={{padding: '5px 0'}}>
                                                        {dif.food.name} - {dif.weight} г
                                                        (К: {(dif.food.calories * (dif.weight / 100)).toFixed(1)},
                                                        Б: {(dif.food.protein * (dif.weight / 100)).toFixed(1)},
                                                        Ж: {(dif.food.fats * (dif.weight / 100)).toFixed(1)},
                                                        У: {(dif.food.carbs * (dif.weight / 100)).toFixed(1)})
                                                    </List.Item>
                                                )}
                                            />
                                            {expandedIntakeNutrition[intake.id] && (
                                                expandedIntakeNutrition[intake.id].error ?
                                                    <Tag color="red" style={{marginTop: 5}}>Ошибка загрузки нутриентов</Tag> :
                                                    <div style={{ marginTop: '5px' }}>
                                                        <Text strong>Суммарно по рациону:</Text><br/>
                                                        <Tag color="processing">К: {expandedIntakeNutrition[intake.id].calories?.toFixed(1)}</Tag>
                                                        <Tag color="success">Б: {expandedIntakeNutrition[intake.id].protein?.toFixed(1)}г</Tag>
                                                        <Tag color="warning">Ж: {expandedIntakeNutrition[intake.id].fats?.toFixed(1)}г</Tag>
                                                        <Tag color="magenta">У: {expandedIntakeNutrition[intake.id].carbs?.toFixed(1)}г</Tag>
                                                    </div>
                                            )}
                                        </>
                                    }
                                />
                            </List.Item>
                        )}
                    />
                ) : (
                    <Empty description="Нет рационов для отображения (проверьте фильтр по дате или добавьте новый рацион)." />
                )}
            </Card>
        );
    };


    return (
        <div>
            <Title level={2}>Пользователи и их рационы</Title>
            <Button type="primary" onClick={() => {
                setEditingUser(null); setIsUserModalVisible(true); }}
                    style={{ marginBottom: 16 }}>
                Добавить пользователя
            </Button>
            <Table
                columns={userColumns}
                dataSource={users.map(u => ({ ...u, key: u.id }))}
                loading={loading}
                rowKey="id"
                expandable={{
                    expandedRowRender: expandedUserRowRender,
                    rowExpandable: record => true, // Всегда можно раскрыть
                    expandedRowKeys: expandedRowKeys,
                    onExpand: handleExpandUser,
                }}
            />
            <UserForm
                visible={isUserModalVisible}
                onCreate={editingUser ? handleUserUpdate : handleUserCreate}
                onCancel={() => { setIsUserModalVisible(false); setEditingUser(null); }}
                initialData={editingUser}
            />
            {isDailyIntakeModalVisible && ( // Условный рендеринг формы, чтобы передавать актуальный userId
                <DailyIntakeForm
                    visible={isDailyIntakeModalVisible}
                    onCreate={editingDailyIntake ? handleDailyIntakeUpdate : handleDailyIntakeCreate}
                    onCancel={() => {
                        setIsDailyIntakeModalVisible(false);
                        setEditingDailyIntake(null);
                        // setCurrentUserIdForIntake(null); // Не сбрасываем, если форма просто закрыта
                    }}
                    initialData={editingDailyIntake}
                    userIdForNewIntake={!editingDailyIntake ? currentUserIdForIntake : null} // Передаем ID только для новых
                />
            )}
        </div>
    );
};

export default UsersPage;