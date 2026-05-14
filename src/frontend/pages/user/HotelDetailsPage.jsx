import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import RoomCard from '../../components/RoomCard';
import StarRating from '../../components/StarRating';
import { Loader } from '../../components/Loader';
import { hotelApi } from '../../api/hotelApi';
import { reviewApi } from '../../api/reviewApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';
import { DEFAULT_HOTEL_IMAGE, DEFAULT_USER_IMAGE, assetUrl, useFallbackImage } from '../../utils/images';

export default function HotelDetailsPage() {
  const { id } = useParams();
  const { showToast } = useToast();
  const [hotel, setHotel] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [imageIndex, setImageIndex] = useState(0);

  const images = useMemo(
    () => [
      assetUrl(hotel?.imageUrl, DEFAULT_HOTEL_IMAGE),
      'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1300&q=80',
      'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1300&q=80',
    ],
    [hotel?.imageUrl]
  );

  useEffect(() => {
    let ignore = false;

    async function loadHotelDetails() {
      setLoading(true);

      try {
        const hotelResponse = await hotelApi.getHotel(id);
        if (ignore) return;

        const nextHotel = asArray(hotelResponse.data)[0];
        setHotel(nextHotel || null);

        if (!nextHotel) {
          showToast('Hotel not found.', 'error');
          return;
        }

        const [roomResult, reviewResult] = await Promise.allSettled([
          hotelApi.getRooms(id),
          reviewApi.getHotelReviews(id),
        ]);

        if (ignore) return;

        if (roomResult.status === 'fulfilled') {
          setRooms(asArray(roomResult.value.data));
        } else {
          setRooms([]);
          showToast(`Rooms unavailable: ${getErrorMessage(roomResult.reason)}`, 'error');
        }

        if (reviewResult.status === 'fulfilled') {
          setReviews(asArray(reviewResult.value.data));
        } else {
          setReviews([]);
          showToast(`Reviews unavailable: ${getErrorMessage(reviewResult.reason)}`, 'error');
        }
      } catch (error) {
        if (!ignore) {
          setHotel(null);
          showToast(`Unable to load hotel details: ${getErrorMessage(error)}`, 'error');
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    void loadHotelDetails();

    return () => {
      ignore = true;
    };
  }, [id, showToast]);

  if (loading) return <Loader label="Preparing hotel details" />;
  if (!hotel) return <div className="empty-state glass">Hotel not found.</div>;

  return (
    <section className="section page-section">
      <div className="details-hero glass">
        <div className="gallery">
          <img src={images[imageIndex]} alt={hotel.name} onError={(event) => useFallbackImage(event, DEFAULT_HOTEL_IMAGE)} />
          <div className="gallery-controls">
            {images.map((_, index) => (
              <button key={index} type="button" className={index === imageIndex ? 'active' : ''} onClick={() => setImageIndex(index)} aria-label={`Show image ${index + 1}`} />
            ))}
          </div>
        </div>
        <div className="details-copy">
          <span className="eyebrow">{hotel.district}, {hotel.state}</span>
          <h1>{hotel.name}</h1>
          <p>{hotel.description || hotel.address}</p>
          <div className="detail-list">
            <span>{hotel.address}</span>
            <span>{hotel.contactNumber}</span>
            <span>{Number(hotel.rating || 0).toFixed(1)}  Rating</span>
          </div>
        </div>
      </div>

      <div className="section-heading">
        <div>
          <span className="eyebrow">Rooms</span>
          <h2>Available room types</h2>
        </div>
      </div>
      {rooms.length ? (
        <div className="grid room-grid">
          {rooms.map((room) => <RoomCard key={room.id} room={room} hotelId={hotel.id} />)}
        </div>
      ) : (
        <div className="empty-state glass">No rooms have been added for this hotel yet.</div>
      )}

      <section className="reviews glass">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Guest voice</span>
            <h2>Reviews</h2>
          </div>
        </div>
        {reviews.length ? reviews.map((review) => (
          <article className="review" key={review.id || `${review.username}-${review.rating}`}>
            <div className="review-head">
              <div className="reviewer">
                <img src={assetUrl(review.userImageUrl, DEFAULT_USER_IMAGE)} alt={review.username || 'Reviewer'} onError={(event) => useFallbackImage(event, DEFAULT_USER_IMAGE)} />
                <strong>{review.username || review.userName || review.email || 'Guest'}</strong>
              </div>
              <StarRating value={review.rating || 5} />
            </div>
            <p>{review.comment || review.review || 'A memorable stay.'}</p>
          </article>
        )) : <p>No reviews yet. Be the first guest to share feedback.</p>}
      </section>
    </section>
  );
}
