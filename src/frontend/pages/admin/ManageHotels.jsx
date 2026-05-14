import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import SearchBar from '../../components/SearchBar';
import { hotelApi } from '../../api/hotelApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

export default function ManageHotels() {
  const { showToast } = useToast();
  const [hotels, setHotels] = useState([]);
  const [filters, setFilters] = useState({ name: '', district: '', state: '', rating: '' });
  const [loading, setLoading] = useState(true);

  const loadHotels = async (nextFilters = filters) => {
    setLoading(true);
    try {
      const params = Object.fromEntries(Object.entries(nextFilters).filter(([, value]) => value));
      const { data } = await hotelApi.getHotels(params);
      setHotels(asArray(data));
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadHotels(); }, []);

  const submit = (event) => {
    event.preventDefault();
    loadHotels(filters);
  };

  return (
    <section className="admin-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Inventory</span>
          <h1>Manage Hotels</h1>
        </div>
        <Link to="/admin/hotels/add"><Button>Add hotel</Button></Link>
      </div>
      <SearchBar filters={filters} onChange={setFilters} onSubmit={submit} compact />
      <div className="table-wrap glass">
        <table>
          <thead><tr><th>Name</th><th>Location</th><th>Rating</th><th>Contact</th><th>Actions</th></tr></thead>
          <tbody>
            {hotels.map((hotel) => (
              <tr key={hotel.id}>
                <td>{hotel.name}</td>
                <td>{hotel.district}, {hotel.state}</td>
                <td>{hotel.rating}</td>
                <td>{hotel.contactNumber}</td>
                <td className="table-actions">
                  <Link to={`/admin/hotels/${hotel.id}/edit`}><Button variant="ghost">Update</Button></Link>
                  <Button variant="danger" onClick={() => showToast('Delete hotel endpoint is not exposed by the backend yet', 'info')}>Delete</Button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && !hotels.length && <div className="empty-state">No hotels found.</div>}
      </div>
    </section>
  );
}
