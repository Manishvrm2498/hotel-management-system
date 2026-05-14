import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import Modal, { ConfirmModal } from '../../components/Modal';
import StarRating from '../../components/StarRating';
import { Loader } from '../../components/Loader';
import { bookingApi } from '../../api/bookingApi';
import { reviewApi } from '../../api/reviewApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { downloadBookingReceipt } from '../../utils/receipt';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';

export default function MyBookingsPage() {
  const { showToast } = useToast();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelId, setCancelId] = useState(null);
  const [reviewBooking, setReviewBooking] = useState(null);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });
  const [reviewing, setReviewing] = useState(false);

  const bookingId = (booking) => booking.bookingId || booking.id;
  const bookingDates = (booking) => {
    const checkIn = booking.checkInDate || booking.checkIn || 'N/A';
    const checkOut = booking.checkOutDate || booking.checkOut || 'N/A';
    return `${checkIn} to ${checkOut}`;
  };

  const loadBookings = () => {
    setLoading(true);
    bookingApi
      .myBookings()
      .then(({ data }) => setBookings(asArray(data)))
      .catch((error) => showToast(getErrorMessage(error), 'error'))
      .finally(() => setLoading(false));
  };

  useEffect(loadBookings, []);

  const cancelBooking = async () => {
    try {
      await bookingApi.cancelBooking(cancelId);
      showToast('Booking cancelled', 'success');
      setCancelId(null);
      loadBookings();
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    }
  };

  const openReview = (booking) => {
    setReviewBooking(booking);
    setReviewForm({ rating: 5, comment: '' });
  };

  const submitReview = async (event) => {
    event.preventDefault();
    setReviewing(true);
    try {
      await reviewApi.addReview({
        bookingId: reviewBooking.bookingId || reviewBooking.id,
        rating: reviewForm.rating,
        comment: reviewForm.comment,
      });
      showToast('Review submitted', 'success');
      setReviewBooking(null);
      setReviewForm({ rating: 5, comment: '' });
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setReviewing(false);
    }
  };

  if (loading) return <Loader label="Loading bookings" />;

  return (
    <section className="section page-section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Guest desk</span>
          <h1>My Bookings</h1>
        </div>
      </div>
      <div className="table-wrap glass">
        <table>
          <thead>
            <tr>
              <th>Hotel</th>
              <th>Room</th>
              <th>Dates</th>
              <th>Status</th>
              <th>Total</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <tr key={bookingId(booking)}>
                <td>{booking.hotelName || booking.hotel?.name || 'Hotel'}</td>
                <td>{booking.roomType || booking.room?.type || booking.roomId}</td>
                <td>{bookingDates(booking)}</td>
                <td><span className={`status-pill ${booking.status === 'PENDING' ? 'muted' : 'success'}`}>{booking.status || 'CONFIRMED'}</span></td>
                <td>₹{Number(booking.totalAmount || booking.amount || 0).toLocaleString('en-IN')}</td>
                <td>
                  <div className="table-actions">
                    {booking.status === 'PENDING' && (
                      <Button variant="ghost" onClick={() => navigate(`/payment/${bookingId(booking)}`, { state: { booking } })}>Pay</Button>
                    )}
                    {booking.status === 'CONFIRMED' && (
                      <Button variant="ghost" onClick={() => openReview(booking)}>Review</Button>
                    )}
                    <Button variant="ghost" onClick={() => downloadBookingReceipt(booking, user)}>Receipt</Button>
                    <Button variant="ghost" onClick={() => setCancelId(bookingId(booking))}>Cancel</Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!bookings.length && <div className="empty-state">No bookings yet.</div>}
      </div>
      <ConfirmModal open={Boolean(cancelId)} title="Cancel booking?" message="This will request cancellation for your selected booking." onClose={() => setCancelId(null)} onConfirm={cancelBooking} />
      <Modal open={Boolean(reviewBooking)} title="Write review" onClose={() => setReviewBooking(null)}>
        <form className="stack-form" onSubmit={submitReview}>
          <div className="reviewer-box">
            <span>Reviewer</span>
            <strong>{`${user?.firstName || ''} ${user?.lastName || ''}`.trim() || user?.email || 'Guest'}</strong>
          </div>
          <div className="reviewer-box">
            <span>Hotel</span>
            <strong>{reviewBooking?.hotelName || 'Hotel'}</strong>
          </div>
          <label className="field">
            <span>Rating</span>
            <StarRating value={reviewForm.rating} onChange={(rating) => setReviewForm((current) => ({ ...current, rating }))} />
          </label>
          <label className="field">
            <span>Comment</span>
            <textarea rows="4" value={reviewForm.comment} onChange={(event) => setReviewForm((current) => ({ ...current, comment: event.target.value }))} required />
          </label>
          <Button disabled={reviewing}>{reviewing ? 'Submitting...' : 'Submit review'}</Button>
        </form>
      </Modal>
    </section>
  );
}
