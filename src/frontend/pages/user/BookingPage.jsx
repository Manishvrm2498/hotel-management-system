import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import Modal from '../../components/Modal';
import { hotelApi } from '../../api/hotelApi';
import { roomApi } from '../../api/roomApi';
import { bookingApi } from '../../api/bookingApi';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { asArray, getErrorMessage } from '../../utils/errors';

export default function BookingPage() {
  const { hotelId, roomId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { showToast } = useToast();
  const [hotel, setHotel] = useState(null);
  const [room, setRoom] = useState(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    checkInDate: '',
    checkOutDate: '',
    totalGuests: 1,
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phoneNumber: '',
  });

  useEffect(() => {
    Promise.all([hotelApi.getHotel(hotelId), roomApi.getRoom(roomId)])
      .then(([hotelResponse, roomResponse]) => {
        setHotel(asArray(hotelResponse.data)[0]);
        setRoom(roomResponse.data);
      })
      .catch((error) => showToast(getErrorMessage(error), 'error'));
  }, [hotelId, roomId]);

  const nights = useMemo(() => {
    if (!form.checkInDate || !form.checkOutDate) return 1;
    const start = new Date(form.checkInDate);
    const end = new Date(form.checkOutDate);
    return Math.max(1, Math.ceil((end - start) / 86400000));
  }, [form.checkInDate, form.checkOutDate]);

  const total = nights * Number(room?.price || 0);
  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const { data } = await bookingApi.createBooking({
        ...form,
        totalGuests: Number(form.totalGuests),
        hotelId: Number(hotelId),
        roomId: Number(roomId),
      });
      const booking = data?.data || {};
      const nextBookingId = data?.bookingId || booking.id;
      if (nextBookingId) {
        navigate(`/payment/${nextBookingId}`, {
          state: {
            booking: {
              ...booking,
              id: nextBookingId,
              hotelName: booking.hotelName || hotel?.name,
              roomType: booking.roomType || room?.type,
              totalPrice: booking.totalPrice || total,
              checkInDate: booking.checkInDate || form.checkInDate,
              checkOutDate: booking.checkOutDate || form.checkOutDate,
            },
          },
        });
        showToast('Booking initiated successfully', 'success');
        return;
      }
      setSuccess(true);
      showToast('Booking initiated successfully', 'success');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="section page-section booking-layout">
      <form className="booking-form glass" onSubmit={submit}>
        <span className="eyebrow">Confirm stay</span>
        <h1>Booking details</h1>
        <div className="two-col">
          <InputField label="Check-in" name="checkInDate" type="date" value={form.checkInDate} onChange={update} required />
          <InputField label="Check-out" name="checkOutDate" type="date" value={form.checkOutDate} onChange={update} required />
        </div>
        <InputField label="Guests" name="totalGuests" type="number" min="1" max="10" value={form.totalGuests} onChange={update} required />
        <div className="two-col">
          <InputField label="First name" name="firstName" value={form.firstName} onChange={update} required />
          <InputField label="Last name" name="lastName" value={form.lastName} onChange={update} required />
        </div>
        <InputField label="Email" name="email" type="email" value={form.email} onChange={update} required />
        <InputField label="Phone number" name="phoneNumber" value={form.phoneNumber} onChange={update} required />
        <Button disabled={loading}>{loading ? 'Confirming...' : 'Confirm booking'}</Button>
      </form>

      <aside className="booking-summary glass">
        <h2>{hotel?.name || 'Selected hotel'}</h2>
        <p>{room?.hotelName} - {String(room?.type || 'Room').replaceAll('_', ' ')}</p>
        <div className="summary-line"><span>Nights</span><strong>{nights}</strong></div>
        <div className="summary-line"><span>Rate</span><strong>₹{Number(room?.price || 0).toLocaleString('en-IN')}</strong></div>
        <div className="summary-line total"><span>Total</span><strong>₹{total.toLocaleString('en-IN')}</strong></div>
      </aside>

      <Modal open={success} title="Booking confirmed" onClose={() => navigate('/my-bookings')}>
        <div className="success-animation">✓</div>
        <p>Your booking has been initiated, but the payment details were not returned. You can review it from your bookings.</p>
        <Button onClick={() => navigate('/my-bookings')}>View my bookings</Button>
      </Modal>
    </section>
  );
}
