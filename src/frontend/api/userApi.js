import api from './axiosInstance';

export const userApi = {
  profile: () => api.get('/api/users/profile'),
  deleteAccount: () => api.delete('/api/users/profile'),
  uploadPicture: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/api/users/profile/picture', formData);
  },
};
