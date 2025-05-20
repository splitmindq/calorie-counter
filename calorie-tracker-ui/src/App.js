import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import UsersPage from './pages/UsersPage';
import FoodsPage from './pages/FoodsPage';
import './App.css';
import 'antd/dist/reset.css'; // Сброс стилей Ant Design

function App() {
  return (
      <Router>
        <MainLayout>
          <Routes>
            <Route path="/" element={<Navigate to="/users" replace />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/foods" element={<FoodsPage />} />
          </Routes>
        </MainLayout>
      </Router>
  );
}

export default App;