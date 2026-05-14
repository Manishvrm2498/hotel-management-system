import api from './axiosInstance';

export const authApi = {
  login: (payload) => api.post('/api/auth/login', payload),
  register: (payload) => api.post('/api/auth/signup', payload),
  verifySignup: (payload) => api.post('/api/auth/verify-signup', payload),
  resendSignupOtp: (payload) => api.post('/api/auth/register/resend', payload),
  forgotPassword: (email) => api.post('/api/auth/forgot-password', { email }),
  verifyForgotOtp: (payload) => api.post('/api/auth/forgot-password/verify-otp', payload),
  resetPassword: (payload) => api.post('/api/auth/forgot-password/reset-password', payload),
  updateProfile: (payload) => api.put('/api/auth/update', payload),
};
