import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const links = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/hotels', label: 'Manage Hotels' },
  { to: '/admin/hotels/add', label: 'Add Hotel' },
  { to: '/admin/rooms', label: 'Manage Rooms' },
  { to: '/admin/bookings', label: 'Bookings' },
  { to: '/admin/users', label: 'Users', roles: ['SUPERADMIN'] },
];

export default function Sidebar() {
  const { role } = useAuth();
  const visibleLinks = links.filter((link) => !link.roles || link.roles.includes(role));

  return (
    <aside className="sidebar glass">
      <div className="sidebar-title">Admin Console</div>
      <nav>
        {visibleLinks.map((link) => (
          <NavLink key={link.to} to={link.to} end={link.to === '/admin'}>
            {link.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
