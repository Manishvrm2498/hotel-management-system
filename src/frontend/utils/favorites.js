export const HOTEL_FAVORITES_KEY = 'hotel_favorites';

export function getFavoriteHotelIds() {
  try {
    return JSON.parse(localStorage.getItem(HOTEL_FAVORITES_KEY) || '[]').map(String);
  } catch {
    return [];
  }
}

export function saveFavoriteHotelIds(ids) {
  localStorage.setItem(HOTEL_FAVORITES_KEY, JSON.stringify(ids.map(String)));
}

export function toggleFavoriteHotel(id, currentIds) {
  const hotelId = String(id);
  const ids = currentIds.map(String);
  return ids.includes(hotelId) ? ids.filter((item) => item !== hotelId) : [...ids, hotelId];
}
