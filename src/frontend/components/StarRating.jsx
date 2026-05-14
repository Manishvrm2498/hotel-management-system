export default function StarRating({ value = 0, onChange, label = 'Rating' }) {
  const roundedValue = Math.round(Number(value || 0));
  const interactive = typeof onChange === 'function';

  return (
    <div className={interactive ? 'star-rating interactive' : 'star-rating'} aria-label={`${label}: ${roundedValue} out of 5`}>
      {[1, 2, 3, 4, 5].map((star) => {
        const active = star <= roundedValue;
        if (!interactive) {
          return <span key={star} className={active ? 'star active' : 'star'} aria-hidden="true">★</span>;
        }
        return (
          <button
            key={star}
            type="button"
            className={active ? 'star active' : 'star'}
            onClick={() => onChange(star)}
            aria-label={`${star} star${star > 1 ? 's' : ''}`}
          >
            ★
          </button>
        );
      })}
    </div>
  );
}
