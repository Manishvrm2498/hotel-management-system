import { Link } from 'react-router-dom';
import Button from './Button';
import { DEFAULT_HOTEL_IMAGE, assetUrl, useFallbackImage } from '../utils/images';

export default function HotelCard({ hotel, favorite, onFavorite }) {
  const toggleFavorite = (event) => {
    event.preventDefault();
    event.stopPropagation();
    onFavorite?.(hotel.id);
  };

  return (
    <article className="hotel-card glass">
      <div className="hotel-image">
        <img src={assetUrl(hotel.imageUrl, DEFAULT_HOTEL_IMAGE)} alt={hotel.name} loading="lazy" onError={(event) => useFallbackImage(event, DEFAULT_HOTEL_IMAGE)} />
        <button type="button" className={`favorite ${favorite ? 'active' : ''}`} onClick={toggleFavorite} aria-label={favorite ? 'Remove from favorites' : 'Add to favorites'} title={favorite ? 'Remove from favorites' : 'Add to favorites'}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M20.8 4.6c-2-2-5.1-1.8-6.9.2L12 6.8l-1.9-2c-1.8-2-4.9-2.2-6.9-.2-2.3 2.3-2.2 6 .2 8.3l7.2 6.8c.8.8 2 .8 2.8 0l7.2-6.8c2.4-2.3 2.5-6 .2-8.3Z" />
          </svg>
        </button>
      </div>
      <div className="hotel-card-body">
        <div className="card-kicker">
          <span>{hotel.district}, {hotel.state}</span>
          <strong>{Number(hotel.rating || 0).toFixed(1)} ⭐️</strong>
        </div>
        <h3>{hotel.name}</h3>
        <p>{hotel.description || hotel.address}</p>
        <div className="card-actions">
          <Link to={`/hotels/${hotel.id}`} className="text-link">
            View details
          </Link>
          <Button as="span" className="btn-small" onClick={() => {}}>
            Reserve
          </Button>
        </div>
      </div>
    </article>
  );
}
