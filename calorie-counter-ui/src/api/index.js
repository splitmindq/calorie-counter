// src/api/index.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// User API
export const getUsers = () => apiClient.get('/users'); // Будет возвращать пользователей с их рационами
export const createUser = (data) => apiClient.post('/users/save_user', data);
export const updateUser = (id, data) => apiClient.put(`/users/update_user/${id}`, data);
export const deleteUser = (id) => apiClient.delete(`/users/delete_user/${id}`);
export const getUserById = (id) => apiClient.get(`/users/${id}`); // Используется для получения обновленных данных пользователя с рационами

// Food API
export const getFoods = () => apiClient.get('/foods');
export const createFood = (data) => apiClient.post('/foods/create_food', data);
export const updateFood = (id, data) => apiClient.put(`/foods/update_food/${id}`, data);
export const deleteFood = (id) => apiClient.delete(`/foods/delete_food/${id}`);
// getFoodById не используется в UI напрямую, но оставим, если понадобится в будущем

// Daily Intake API (в контексте пользователя)
// Создание рациона: эндпоинт /daily_intakes/create_intake принимает userId в теле, это подходит
export const createDailyIntake = (data) => apiClient.post('/daily_intakes/create_intake', data);
// Обновление рациона: эндпоинт /daily_intakes/update_intake/{id} принимает ID рациона
export const updateDailyIntake = (id, data) => apiClient.patch(`/daily_intakes/update_intake/${id}`, data);
// Удаление рациона: эндпоинт /daily_intakes/delete_intake/{id} принимает ID рациона
export const deleteDailyIntake = (id) => apiClient.delete(`/daily_intakes/delete_intake/${id}`);

// Nutrition API (остаются как есть, так как они привязаны к email/date или intakeId)
export const getDailyNutrition = (params) => apiClient.get('/daily_intakes/nutrition', { params });
export const getNutritionForIntake = (intakeId) => apiClient.get(`/daily_intakes/${intakeId}/nutrition`);