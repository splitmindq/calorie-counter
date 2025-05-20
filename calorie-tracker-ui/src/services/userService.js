import apiClient from '../api';

export const getAllUsers = () => apiClient.get('/users');
export const createUser = (userData) => apiClient.post('/users/save_user', userData);
export const updateUser = (id, userData) => apiClient.put(`/users/update_user/${id}`, userData);
export const deleteUser = (id) => apiClient.delete(`/users/delete_user/${id}`);
export const getUserById = (id) => apiClient.get(`/users/${id}`);