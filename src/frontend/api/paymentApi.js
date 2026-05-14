import api from './axiosInstance';

export const paymentApi = {
  processPayment: ({ bookingId, amount, method }) => api.post('/api/payments/process', null, {
    params: { bookingId, amount, method },
  }),
};
