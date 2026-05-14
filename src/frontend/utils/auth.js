export const TOKEN_KEY = 'hotel_hms_token';
export const USER_KEY = 'hotel_hms_user';
export const THEME_KEY = 'hotel_hms_theme';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function decodeJwt(token) {
  if (!token) return null;
  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(window.atob(normalized));
  } catch {
    return null;
  }
}

export function normalizeRole(role) {
  if (!role) return 'USER';
  if (Array.isArray(role)) {
    const firstRole = role.find((item) => String(item?.authority || item).includes('ADMIN')) || role[0];
    return normalizeRole(firstRole?.authority || firstRole);
  }
  return String(role).replace('ROLE_', '').replace(/[^a-zA-Z_]/g, '').toUpperCase() || 'USER';
}

export function getInitials(user) {
  const first = user?.firstName || user?.name || user?.email || 'Guest';
  const last = user?.lastName || '';
  return `${first[0] || 'G'}${last[0] || ''}`.toUpperCase();
}
