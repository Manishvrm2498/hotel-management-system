import { useState } from 'react';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { adminApi } from '../../api/adminApi';
import { getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

export default function ManageBookings() {
  const { showToast } = useToast();
  const [bookingId, setBookingId] = useState('');
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const { data } = await adminApi.getBooking(bookingId);
      setBooking(data);
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Reservations</span>
          <h1>Manage Bookings</h1>
        </div>
      </div>
      <form className="search-panel glass search-compact" onSubmit={submit}>
        <InputField label="Booking ID" value={bookingId} onChange={(event) => setBookingId(event.target.value)} required />
        <Button disabled={loading}>{loading ? 'Searching...' : 'Find booking'}</Button>
      </form>
      {booking && (
        <article className="booking-detail glass">
          <h2>Booking #{booking.id}</h2>
          <div className="detail-list">
            <span>Hotel: {booking.hotelName || booking.hotelId}</span>
            <span>Room: {booking.roomType || booking.roomId}</span>
            <span>Guest: {booking.firstName} {booking.lastName}</span>
            <span>Email: {booking.email}</span>
            <span>Dates: {booking.checkInDate} to {booking.checkOutDate}</span>
            <span>Status: {booking.status}</span>
          </div>
        </article>
      )}
    </section>
  );
}
