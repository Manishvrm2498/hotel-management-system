import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import { hotelApi } from '../../api/hotelApi';
import { asArray } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

export default function AdminDashboard() {
  const { showToast } = useToast();
  const [hotels, setHotels] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    hotelApi
      .getHotels()
      .then(({ data }) => setHotels(asArray(data)))
      .catch(() => showToast('Unable to load dashboard statistics', 'error'))
      .finally(() => setLoading(false));
  }, []);

  const stats = useMemo(() => {
    const avgRating = hotels.length ? hotels.reduce((sum, hotel) => sum + Number(hotel.rating || 0), 0) / hotels.length : 0;
    const locations = new Set(hotels.map((hotel) => hotel.district).filter(Boolean));
    return [
      ['Hotels', hotels.length],
      ['Locations', locations.size],
      ['Avg rating', avgRating.toFixed(1)],
      ['Occupancy signal', loading ? '...' : 'Live'],
    ];
  }, [hotels, loading]);

  return (
    <section className="admin-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Control room</span>
          <h1>Admin Dashboard</h1>
        </div>
        <Link to="/admin/hotels/add"><Button>Add hotel</Button></Link>
      </div>
      <div className="dashboard-grid">
        {stats.map(([label, value]) => (
          <article className="stat-card glass" key={label}>
            <strong>{value}</strong>
            <span>{label}</span>
          </article>
        ))}
      </div>
      <section className="glass admin-panel">
        <h2>Recent hotels</h2>
        <div className="table-wrap clean">
          <table>
            <thead><tr><th>Name</th><th>Location</th><th>Rating</th><th>Contact</th></tr></thead>
            <tbody>
              {hotels.slice(0, 6).map((hotel) => (
                <tr key={hotel.id}>
                  <td>{hotel.name}</td>
                  <td>{hotel.district}, {hotel.state}</td>
                  <td>{hotel.rating}</td>
                  <td>{hotel.contactNumber}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}
