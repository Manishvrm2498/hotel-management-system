export function Loader({ label = 'Loading' }) {
  return (
    <div className="loader-wrap">
      <div className="loader" />
      <span>{label}</span>
    </div>
  );
}

export function SkeletonGrid({ count = 6 }) {
  return (
    <div className="grid cards-grid">
      {Array.from({ length: count }).map((_, index) => (
        <div className="skeleton-card" key={index}>
          <span />
          <strong />
          <p />
          <p />
        </div>
      ))}
    </div>
  );
}
