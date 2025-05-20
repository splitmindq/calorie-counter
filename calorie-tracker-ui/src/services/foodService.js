import axios from 'axios';

export const getAllFood = async () => {
    const url = 'http://localhost:8080/api/v1/foods';
    console.log(`Fetching foods from ${url}`);
    try {
        const response = await axios.get(url);
        console.log('Foods fetched:', response.data);
        return response.data; // Возвращаем только данные
    } catch (error) {
        console.error('Error fetching foods:', error.message, {
            status: error.response?.status,
            data: error.response?.data,
            url,
        });
        throw new Error('Failed to fetch foods');
    }
};

export const createFood = async (foodData) => {
    const url = 'http://localhost:8080/api/v1/foods/create_food';
    console.log(`Creating food with data:`, foodData);
    try {
        const response = await axios.post(url, foodData);
        console.log('Food created:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error creating food:', error.message, {
            status: error.response?.status,
            data: error.response?.data,
            url,
        });
        throw error;
    }
};

export const updateFood = async (id, foodData) => {
    const url = `/api/v1/foods/${id}`;
    console.log(`Updating food with id: ${id}, data:`, foodData);
    try {
        const response = await axios.put(url, foodData);
        console.log('Food updated:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error updating food:', error.message, {
            status: error.response?.status,
            data: error.response?.data,
            url,
        });
        throw error;
    }
};

export const deleteFood = async (id) => {
    const url = `/api/v1/foods/delete_food/${id}`;
    console.log(`Deleting food with id: ${id}`);
    try {
        const response = await axios.delete(url);
        console.log('Food deleted:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error deleting food:', error.message, {
            status: error.response?.status,
            data: error.response?.data,
            url,
        });
        throw error;
    }
};