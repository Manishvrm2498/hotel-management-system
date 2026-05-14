import api from './axiosInstance';

export const adminApi = {
  addHotel: (payload) => api.post('/api/admin/add', payload),
  updateHotel: (hotelId, payload) => api.put(`/api/admin/update/${hotelId}`, payload),
  uploadHotelPicture: (hotelId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/api/admin/hotels/${hotelId}/picture`, formData);
  },
  addRoom: (payload) => api.post('/api/admin/add-room', payload),
  updateRoom: (roomId, payload) => api.patch(`/api/admin/update/${roomId}`, payload),
  uploadRoomPicture: (roomId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/api/admin/rooms/${roomId}/picture`, formData);
  },
  getBooking: (bookingId) => api.get(`/api/admin/${bookingId}`),
  getUsers: () => api.get('/api/admin/user-list'),
  updateUserRole: (userId, role) => api.patch(`/api/admin/users/${userId}/role`, { role }),
  deleteUser: (userId) => api.delete(`/api/admin/users/${userId}`),
};
