import api from './axiosInstance';

export const hotelApi = {
  getHotels: (params = {}) => api.get('/api/hotels/searchBy', { params }),
  searchHotels: (params = {}) => api.get('/api/hotels/search', { params }),
  findByName: (name) => api.get('/api/hotels/find', { params: { name } }),
  getHotel: (id) => api.get(`/api/hotels/${id}`),
  getRooms: (hotelId) => api.get(`/api/hotels/${hotelId}/rooms`),
  checkAvailability: (params) => api.get('/api/hotels/availability', { params }),
};
