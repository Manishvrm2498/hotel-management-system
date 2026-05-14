import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Button from '../../components/Button';
import InputField from '../../components/InputField';
import { ConfirmModal } from '../../components/Modal';
import { authApi } from '../../api/authApi';
import { userApi } from '../../api/userApi';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { getErrorMessage } from '../../utils/errors';
import { DEFAULT_USER_IMAGE, useFallbackImage, versionedAssetUrl } from '../../utils/images';

function profileToForm(profile) {
  const email = profile?.email || '';
  return {
    firstName: profile?.firstName || '',
    lastName: profile?.lastName || '',
    email,
    password: '',
    role: profile?.role || 'USER',
  };
}

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, refreshProfile, logout } = useAuth();
  const { showToast } = useToast();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', role: 'USER' });
  const [picture, setPicture] = useState(null);
  const [loading, setLoading] = useState(false);
  const [uploadingPicture, setUploadingPicture] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletingAccount, setDeletingAccount] = useState(false);

  useEffect(() => {
    let ignore = false;
    refreshProfile().then((profile) => {
      if (!ignore && profile) {
        setForm(profileToForm(profile));
      }
    });
    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {
    setForm(profileToForm(user));
  }, [user]);

  const update = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const uploadProfilePicture = async () => {
    if (!picture) {
      showToast('Please choose a JPG, JPEG, or PNG file', 'error');
      return;
    }
    setUploadingPicture(true);
    try {
      await userApi.uploadPicture(picture);
      setPicture(null);
      showToast('Profile picture updated', 'success');
      await refreshProfile();
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setUploadingPicture(false);
    }
  };

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      await authApi.updateProfile(form);
      showToast('Profile updated', 'success');
      const profile = await refreshProfile();
      if (profile) setForm(profileToForm(profile));
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  };

  const deleteAccount = async () => {
    setDeletingAccount(true);
    try {
      await userApi.deleteAccount();
      setShowDeleteConfirm(false);
      logout('Your account has been deleted', 'success');
      navigate('/login', { replace: true });
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setDeletingAccount(false);
    }
  };

  return (
    <>
      <section className="section page-section profile-layout">
        <div className="profile-card glass">
          <div className="profile-avatar">
            <img src={versionedAssetUrl(user?.imageUrl, DEFAULT_USER_IMAGE, user?.imageVersion)} alt={form.firstName || 'User'} onError={(event) => useFallbackImage(event, DEFAULT_USER_IMAGE)} />
          </div>
          <h1>{form.firstName || 'Profile'} {form.lastName}</h1>
          <p>{form.email}</p>
          <span className="status-pill">{form.role}</span>
        </div>
        <form className="profile-form glass" onSubmit={submit}>
          <h2>Edit profile</h2>
          <div className="two-col">
            <InputField label="First name" name="firstName" value={form.firstName} onChange={update} required />
            <InputField label="Last name" name="lastName" value={form.lastName} onChange={update} required />
          </div>
          <InputField label="Email" name="email" type="email" value={form.email} onChange={update} required />
          <InputField label="Password" name="password" type="password" value={form.password} onChange={update} placeholder="Enter only when changing" />
          <label className="field">
            <span>Profile picture</span>
            <input type="file" accept=".jpg,.jpeg,.png,image/jpeg,image/png" onChange={(event) => setPicture(event.target.files?.[0] || null)} />
          </label>
          <Button type="button" variant="ghost" onClick={uploadProfilePicture} disabled={uploadingPicture || !picture}>
            {uploadingPicture ? 'Uploading...' : 'Upload picture'}
          </Button>
          <Button disabled={loading}>{loading ? 'Saving...' : 'Save profile'}</Button>
          <div className="danger-zone">
            <div>
              <strong>Delete account</strong>
              <p>This permanently removes your profile and account access.</p>
            </div>
            <Button type="button" variant="danger" onClick={() => setShowDeleteConfirm(true)}>
              Delete account
            </Button>
          </div>
        </form>
      </section>
      <ConfirmModal
        open={showDeleteConfirm}
        title="Delete account?"
        message="This will permanently delete your account. Your bookings, reviews, and saved login access will be removed."
        onClose={() => setShowDeleteConfirm(false)}
        onConfirm={deleteAccount}
        loading={deletingAccount}
        confirmVariant="danger"
      />
    </>
  );
}
