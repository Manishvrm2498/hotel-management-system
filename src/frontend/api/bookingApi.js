import api from './axiosInstance';

export const bookingApi = {
  createBooking: (payload) => api.post('/api/bookings/confirm', payload),
  myBookings: () => api.get('/api/bookings/my-bookings-details'),
  cancelBooking: (id) => api.patch(`/api/bookings/${id}/cancel`),
};
