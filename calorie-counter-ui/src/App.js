import React from 'react';
import { Layout, Tabs, ConfigProvider, theme } from 'antd';
import ruRU from 'antd/locale/ru_RU';
import UsersPage from './pages/UsersPage';
import FoodsPage from './pages/FoodsPage';
// import DailyIntakesPage from './pages/DailyIntakesPage'; // Удалено

const { Header, Content, Footer } = Layout;

const App = () => {
  const items = [
    {
      key: '1',
      label: `Пользователи`,
      children: <UsersPage />,
    },
    {
      key: '2',
      label: `Продукты`,
      children: <FoodsPage />,
    },
    // { // Удалена вкладка
    //     key: '3',
    //     label: `Дневные рационы`,
    //     children: <DailyIntakesPage />,
    // },
  ];

  return (
      <ConfigProvider
          locale={ruRU}
          theme={{
            token: {
              colorPrimary: '#007bff',
            },
            algorithm: theme.defaultAlgorithm,
          }}
      >
        <Layout className="layout" style={{ minHeight: '100vh' }}> {/* minHeight: '100vh' - это хорошо, заставляет layout занимать всю высоту экрана */}
          <Header style={{ display: 'flex', alignItems: 'center', padding: '0 24px' /* Пример: можно явно задать padding, если нужно меньше */ }}>
            <div className="logo" style={{ color: 'white', fontSize: '20px' /* убрал marginRight отсюда, если он не нужен */ }}>
              Счетчик Калорий
            </div>
            {/* Если были еще элементы в Header, возможно, их отступы нужно настроить */}
          </Header>
          <Content style={{ padding: '20px 24px', /* Изменил отступы, если 50px было много */ background: '#fff' /* Добавил фон сюда для ясности */ }}>
            {/* Убрал лишний div "site-layout-content", если он не нужен для специфичных стилей */}
            <Tabs defaultActiveKey="1" items={items} centered />
          </Content>
          <Footer style={{ textAlign: 'center', padding: '12px 24px' /* Уменьшил padding футера */}}>
            Calorie Counter UI ©2024
          </Footer>
        </Layout>
      </ConfigProvider>
  );
};

export default App;