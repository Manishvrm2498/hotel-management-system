import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Button from './Button';
import { useAuth } from '../context/AuthContext';
import { THEME_KEY, getInitials } from '../utils/auth';
import { DEFAULT_USER_IMAGE, useFallbackImage, versionedAssetUrl } from '../utils/images';

export default function Navbar() {
  const navigate = useNavigate();
  const { isAuthenticated, logout, role, user } = useAuth();
  const [open, setOpen] = useState(false);
  const [dark, setDark] = useState(() => localStorage.getItem(THEME_KEY) !== 'light');

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? 'dark' : 'light';
    localStorage.setItem(THEME_KEY, dark ? 'dark' : 'light');
  }, [dark]);

  const signOut = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar glass">
      <Link className="brand" to="/">
        <span>AR</span>
        GrandStay
      </Link>
      <button type="button" className="menu-toggle" onClick={() => setOpen((value) => !value)} aria-label="Toggle menu">
        menu
      </button>
      <nav className={open ? 'nav-links open' : 'nav-links'}>
        <NavLink to="/hotels">Hotels</NavLink>
        <NavLink to="/ai">AI Assistant</NavLink>
        <NavLink to="/favorites">Favorites</NavLink>
        {isAuthenticated && <NavLink to="/my-bookings">My Bookings</NavLink>}
        {['ADMIN', 'SUPERADMIN'].includes(role) && <NavLink to="/admin">Admin</NavLink>}
        <button type="button" className="theme-toggle" onClick={() => setDark((value) => !value)}>
          {dark ? 'Light' : 'Dark'}
        </button>
        {isAuthenticated ? (
          <>
            <Link className="avatar-link" to="/profile" title={user?.email}>
              {user?.imageUrl ? (
                <img src={versionedAssetUrl(user.imageUrl, DEFAULT_USER_IMAGE, user.imageVersion)} alt={getInitials(user)} onError={(event) => useFallbackImage(event, DEFAULT_USER_IMAGE)} />
              ) : getInitials(user)}
            </Link>
            <Button variant="ghost" onClick={signOut}>
              Logout
            </Button>
          </>
        ) : (
          <Link to="/login">
            <Button>Login</Button>
          </Link>
        )}
      </nav>
    </header>
  );
}
