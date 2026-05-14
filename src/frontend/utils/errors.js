export function getErrorMessage(error) {
  const data = error?.response?.data;
  if (typeof data === 'string') return data;
  if (data?.message) return data.message;
  if (data?.error) return data.error;
  if (Array.isArray(data?.errors)) return data.errors.join(', ');
  return error?.message || 'Something went wrong. Please try again.';
}

export function asArray(value) {
  if (!value) return [];
  return Array.isArray(value) ? value : [value];
}
