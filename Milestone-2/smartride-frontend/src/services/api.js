import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests automatically
// Every request will include: Authorization: Bearer eyJhbGc...
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle 401 and 403 errors (unauthorized/forbidden) - auto logout user
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH APIs ====================
// POST /api/auth/register     → Register new user
// POST /api/auth/verify-otp   → Verify email with OTP
// POST /api/auth/login        → Login and get JWT token
// POST /api/auth/resend-otp   → Resend OTP email

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  verifyOtp: (data) => api.post('/auth/verify-otp', data),
  login: (data) => api.post('/auth/login', data),
  resendOtp: (email) => api.post('/auth/resend-otp', null, { params: { email } }),
};

// ==================== RIDE APIs ====================
// POST /api/rides              → Post a new ride (Driver)
// GET  /api/rides/search       → Search rides (Public)
// GET  /api/rides/:id          → Get ride by ID
// GET  /api/rides/my-rides     → Driver's posted rides
// PUT  /api/rides/:id/cancel   → Cancel a ride (Driver)
// PUT  /api/rides/:id/complete → Complete a ride (Driver)

export const rideAPI = {
  postRide: (data) => api.post('/rides', data),
  searchRides: (params) => api.get('/rides/search', { params }),
  getRideById: (id) => api.get(`/rides/${id}`),
  getMyRides: () => api.get('/rides/my-rides'),
  cancelRide: (id) => api.put(`/rides/${id}/cancel`),
  completeRide: (id) => api.put(`/rides/${id}/complete`),
  getStatistics: () => api.get('/rides/statistics'),
};

// ==================== BOOKING APIs ====================
// POST /api/bookings                    → Book a ride (Passenger)
// GET  /api/bookings/my-bookings        → Passenger's bookings  ✅ FIXED: was /bookings/my
// GET  /api/bookings/ride/:rideId       → Bookings for a ride (Driver)
// GET  /api/bookings/:bookingId         → Get booking by ID
// PUT  /api/bookings/:bookingId/cancel  → Cancel a booking (Passenger)

export const bookingAPI = {
  bookRide: (data) => api.post('/bookings', data),
  getMyBookings: () => api.get('/bookings/my-bookings'),    // ✅ FIXED: was '/bookings/my'
  getRideBookings: (rideId) => api.get(`/bookings/ride/${rideId}`),
  getBookingById: (bookingId) => api.get(`/bookings/${bookingId}`),
  cancelBooking: (bookingId) => api.put(`/bookings/${bookingId}/cancel`),
};

export const paymentAPI = {
  createOrder: (data) => api.post('/payments/create-order', data),
  verifyPayment: (data) => api.post('/payments/verify', data),
};

export const fareAPI = {
  estimateFare: (data) => api.post('/fare/estimate', data),
};

// ==================== NOTIFICATION APIs ====================
export const notificationAPI = {
  getNotifications: () => api.get('/notifications'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
};

// ==================== RIDE ACTION APIs ====================
export const rideActionAPI = {
  acceptRide: (id) => api.patch(`/ride-actions/${id}/accept`),
  startRide: (id) => api.patch(`/ride-actions/${id}/start`),
  endRide: (id) => api.patch(`/ride-actions/${id}/end`),
};

export default api;