import api from './axiosInstance';

export const roomApi = {
  getRoom: (id) => api.get(`/api/rooms/${id}`),
  updateRoom: (id, payload) => api.put(`/api/rooms/${id}`, payload),
};
