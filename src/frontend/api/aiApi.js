import api from './axiosInstance';

export const aiApi = {
  chat: (prompt) => api.post('/api/ai/chat', { prompt }),
};
