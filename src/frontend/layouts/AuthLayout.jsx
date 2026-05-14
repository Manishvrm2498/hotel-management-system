import { Outlet } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <main className="auth-layout">
      <section className="auth-art">
        <div>
          <span className="eyebrow">AR GrandStay</span>
          <h1>Luxury operations, calm booking flows.</h1>
          <p>Manage guests, rooms, bookings, and hotel discovery from one polished frontend.</p>
        </div>
      </section>
      <Outlet />
    </main>
  );
}
