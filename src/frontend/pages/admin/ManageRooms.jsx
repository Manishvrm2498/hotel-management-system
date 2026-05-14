import { useEffect, useState } from 'react';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { adminApi } from '../../api/adminApi';
import { hotelApi } from '../../api/hotelApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

const initialRoom = { hotelId: '', type: 'DELUXE', price: '', totalRooms: '' };

export default function ManageRooms() {
  const { showToast } = useToast();
  const [hotels, setHotels] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [selectedHotel, setSelectedHotel] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(initialRoom);
  const [picture, setPicture] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    hotelApi
      .getHotels()
      .then(({ data }) => setHotels(asArray(data)))
      .catch((error) => showToast(getErrorMessage(error), 'error'));
  }, []);

  useEffect(() => {
    if (!selectedHotel) return;
    hotelApi
      .getRooms(selectedHotel)
      .then(({ data }) => setRooms(asArray(data)))
      .catch((error) => showToast(getErrorMessage(error), 'error'));
  }, [selectedHotel]);

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const editRoom = (room) => {
    setEditingId(room.id);
    setForm({
      hotelId: room.hotelId || selectedHotel,
      type: room.type,
      price: room.price,
      totalRooms: room.totalRooms,
    });
    setPicture(null);
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const payload = {
        hotelId: Number(form.hotelId),
        type: form.type,
        price: Number(form.price),
        totalRooms: Number(form.totalRooms),
      };
      const response = editingId ? await adminApi.updateRoom(editingId, payload) : await adminApi.addRoom(payload);
      if (picture) {
        await adminApi.uploadRoomPicture(response.data.id || editingId, picture);
      }
      showToast(editingId ? 'Room updated' : 'Room added', 'success');
      setForm(initialRoom);
      setPicture(null);
      setEditingId(null);
      setSelectedHotel(String(payload.hotelId));
      const roomsResponse = await hotelApi.getRooms(payload.hotelId);
      setRooms(asArray(roomsResponse.data));
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="admin-page rooms-admin">
      <div className="section-heading">
        <div>
          <span className="eyebrow">Rooms</span>
          <h1>Manage Rooms</h1>
        </div>
      </div>
      <form className="admin-form glass" onSubmit={submit}>
        <div className="two-col">
          <label className="field">
            <span>Hotel</span>
            <select name="hotelId" value={form.hotelId} onChange={update} required>
              <option value="">Select hotel</option>
              {hotels.map((hotel) => <option key={hotel.id} value={hotel.id}>{hotel.name}</option>)}
            </select>
          </label>
          <label className="field">
            <span>Room type</span>
            <select name="type" value={form.type} onChange={update}>
              <option value="DELUXE">Deluxe</option>
              <option value="SINGLE">Single</option>
              <option value="DOUBLE">Double</option>
              <option value="SUITE">Suite</option>
              <option value="PRESIDENTIAL">Presidential</option>
            </select>
          </label>
        </div>
        <div className="two-col">
          <InputField label="Price" name="price" type="number" value={form.price} onChange={update} required />
          <InputField label="Total rooms" name="totalRooms" type="number" value={form.totalRooms} onChange={update} required />
        </div>
        <label className="field">
          <span>Room picture</span>
          <input type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png" onChange={(event) => setPicture(event.target.files?.[0] || null)} />
        </label>
        <Button disabled={loading}>{loading ? 'Saving...' : editingId ? 'Update room' : 'Add room'}</Button>
      </form>

      <div className="table-wrap glass">
        <div className="table-toolbar">
          <h2>Room inventory</h2>
          <select value={selectedHotel} onChange={(event) => setSelectedHotel(event.target.value)}>
            <option value="">Choose hotel</option>
            {hotels.map((hotel) => <option key={hotel.id} value={hotel.id}>{hotel.name}</option>)}
          </select>
        </div>
        <table>
          <thead><tr><th>Type</th><th>Price</th><th>Total</th><th>Status</th><th /></tr></thead>
          <tbody>
            {rooms.map((room) => (
              <tr key={room.id}>
                <td>{room.type}</td>
                <td>₹{Number(room.price).toLocaleString('en-IN')}</td>
                <td>{room.totalRooms}</td>
                <td><span className="status-pill success">{room.available ? 'Available' : 'Limited'}</span></td>
                <td><Button variant="ghost" onClick={() => editRoom(room)}>Edit</Button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
