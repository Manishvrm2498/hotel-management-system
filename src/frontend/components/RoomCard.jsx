import { Link } from 'react-router-dom';
import Button from './Button';
import { DEFAULT_ROOM_IMAGE, assetUrl, useFallbackImage } from '../utils/images';

export default function RoomCard({ room, hotelId }) {
  return (
    <article className="room-card glass">
      <img className="room-card-image" src={assetUrl(room.imageUrl, DEFAULT_ROOM_IMAGE)} alt={room.type || 'Room'} loading="lazy" onError={(event) => useFallbackImage(event, DEFAULT_ROOM_IMAGE)} />
      <div>
        <span className={`status-pill ${room.available ? 'success' : 'muted'}`}>
          {room.available ? 'Available' : 'Limited'}
        </span>
        <h3>{String(room.type || 'Suite').replaceAll('_', ' ')}</h3>
        <p>{room.totalRooms || 0} rooms ready at {room.hotelName || 'this property'}.</p>
      </div>
      <div className="room-price">
        <strong>₹{Number(room.price || 0).toLocaleString('en-IN')}</strong>
        <span>/ night</span>
      </div>
      <div className="card-actions">
        <Link className="text-link" to={`/rooms/${room.id}`} state={{ hotelId: hotelId || room.hotelId }}>
          Details
        </Link>
        <Link to={`/booking/${hotelId || room.hotelId}/${room.id}`}>
          <Button>Book</Button>
        </Link>
      </div>
    </article>
  );
}
