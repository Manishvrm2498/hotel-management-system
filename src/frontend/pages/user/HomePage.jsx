import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import HotelCard from '../../components/HotelCard';
import SearchBar from '../../components/SearchBar';
import { SkeletonGrid } from '../../components/Loader';
import { hotelApi } from '../../api/hotelApi';
import { asArray } from '../../utils/errors';
import { getFavoriteHotelIds, saveFavoriteHotelIds, toggleFavoriteHotel } from '../../utils/favorites';
import { useToast } from '../../context/ToastContext';

export default function HomePage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [filters, setFilters] = useState({ name: '', district: '', state: '', rating: '' });
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [favorites, setFavorites] = useState(() => getFavoriteHotelIds());

  useEffect(() => {
    hotelApi
      .getHotels()
      .then(({ data }) => setHotels(asArray(data).slice(0, 3)))
      .catch(() => showToast('Unable to load featured hotels', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const stats = useMemo(
    () => [
      ['Luxury stays', hotels.length || 0],
      ['Average rating', hotels.length ? (hotels.reduce((sum, hotel) => sum + Number(hotel.rating || 0), 0) / hotels.length).toFixed(1) : '5.0'],
      ['Support', '24/7'],
    ],
    [hotels]
  );

  const submit = (event) => {
    event.preventDefault();
    const params = new URLSearchParams(Object.entries(filters).filter(([, value]) => value));
    navigate(`/hotels?${params.toString()}`);
  };

  const toggleFavorite = (id) => {
    const next = toggleFavoriteHotel(id, favorites);
    setFavorites(next);
    saveFavoriteHotelIds(next);
  };

  return (
    <div>
      <section className="hero">
        <div className="hero-content">
          <span className="eyebrow">Luxury hotel management</span>
          <h1>A polished stay experience from search to checkout.</h1>
          <p>Discover premium hotels, reserve rooms, manage bookings, and run admin operations from a responsive enterprise interface.</p>
          <div className="hero-actions">
            <Link to="/hotels"><Button>Explore hotels</Button></Link>
            <Link to="/register" className="text-link">Create guest account</Link>
          </div>
        </div>
        <SearchBar filters={filters} onChange={setFilters} onSubmit={submit} />
      </section>

      <section className="section stat-strip">
        {stats.map(([label, value]) => (
          <div className="stat-card glass" key={label}>
            <strong>{value}</strong>
            <span>{label}</span>
          </div>
        ))}
      </section>

      <section className="section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">Featured</span>
            <h2>Hotels guests love</h2>
          </div>
          <Link to="/hotels" className="text-link">View all</Link>
        </div>
        {loading ? (
          <SkeletonGrid count={3} />
        ) : (
          <div className="grid cards-grid">
            {hotels.map((hotel) => (
              <HotelCard key={hotel.id} hotel={hotel} favorite={favorites.includes(String(hotel.id))} onFavorite={toggleFavorite} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
