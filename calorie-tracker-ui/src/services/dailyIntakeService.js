import apiClient from '../api';
import dayjs from 'dayjs'; // Убедитесь, что dayjs установлен и импортирован

/**
 * Получает список дневных рационов для пользователя.
 * Можно фильтровать по дате. Если дата не указана, возвращает все рационы пользователя.
 * @param {string} email - Email пользователя.
 * @param {string | Object | null} [date=null] - Дата в формате 'YYYY-MM-DD' или объект Dayjs, или null.
 * @returns {Promise} Promise с ответом от API.
 */
export const getDailyIntakesByUserEmail = (email, date = null) => {
    if (!email) {
        console.error('getDailyIntakesByUserEmail вызван без email');
        return Promise.reject(new Error('Email пользователя обязателен'));
    }

    let url = `/daily_intakes/filter?email=${encodeURIComponent(email)}`;
    if (date) {
        const formattedDate = dayjs(date).isValid()
            ? dayjs(date).format('YYYY-MM-DD')
            : null;
        if (formattedDate) {
            url += `&date=${formattedDate}`;
        } else if (date) {
            console.warn(`getDailyIntakesByUserEmail: Передана невалидная дата: ${date}. Запрос будет выполнен без даты.`);
        }
    }
    return apiClient.get(url);
};

/**
 * Получает КБЖУ для конкретного рациона по его ID.
 * Вызывает эндпоинт: GET /api/v1/daily_intakes/{intakeId}/nutrition
 * @param {number | string} intakeId - ID рациона.
 * @returns {Promise} Promise с ответом от API (ожидается DailyNutritionDto).
 */
export const getNutritionForIntakeById = (intakeId) => {
    if (!intakeId && intakeId !== 0) {
        console.error('getNutritionForIntakeById вызван без intakeId');
        return Promise.reject(new Error('ID рациона обязателен'));
    }
    return apiClient.get(`/daily_intakes/${intakeId}/nutrition`);
};

/**
 * Получает суммарные КБЖУ для пользователя за указанный день.
 * Вызывает эндпоинт: GET /api/v1/daily_intakes/nutrition?email={email}&date={date}
 * @param {string} email - Email пользователя.
 * @param {string | Object} date - Дата в формате 'YYYY-MM-DD' или объект Dayjs.
 * @returns {Promise} Promise с ответом от API (ожидается DailyNutritionDto).
 */
export const getTotalDailyNutritionForDate = (email, date) => {
    if (!email) {
        console.error('getTotalDailyNutritionForDate вызван без email');
        return Promise.reject(new Error('Email пользователя обязателен'));
    }
    if (!date) {
        console.error('getTotalDailyNutritionForDate вызван без даты');
        return Promise.reject(new Error('Дата обязательна'));
    }

    const formattedDate = dayjs(date).isValid()
        ? dayjs(date).format('YYYY-MM-DD')
        : null;

    if (!formattedDate) {
        console.error(`getTotalDailyNutritionForDate: Передана невалидная дата: ${date}`);
        return Promise.reject(new Error('Передана невалидная дата'));
    }

    return apiClient.get(`/daily_intakes/nutrition?email=${encodeURIComponent(email)}&date=${formattedDate}`);
};

/**
 * Создаёт новый дневной рацион.
 * @param {Object} data - Данные рациона (userId, foodEntries).
 * @returns {Promise} Promise с ответом от API.
 */
export const createDailyIntake = async (data) => {
    console.log('Creating daily intake with data:', data);
    try {
        const response = await apiClient.post('/daily_intakes/create_intake', data);
        console.log('Created intake:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error creating intake:', error.message, error.response?.data, error.response?.status);
        throw error;
    }
};

/**
 * Получает данные конкретного дневного рациона по его ID.
 * @param {number | string} id - ID рациона.
 * @returns {Promise} Promise с ответом от API.
 */
export const getDailyIntakeById = (id) => {
    if (!id && id !== 0) {
        console.error('getDailyIntakeById вызван без id');
        return Promise.reject(new Error('ID рациона обязателен'));
    }
    return apiClient.get(`/daily_intakes/${id}`);
};

/**
 * Обновляет существующий дневной рацион.
 * @param {number | string} id - ID рациона.
 * @param {Object} data - Данные рациона (userId, foodEntries).
 * @returns {Promise} Promise с ответом от API.
 */
export const updateDailyIntake = async (id, data) => {
    console.log('Updating daily intake with id:', id, 'and data:', data);
    try {
        const response = await apiClient.patch(`/daily_intakes/update_intake/${id}`, data);
        console.log('Updated intake:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error updating intake:', error.message, error.response?.data, error.response?.status);
        throw error;
    }
};

/**
 * Удаляет запись о дневном рационе по его ID.
 * @param {number | string} id - ID рациона для удаления.
 * @returns {Promise} Promise с ответом от API.
 */
export const deleteDailyIntake = (id) => {
    if (!id && id !== 0) {
        console.error('deleteDailyIntake вызван без id');
        return Promise.reject(new Error('ID рациона обязателен'));
    }
    return apiClient.delete(`/daily_intakes/delete_intake/${id}`);
};