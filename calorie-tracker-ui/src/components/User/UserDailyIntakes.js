import React, { useState, useEffect, useCallback } from 'react';
// 1. Импорты из node_modules
import dayjs from 'dayjs';
import 'dayjs/locale/ru';
import localizedFormat from 'dayjs/plugin/localizedFormat';
import {
    Table,
    Button,
    Spin,
    Alert,
    Typography,
    Popconfirm,
    message,
    Empty,
    DatePicker,
    Space,
    Card,
    Row,
    Col,
    Statistic
} from 'antd';
import {
    EditOutlined,
    DeleteOutlined,
    PlusOutlined,
    CalendarOutlined
} from '@ant-design/icons';

// 2. Импорты из вашего проекта
import {
    getDailyIntakesByUserEmail,
    getNutritionForIntakeById,
    getTotalDailyNutritionForDate,
    deleteDailyIntake
} from '../../services/dailyIntakeService';
import DailyIntakeForm from '../DailyIntake/DailyIntakeForm';

dayjs.locale('ru');
dayjs.extend(localizedFormat);

const { Text, Title } = Typography;

const UserDailyIntakes = ({ user }) => {
    // ПЕРЕНОСИМ ВСЕ useState В НАЧАЛО КОМПОНЕНТА
    const [intakes, setIntakes] = useState([]);
    const [loadingIntakes, setLoadingIntakes] = useState(false);
    const [loadingIndividualNutrition, setLoadingIndividualNutrition] = useState(false);
    const [loadingTotalNutrition, setLoadingTotalNutrition] = useState(false);
    const [error, setError] = useState(null);
    const [individualNutritionData, setIndividualNutritionData] = useState({});
    const [totalNutritionForDisplayedDate, setTotalNutritionForDisplayedDate] = useState(null);
    const [currentDateForTotals, setCurrentDateForTotals] = useState(dayjs());
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingIntake, setEditingIntake] = useState(null);
    const [expandedRowKeys, setExpandedRowKeys] = useState([]); // Объявлен здесь

    // Теперь идут useCallback и useEffect
    const fetchTotalNutrition = useCallback(async (dateToFetch) => {
        if (!user?.email || !dateToFetch || !dayjs(dateToFetch).isValid()) {
            setTotalNutritionForDisplayedDate(null); // Используем сеттер
            return;
        }
        setLoadingTotalNutrition(true); // Используем сеттер
        try {
            const formattedDate = dayjs(dateToFetch).format('YYYY-MM-DD');
            const response = await getTotalDailyNutritionForDate(user.email, formattedDate);
            setTotalNutritionForDisplayedDate(response.data); // Используем сеттер
        } catch (err) {
            console.error(`[fetchTotalNutrition] Ошибка суммарных КБЖУ для ${dayjs(dateToFetch).format('DD.MM.YYYY')}:`, err.response || err);
            if (err.response?.status === 404) {
                setTotalNutritionForDisplayedDate(null);
            } else {
                // setError('Ошибка при загрузке суммарных КБЖУ.'); // Можно установить общую ошибку, если нужно
                setTotalNutritionForDisplayedDate({ calories: 0, protein: 0, fats: 0, carbs: 0 }); // Фоллбэк с нулями
            }
        } finally {
            setLoadingTotalNutrition(false); // Используем сеттер
        }
    }, [user?.email]);

    const fetchIntakesAndIndividualNutrition = useCallback(async () => {
        if (!user?.email) {
            setIntakes([]); // Используем сеттер
            setIndividualNutritionData({}); // Используем сеттер
            setTotalNutritionForDisplayedDate(null); // Используем сеттер
            setCurrentDateForTotals(dayjs()); // Используем сеттер
            return;
        }

        setLoadingIntakes(true); // Используем сеттер
        setLoadingIndividualNutrition(true); // Используем сеттер
        setError(null); // Используем сеттер
        let fetchedIntakes = [];

        try {
            const response = await getDailyIntakesByUserEmail(user.email);
            const intakesData = Array.isArray(response.data) ? response.data : [];
            fetchedIntakes = [...intakesData].sort((a, b) => dayjs(b.creationDate).diff(dayjs(a.creationDate)));
            setIntakes(fetchedIntakes); // Используем сеттер

            // Обновляем/инициализируем currentDateForTotals, что вызовет другой useEffect для totalNutrition
            if (fetchedIntakes.length > 0) {
                const firstIntakeDate = dayjs(fetchedIntakes[0].creationDate);
                // Устанавливаем только если отличается от текущего, чтобы избежать лишних вызовов useEffect
                if (!currentDateForTotals || !currentDateForTotals.isSame(firstIntakeDate, 'day')) {
                    setCurrentDateForTotals(firstIntakeDate); // Используем сеттер
                } else {
                    // Если дата та же, но список рационов мог обновиться (например, после удаления/добавления)
                    // нужно принудительно обновить итоги дня
                    await fetchTotalNutrition(currentDateForTotals);
                }
            } else if (!currentDateForTotals) { // Если рационов нет и дата не установлена
                setCurrentDateForTotals(dayjs()); // Устанавливаем на сегодня
            } else if (currentDateForTotals && fetchedIntakes.length === 0) {
                // Если дата была, но рационы исчезли, запросим итоги (скорее всего, получим нули)
                await fetchTotalNutrition(currentDateForTotals);
            }


            if (fetchedIntakes.length > 0) {
                const newIndividualNutritionData = {};
                const nutritionPromises = fetchedIntakes.map(intake =>
                    getNutritionForIntakeById(intake.id)
                        .then(res => { newIndividualNutritionData[intake.id] = res.data; })
                        .catch(err => {
                            console.error(`[FII] Ошибка инд. КБЖУ для ID ${intake.id}:`, err.response || err);
                            newIndividualNutritionData[intake.id] = { calories: 0, protein: 0, fats: 0, carbs: 0 };
                        })
                );
                await Promise.all(nutritionPromises);
                setIndividualNutritionData(newIndividualNutritionData); // Используем сеттер
            } else {
                setIndividualNutritionData({}); // Используем сеттер
            }

        } catch (err) {
            console.error('[FII] Ошибка загрузки данных:', err.response || err);
            if (err.response?.status === 404) { // Если список рационов не найден
                setIntakes([]);
                setIndividualNutritionData({});
                // Не сбрасываем totalNutritionForDisplayedDate здесь, так как оно может быть для другой даты
            } else {
                setError('Произошла ошибка при загрузке рационов.'); // Используем сеттер
            }
        } finally {
            setLoadingIntakes(false); // Используем сеттер
            setLoadingIndividualNutrition(false); // Используем сеттер
        }
    }, [user?.email, currentDateForTotals, fetchTotalNutrition]); // getNutritionForIntakeById убрана, так как импортирована

    useEffect(() => {
        fetchIntakesAndIndividualNutrition();
    }, [fetchIntakesAndIndividualNutrition]);

    useEffect(() => {
        if (user?.email && currentDateForTotals && dayjs(currentDateForTotals).isValid()) {
            fetchTotalNutrition(currentDateForTotals);
        } else if (user?.email && !currentDateForTotals) {
            // Если currentDateForTotals стал null (например, через allowClear DatePicker'а)
            // можно установить его на сегодня или оставить как есть (тогда итоги не загрузятся)
            //setCurrentDateForTotals(dayjs()); // Раскомментируйте, если хотите авто-возврат к сегодняшнему дню
        }
    }, [user?.email, currentDateForTotals, fetchTotalNutrition]);

    const handleDateForTotalsChange = (date) => { // date от DatePicker - это dayjs-объект или null
        setCurrentDateForTotals(date); // Используем сеттер
    };

    const handleAddIntake = () => {
        console.log("[UserDailyIntakes] handleAddIntake ВЫЗВАНА");
        setEditingIntake(null); // Используем сеттер
        setIsModalVisible(true); // Используем сеттер
    };

    const handleEditIntake = (intake) => {
        setEditingIntake(intake); // Используем сеттер
        setIsModalVisible(true); // Используем сеттер
    };

    const handleDeleteIntake = async (intakeId) => {
        console.log(`[handleDeleteIntake] Попытка удалить рацион с ID: ${intakeId}`);
        try {
            await deleteDailyIntake(intakeId);
            message.success('Рацион успешно удален.');
            // После удаления рационов, нужно обновить currentDateForTotals, если удален последний рацион на эту дату
            // и это была дата, для которой показывались итоги.
            // Проще всего просто перезапросить все.
            await fetchIntakesAndIndividualNutrition();
        } catch (err) {
            console.error(`[handleDeleteIntake] Ошибка при удалении рациона ID ${intakeId}:`, err.response || err);
            message.error('Ошибка при удалении рациона.'); // Можно конкретизировать из err.response.data
        }
    };

    const handleModalClose = (refresh) => {
        setIsModalVisible(false); // Используем сеттер
        setEditingIntake(null); // Используем сеттер
        if (refresh) {
            fetchIntakesAndIndividualNutrition();
        }
    };

    const expandedRowRender = (record) => {
        if (!record.dailyIntakeFoods || record.dailyIntakeFoods.length === 0) {
            return <Text type="secondary">Нет продуктов в этом рационе.</Text>;
        }
        const foodColumns = [
            { title: 'Продукт', dataIndex: ['food', 'name'], key: 'foodName' },
            { title: 'Вес (г)', dataIndex: 'weight', key: 'weight', render: (val) => val?.toFixed(1) || '0.0' },
            { title: 'Ккал', dataIndex: ['food', 'calories'], key: 'calories', render: (val, item) => item.food && typeof item.weight === 'number' ? ((val * item.weight) / 100).toFixed(1) : '0.0' },
            { title: 'Белки (г)', dataIndex: ['food', 'protein'], key: 'protein', render: (val, item) => item.food && typeof item.weight === 'number' ? ((val * item.weight) / 100).toFixed(1) : '0.0' },
            { title: 'Жиры (г)', dataIndex: ['food', 'fats'], key: 'fats', render: (val, item) => item.food && typeof item.weight === 'number' ? ((val * item.weight) / 100).toFixed(1) : '0.0' },
            { title: 'Углеводы (г)', dataIndex: ['food', 'carbs'], key: 'carbs', render: (val, item) => item.food && typeof item.weight === 'number' ? ((val * item.weight) / 100).toFixed(1) : '0.0' },
        ];
        return <Table columns={foodColumns} dataSource={record.dailyIntakeFoods} pagination={false} rowKey={(foodItem) => foodItem.food?.id ?? foodItem.id ?? Math.random()} size="small" />;
    };

    const columns = [
        {
            title: 'Дата',
            dataIndex: 'creationDate',
            key: 'creationDate',
            render: (date) => dayjs(date).format('DD.MM.YYYY'),
            sorter: (a, b) => dayjs(a.creationDate).unix() - dayjs(b.creationDate).unix(),
            defaultSortOrder: 'descend',
        },
        { title: 'Ккал (рацион)', key: 'ind_calories', render: (_, record) => individualNutritionData[record.id]?.calories?.toFixed(1) ?? '0.0' },
        { title: 'Белки (г) (рацион)', key: 'ind_protein', render: (_, record) => individualNutritionData[record.id]?.protein?.toFixed(1) ?? '0.0' },
        { title: 'Жиры (г) (рацион)', key: 'ind_fats', render: (_, record) => individualNutritionData[record.id]?.fats?.toFixed(1) ?? '0.0' },
        { title: 'Угл. (г) (рацион)', key: 'ind_carbs', render: (_, record) => individualNutritionData[record.id]?.carbs?.toFixed(1) ?? '0.0' },
        {
            title: 'Действия',
            key: 'actions',
            render: (_, record) => (
                <Space size="small" className="action-buttons-container">
                    <Button icon={<EditOutlined />} onClick={() => handleEditIntake(record)} size="small">
                        Ред.
                    </Button>
                    <Popconfirm
                        title="Удалить этот рацион?"
                        onConfirm={() => handleDeleteIntake(record.id)}
                        okText="Да"
                        cancelText="Нет"
                    >
                        <Button icon={<DeleteOutlined />} danger size="small">
                            Удалить
                        </Button>
                    </Popconfirm>
                </Space>
            )
        },
    ];

    const isLoading = loadingIntakes || loadingIndividualNutrition || loadingTotalNutrition;

    if (error && intakes.length === 0) { // Используем переменную состояния 'intakes'
        return <Alert message={error} type="error" showIcon />;
    }

    return (
        <Spin spinning={isLoading} tip="Загрузка данных...">
            <div style={{ padding: '10px', backgroundColor: '#f0f2f5', border: '1px solid #e0e0e0', borderRadius: '8px' }}>
                <Title level={4} style={{ marginBottom: 20, color: '#1677ff' }}>
                    Дневные рационы для {user.firstName} {user.lastName}{' '}
                    <Text type="secondary" style={{ fontSize: '0.9em' }}>
                        ({user.email})
                    </Text>
                </Title>

                <Card
                    style={{ marginBottom: 24, boxShadow: '0 2px 8px rgba(0, 0, 0, 0.09)' }}
                    title={
                        <Space align="center">
                            <CalendarOutlined style={{ color: '#1677ff' }} />
                            <Text strong>Суммарно за день:</Text>
                            <DatePicker
                                value={currentDateForTotals} // Используем переменную состояния
                                onChange={handleDateForTotalsChange}
                                format="DD.MM.YYYY"
                                placeholder="Выберите дату"
                                allowClear
                                style={{ width: 150 }}
                            />
                        </Space>
                    }
                    bordered={false}
                >
                    {(loadingTotalNutrition && !totalNutritionForDisplayedDate) ? <Spin size="small" /> : totalNutritionForDisplayedDate ? (
                        <Row gutter={[16, 16]}>
                            <Col xs={12} sm={6}>
                                <Statistic title="Калории" value={totalNutritionForDisplayedDate.calories?.toFixed(1) ?? '0.0'} suffix="ккал" valueStyle={{ color: '#cf1322' }} />
                            </Col>
                            <Col xs={12} sm={6}>
                                <Statistic title="Белки" value={totalNutritionForDisplayedDate.protein?.toFixed(1) ?? '0.0'} suffix="г" valueStyle={{ color: '#3f8600' }} />
                            </Col>
                            <Col xs={12} sm={6}>
                                <Statistic title="Жиры" value={totalNutritionForDisplayedDate.fats?.toFixed(1) ?? '0.0'} suffix="г" valueStyle={{ color: '#faad14' }} />
                            </Col>
                            <Col xs={12} sm={6}>
                                <Statistic title="Углеводы" value={totalNutritionForDisplayedDate.carbs?.toFixed(1) ?? '0.0'} suffix="г" valueStyle={{ color: '#1677ff' }} />
                            </Col>
                        </Row>
                    ) : (
                        <Text type="secondary">{currentDateForTotals ? 'Нет данных за выбранную дату.' : 'Выберите дату для просмотра итогов.'}</Text>
                    )}
                </Card>

                <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                    <Button type="primary" icon={<PlusOutlined />} onClick={handleAddIntake}>
                        Добавить рацион
                    </Button>
                </div>

                {error && intakes.length > 0 && <Alert message={error} type="warning" showIcon style={{ marginBottom: 16 }} />}
                {/* Используем переменную состояния 'intakes' */}

                <Table
                    columns={columns}
                    dataSource={intakes} // Используем переменную состояния 'intakes'
                    rowKey="id"
                    pagination={{ pageSize: 5, hideOnSinglePage: true, showSizeChanger: false }}
                    size="middle"
                    expandable={{
                        expandedRowRender,
                        rowExpandable: (record) => record.dailyIntakeFoods && record.dailyIntakeFoods.length > 0,
                        expandedRowKeys: expandedRowKeys, // Используем переменную состояния
                        onExpand: (expanded, record) => {
                            const keys = expanded ? [...expandedRowKeys, record.id] : expandedRowKeys.filter((k) => k !== record.id);
                            setExpandedRowKeys(keys); // Используем сеттер
                        },
                    }}
                    locale={{
                        emptyText: (
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description={<Text type="secondary">Для этого пользователя еще нет записей о рационе. Нажмите "Добавить рацион", чтобы начать.</Text>}
                            />
                        ),
                    }}
                />

                {isModalVisible && ( // Используем переменную состояния
                    <DailyIntakeForm
                        open={isModalVisible} // Используем переменную состояния
                        onClose={handleModalClose}
                        userId={user.id}
                        editingIntake={editingIntake} // Используем переменную состояния
                        userEmail={user.email}
                        initialDate={editingIntake ? null : dayjs()} // Используем переменную состояния
                    />
                )}
            </div>
        </Spin>
    );
};

export default UserDailyIntakes;