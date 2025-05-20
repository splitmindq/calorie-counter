import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import { ConfigProvider } from 'antd';
import ruRU from 'antd/lib/locale/ru_RU'; // Импорт русской локализации
import dayjs from 'dayjs';
import 'dayjs/locale/ru'; // Импорт русской локали для dayjs

dayjs.locale('ru'); // Установка русской локали для dayjs глобально

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <ConfigProvider locale={ruRU} theme={{ token: { colorPrimary: '#1677ff' } }}>
            <App />
        </ConfigProvider>
    </React.StrictMode>
);