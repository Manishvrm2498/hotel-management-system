import api from './axiosInstance';

export const reviewApi = {
  getHotelReviews: (hotelId) => api.get(`/api/reviews/hotel/${hotelId}`),
  addReview: (payload) => api.post('/api/reviews/add', payload),
};
