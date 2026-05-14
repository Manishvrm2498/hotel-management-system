import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import Button from '../../components/Button';
import { Loader } from '../../components/Loader';
import { roomApi } from '../../api/roomApi';
import { getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';
import { DEFAULT_ROOM_IMAGE, assetUrl, useFallbackImage } from '../../utils/images';

export default function RoomDetailsPage() {
  const { id } = useParams();
  const location = useLocation();
  const { showToast } = useToast();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    roomApi
      .getRoom(id)
      .then(({ data }) => setRoom(data))
      .catch((error) => showToast(getErrorMessage(error), 'error'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Loader label="Loading room" />;
  if (!room) return <div className="empty-state glass">Room not found.</div>;

  const hotelId = location.state?.hotelId || room.hotelId;

  return (
    <section className="section page-section">
      <div className="room-detail glass">
        <img src={assetUrl(room.imageUrl, DEFAULT_ROOM_IMAGE)} alt={room.type} onError={(event) => useFallbackImage(event, DEFAULT_ROOM_IMAGE)} />
        <div>
          <span className="eyebrow">{room.hotelName}</span>
          <h1>{String(room.type || 'Suite').replaceAll('_', ' ')}</h1>
          <p>Spacious, quiet, and designed for restful stays with premium service from arrival to checkout.</p>
          <div className="feature-list">
            <span>High speed Wi-Fi</span>
            <span>Breakfast eligible</span>
            <span>Concierge support</span>
            <span>{room.totalRooms} rooms in inventory</span>
          </div>
          <div className="room-price large">
            <strong>₹{Number(room.price || 0).toLocaleString('en-IN')}</strong>
            <span>/ night</span>
          </div>
          <Link to={`/booking/${hotelId}/${room.id}`}>
            <Button>Continue booking</Button>
          </Link>
        </div>
      </div>
    </section>
  );
}
