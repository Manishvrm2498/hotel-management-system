import { API_BASE_URL } from '../api/axiosInstance';

export const DEFAULT_HOTEL_IMAGE = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1300&q=80';
export const DEFAULT_ROOM_IMAGE = 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1200&q=80';
export const DEFAULT_USER_IMAGE = 'https://images.unsplash.com/photo-1633332755192-727a05c4013d?auto=format&fit=crop&w=400&q=80';

export function assetUrl(path, fallback) {
  if (!path) return fallback;
  if (/^https?:\/\//i.test(path) || path.startsWith('data:')) return path;
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`;
}

export function versionedAssetUrl(path, fallback, version) {
  const url = assetUrl(path, fallback);
  if (!path || !version || url === fallback) return url;
  return `${url}${url.includes('?') ? '&' : '?'}v=${version}`;
}

export function useFallbackImage(event, fallback) {
  if (event.currentTarget.src !== fallback) {
    event.currentTarget.src = fallback;
  }
}
