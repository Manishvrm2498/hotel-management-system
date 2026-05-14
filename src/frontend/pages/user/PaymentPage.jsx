import { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import Button from '../../components/Button';
import Modal from '../../components/Modal';
import { Loader } from '../../components/Loader';
import { bookingApi } from '../../api/bookingApi';
import { paymentApi } from '../../api/paymentApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

const methods = ['CARD', 'UPI', 'NET_BANKING', 'CASH'];

export default function PaymentPage() {
  const { bookingId } = useParams();
  const { state } = useLocation();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [booking, setBooking] = useState(state?.booking || null);
  const [method, setMethod] = useState('CARD');
  const [loading, setLoading] = useState(!state?.booking);
  const [paying, setPaying] = useState(false);
  const [receipt, setReceipt] = useState(null);

  useEffect(() => {
    if (booking) return;
    setLoading(true);
    bookingApi
      .myBookings()
      .then(({ data }) => {
        const match = asArray(data).find((item) => String(item.bookingId || item.id) === String(bookingId));
        setBooking(match || null);
      })
      .catch((error) => showToast(getErrorMessage(error), 'error'))
      .finally(() => setLoading(false));
  }, [booking, bookingId]);

  const amount = useMemo(() => Number(booking?.totalPrice || booking?.amount || booking?.totalAmount || 0), [booking]);
  const status = String(booking?.status || 'PENDING').toUpperCase();
  const isPaid = status === 'CONFIRMED' || status === 'PAID';

  const pay = async (event) => {
    event.preventDefault();
    setPaying(true);
    try {
      const { data } = await paymentApi.processPayment({
        bookingId: Number(bookingId),
        amount,
        method,
      });
      setReceipt(data);
      showToast('Payment completed', 'success');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setPaying(false);
    }
  };

  if (loading) return <Loader label="Loading payment" />;

  if (!booking) {
    return (
      <section className="section page-section">
        <div className="booking-detail glass">
          <span className="eyebrow">Payment</span>
          <h1>Booking not found</h1>
          <p>This booking is not available in your account.</p>
          <Button onClick={() => navigate('/my-bookings')}>View my bookings</Button>
        </div>
      </section>
    );
  }

  return (
    <section className="section page-section booking-layout">
      <form className="booking-form glass" onSubmit={pay}>
        <span className="eyebrow">Secure payment</span>
        <h1>Complete payment</h1>
        <label className="field">
          <span>Payment method</span>
          <select value={method} onChange={(event) => setMethod(event.target.value)} disabled={isPaid}>
            {methods.map((item) => (
              <option key={item} value={item}>{item.replace('_', ' ')}</option>
            ))}
          </select>
        </label>
        <div className="summary-line total">
          <span>Amount payable</span>
          <strong>₹{amount.toLocaleString('en-IN')}</strong>
        </div>
        <Button disabled={paying || isPaid || amount <= 0}>
          {isPaid ? 'Already paid' : paying ? 'Processing...' : 'Pay now'}
        </Button>
        <Button type="button" variant="ghost" onClick={() => navigate('/my-bookings')}>Back to bookings</Button>
      </form>

      <aside className="booking-summary glass">
        <h2>{booking.hotelName || 'Selected hotel'}</h2>
        <p>{String(booking.roomType || 'Room').replaceAll('_', ' ')}</p>
        <div className="summary-line"><span>Booking ID</span><strong>#{bookingId}</strong></div>
        <div className="summary-line"><span>Check-in</span><strong>{booking.checkInDate || booking.checkIn}</strong></div>
        <div className="summary-line"><span>Check-out</span><strong>{booking.checkOutDate || booking.checkOut}</strong></div>
        <div className="summary-line"><span>Status</span><strong>{status}</strong></div>
      </aside>

      <Modal open={Boolean(receipt)} title="Payment successful" onClose={() => navigate('/my-bookings')}>
        <div className="success-animation">✓</div>
        <p>Transaction {receipt?.transactionId} was completed for ₹{Number(receipt?.amount || amount).toLocaleString('en-IN')}.</p>
        <Button onClick={() => navigate('/my-bookings')}>View my bookings</Button>
      </Modal>
    </section>
  );
}
