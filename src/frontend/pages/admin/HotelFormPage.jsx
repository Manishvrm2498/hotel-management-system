import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { adminApi } from '../../api/adminApi';
import { hotelApi } from '../../api/hotelApi';
import { asArray, getErrorMessage } from '../../utils/errors';
import { useToast } from '../../context/ToastContext';

const initialForm = {
  name: '',
  state: '',
  district: '',
  address: '',
  contactNumber: '',
  rating: '',
  description: '',
};

export default function HotelFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [form, setForm] = useState(initialForm);
  const [picture, setPicture] = useState(null);
  const [loading, setLoading] = useState(false);
  const isEdit = Boolean(id);

  useEffect(() => {
    if (!id) return;
    hotelApi
      .getHotel(id)
      .then(({ data }) => {
        const hotel = asArray(data)[0];
        if (hotel) setForm({ ...initialForm, ...hotel });
      })
      .catch((error) => showToast(getErrorMessage(error), 'error'));
  }, [id]);

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const payload = { ...form, rating: Number(form.rating) };
      const { data } = isEdit ? await adminApi.updateHotel(id, payload) : await adminApi.addHotel(payload);
      if (picture) {
        await adminApi.uploadHotelPicture(data.id || id, picture);
      }
      showToast(isEdit ? 'Hotel updated' : 'Hotel added', 'success');
      navigate('/admin/hotels');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">{isEdit ? 'Update' : 'Create'}</span>
          <h1>{isEdit ? 'Update Hotel' : 'Add Hotel'}</h1>
        </div>
      </div>
      <form className="admin-form glass" onSubmit={submit}>
        <div className="two-col">
          <InputField label="Hotel name" name="name" value={form.name} onChange={update} required />
          <InputField label="Contact number" name="contactNumber" value={form.contactNumber} onChange={update} required />
        </div>
        <div className="two-col">
          <InputField label="State" name="state" value={form.state} onChange={update} required />
          <InputField label="District" name="district" value={form.district} onChange={update} required />
        </div>
        <InputField label="Address" name="address" value={form.address} onChange={update} required />
        <InputField label="Rating" name="rating" type="number" min="0" max="5" step="0.1" value={form.rating} onChange={update} required />
        <label className="field">
          <span>Description</span>
          <textarea name="description" rows="5" value={form.description || ''} onChange={update} />
        </label>
        <label className="field">
          <span>Hotel picture</span>
          <input type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png" onChange={(event) => setPicture(event.target.files?.[0] || null)} />
        </label>
        <Button disabled={loading}>{loading ? 'Saving...' : 'Save hotel'}</Button>
      </form>
    </section>
  );
}
