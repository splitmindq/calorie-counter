import React, { useState, useEffect, useCallback } from 'react';
import { Table, Button, Popconfirm, message, Spin, Alert, Input, Space, Tag} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { getAllUsers, createUser, updateUser, deleteUser } from '../../services/userService';
import UserForm from './UserForm';
import UserDailyIntakes from './UserDailyIntakes';

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [searchText, setSearchText] = useState('');
    const [expandedRowKeys, setExpandedRowKeys] = useState([]);

    const fetchUsers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await getAllUsers();
            setUsers(response.data || []);
        } catch (err) {
            setError('Не удалось загрузить пользователей.');
            console.error(err);
            setUsers([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [fetchUsers]);

    const handleAdd = () => {
        setEditingUser(null);
        setIsModalVisible(true);
    };

    const handleEdit = (user) => {
        setEditingUser(user);
        setIsModalVisible(true);
    };

    const handleDelete = async (userId) => {
        try {
            await deleteUser(userId);
            message.success('Пользователь успешно удален.');
            fetchUsers();
        } catch (err) {
            message.error('Ошибка при удалении пользователя. Возможно, он связан с рационами.');
            console.error(err);
        }
    };

    const handleCreateUser = async (values) => {
        try {
            await createUser(values);
            message.success('Пользователь успешно создан.');
            setIsModalVisible(false);
            fetchUsers();
        } catch (err) {
            if (err.response && err.response.status === 409) {
                message.error('Email уже используется.');
            } else {
                message.error('Ошибка при создании пользователя.');
            }
            console.error(err);
        }
    };

    const handleUpdateUser = async (id, values) => {
        try {
            await updateUser(id, values);
            message.success('Пользователь успешно обновлен.');
            setIsModalVisible(false);
            setEditingUser(null);
            fetchUsers();
        } catch (err) {
            if (err.response && err.response.status === 409) {
                message.error('Email уже используется.');
            } else {
                message.error('Ошибка при обновлении пользователя.');
            }
            console.error(err);
        }
    };

    const handleModalCancel = () => {
        setIsModalVisible(false);
        setEditingUser(null);
    };

    const filteredUsers = users.filter(user =>
        (user.firstName?.toLowerCase() || '').includes(searchText.toLowerCase()) ||
        (user.lastName?.toLowerCase() || '').includes(searchText.toLowerCase()) ||
        (user.email?.toLowerCase() || '').includes(searchText.toLowerCase())
    );

    const columns = [
        {
            title: 'ID',
            dataIndex: 'id',
            key: 'id',
            sorter: (a, b) => a.id - b.id,
        },
        {
            title: 'Имя',
            dataIndex: 'firstName',
            key: 'firstName',
            sorter: (a, b) => a.firstName.localeCompare(b.firstName),
        },
        {
            title: 'Фамилия',
            dataIndex: 'lastName',
            key: 'lastName',
            sorter: (a, b) => a.lastName.localeCompare(b.lastName),
        },
        {
            title: 'Email',
            dataIndex: 'email',
            key: 'email',
        },
        {
            title: 'Возраст',
            dataIndex: 'age',
            key: 'age',
        },
        {
            title: 'Пол',
            dataIndex: 'gender',
            key: 'gender',
            render: (gender) => {
                let color = 'default';
                let text = gender;
                if (gender === 'MALE') { color = 'blue'; text = 'Муж'; }
                else if (gender === 'FEMALE') { color = 'pink'; text = 'Жен'; }
                else if (gender === 'OTHER') { color = 'purple'; text = 'Другой'; }
                return gender ? <Tag color={color}>{text}</Tag> : 'N/A';
            }
        },
        {
            title: 'Действия',
            key: 'actions',
            render: (_, record) => (
                <Space size="middle" className="action-buttons-container">
                    <Button icon={<EditOutlined />} onClick={() => handleEdit(record)} size="small">
                        Ред.
                    </Button>
                    <Popconfirm
                        title="Удалить этого пользователя?"
                        description="Это действие также удалит все связанные с ним дневные рационы."
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

    if (loading) return <Spin tip="Загрузка пользователей..." />;
    if (error && users.length === 0) return <Alert message={error} type="error" showIcon />;

    return (
        <div>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
                <Input
                    placeholder="Поиск по имени, фамилии, email"
                    prefix={<SearchOutlined />}
                    style={{ width: 300 }}
                    onChange={e => setSearchText(e.target.value)}
                />
                <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                    Добавить пользователя
                </Button>
            </div>
            {error && <Alert message={error} type="warning" showIcon style={{ marginBottom: 16 }} />}
            <Table
                columns={columns}
                dataSource={filteredUsers}
                rowKey="id"
                pagination={{ pageSize: 10 }}
                expandable={{ // <--- ВОТ ЭТА СЕКЦИЯ
                    expandedRowRender: record => <UserDailyIntakes user={record} />,
                    rowExpandable: record => true,
                    expandedRowKeys: expandedRowKeys,
                    onExpand: (expanded, record) => {
                        const keys = expanded ? [...expandedRowKeys, record.id] : expandedRowKeys.filter(k => k !== record.id);
                        setExpandedRowKeys(keys);
                    }
                }}
            />
            <UserForm
                visible={isModalVisible}
                onCreate={handleCreateUser}
                onUpdate={handleUpdateUser}
                onCancel={handleModalCancel}
                editingUser={editingUser}
            />
        </div>
    );
};

export default UserList;