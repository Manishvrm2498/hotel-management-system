import { useEffect, useMemo, useState } from 'react';
import HotelCard from '../../components/HotelCard';
import { SkeletonGrid } from '../../components/Loader';
import { hotelApi } from '../../api/hotelApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { getFavoriteHotelIds, saveFavoriteHotelIds, toggleFavoriteHotel } from '../../utils/favorites';
import { useToast } from '../../context/ToastContext';

export default function FavoriteHotelsPage() {
  const { showToast } = useToast();
  const [hotels, setHotels] = useState([]);
  const [favorites, setFavorites] = useState(() => getFavoriteHotelIds());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    hotelApi
      .getHotels()
      .then(({ data }) => setHotels(asArray(data)))
      .catch((error) => showToast(getErrorMessage(error), 'error'))
      .finally(() => setLoading(false));
  }, []);

  const favoriteHotels = useMemo(
    () => hotels.filter((hotel) => favorites.includes(String(hotel.id))),
    [hotels, favorites]
  );

  const toggleFavorite = (id) => {
    const next = toggleFavoriteHotel(id, favorites);
    setFavorites(next);
    saveFavoriteHotelIds(next);
  };

  return (
    <section className="section page-section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Saved</span>
          <h1>Favorite Hotels</h1>
        </div>
      </div>
      {loading ? (
        <SkeletonGrid count={3} />
      ) : favoriteHotels.length ? (
        <div className="grid cards-grid">
          {favoriteHotels.map((hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} favorite onFavorite={toggleFavorite} />
          ))}
        </div>
      ) : (
        <div className="empty-state glass">No favorite hotels yet.</div>
      )}
    </section>
  );
}
