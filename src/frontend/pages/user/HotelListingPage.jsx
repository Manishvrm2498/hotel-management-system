import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import HotelCard from '../../components/HotelCard';
import Pagination from '../../components/Pagination';
import SearchBar from '../../components/SearchBar';
import { SkeletonGrid } from '../../components/Loader';
import { hotelApi } from '../../api/hotelApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { getFavoriteHotelIds, saveFavoriteHotelIds, toggleFavoriteHotel } from '../../utils/favorites';
import { useToast } from '../../context/ToastContext';

const PAGE_SIZE = 6;

export default function HotelListingPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const { showToast } = useToast();
  const [filters, setFilters] = useState({
    name: searchParams.get('name') || '',
    district: searchParams.get('district') || '',
    state: searchParams.get('state') || '',
    rating: searchParams.get('rating') || '',
    maxPrice: searchParams.get('maxPrice') || '',
  });
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [favorites, setFavorites] = useState(() => getFavoriteHotelIds());

  const loadHotels = async (nextFilters = filters) => {
    setLoading(true);
    try {
      const params = Object.fromEntries(Object.entries(nextFilters).filter(([, value]) => value));
      const request = params.name && !params.state && !params.district && !params.rating ? hotelApi.findByName(params.name) : hotelApi.getHotels(params);
      const { data } = await request;
      setHotels(asArray(data));
      setPage(1);
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHotels(filters);
  }, []);

  const filteredHotels = useMemo(() => {
    return hotels.filter((hotel) => {
      const ratingOk = filters.rating ? Number(hotel.rating || 0) >= Number(filters.rating) : true;
      return ratingOk;
    });
  }, [hotels, filters.rating]);

  const totalPages = Math.max(1, Math.ceil(filteredHotels.length / PAGE_SIZE));
  const pageItems = filteredHotels.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const submit = (event) => {
    event.preventDefault();
    setSearchParams(Object.fromEntries(Object.entries(filters).filter(([, value]) => value)));
    loadHotels(filters);
  };

  const toggleFavorite = (id) => {
    const next = toggleFavoriteHotel(id, favorites);
    setFavorites(next);
    saveFavoriteHotelIds(next);
  };

  return (
    <section className="section page-section">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Explore</span>
          <h1>Hotel Collection</h1>
        </div>
      </div>
      <SearchBar filters={filters} onChange={setFilters} onSubmit={submit} />
      {loading ? (
        <SkeletonGrid count={6} />
      ) : (
        <>
          <div className="result-meta">{filteredHotels.length} hotels found</div>
          <div className="grid cards-grid">
            {pageItems.map((hotel) => (
              <HotelCard key={hotel.id} hotel={hotel} favorite={favorites.includes(String(hotel.id))} onFavorite={toggleFavorite} />
            ))}
          </div>
          {!pageItems.length && <div className="empty-state glass">No hotels match your filters.</div>}
          <Pagination page={page} totalPages={totalPages} onChange={setPage} />
        </>
      )}
    </section>
  );
}
