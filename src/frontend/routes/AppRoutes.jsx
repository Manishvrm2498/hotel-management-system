import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from '../layouts/AdminLayout';
import AuthLayout from '../layouts/AuthLayout';
import PublicLayout from '../layouts/PublicLayout';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import HomePage from '../pages/user/HomePage';
import AiAssistantPage from '../pages/user/AiAssistantPage';
import HotelListingPage from '../pages/user/HotelListingPage';
import HotelDetailsPage from '../pages/user/HotelDetailsPage';
import FavoriteHotelsPage from '../pages/user/FavoriteHotelsPage';
import RoomDetailsPage from '../pages/user/RoomDetailsPage';
import BookingPage from '../pages/user/BookingPage';
import MyBookingsPage from '../pages/user/MyBookingsPage';
import PaymentPage from '../pages/user/PaymentPage';
import ProfilePage from '../pages/user/ProfilePage';
import ProtectedRoute from './ProtectedRoute';
import AdminDashboard from '../pages/admin/AdminDashboard';
import ManageHotels from '../pages/admin/ManageHotels';
import HotelFormPage from '../pages/admin/HotelFormPage';
import ManageRooms from '../pages/admin/ManageRooms';
import ManageBookings from '../pages/admin/ManageBookings';
import ManageUsers from '../pages/admin/ManageUsers';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      </Route>

      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/ai" element={<AiAssistantPage />} />
        <Route path="/hotels" element={<HotelListingPage />} />
        <Route path="/favorites" element={<FavoriteHotelsPage />} />
        <Route path="/hotels/:id" element={<HotelDetailsPage />} />
        <Route path="/rooms/:id" element={<RoomDetailsPage />} />
        <Route element={<ProtectedRoute roles={['USER', 'ADMIN', 'SUPERADMIN']} />}>
          <Route path="/booking/:hotelId/:roomId" element={<BookingPage />} />
          <Route path="/payment/:bookingId" element={<PaymentPage />} />
          <Route path="/my-bookings" element={<MyBookingsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['ADMIN', 'SUPERADMIN']} />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="hotels" element={<ManageHotels />} />
          <Route path="hotels/add" element={<HotelFormPage />} />
          <Route path="hotels/:id/edit" element={<HotelFormPage />} />
          <Route path="rooms" element={<ManageRooms />} />
          <Route path="bookings" element={<ManageBookings />} />
          <Route element={<ProtectedRoute roles={['SUPERADMIN']} />}>
            <Route path="users" element={<ManageUsers />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
