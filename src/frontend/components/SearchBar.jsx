import Button from './Button';

export default function SearchBar({ filters, onChange, onSubmit, compact = false }) {
  const update = (event) => {
    onChange({ ...filters, [event.target.name]: event.target.value });
  };

  return (
    <form className={`search-panel glass ${compact ? 'search-compact' : ''}`} onSubmit={onSubmit}>
      <input name="name" placeholder="Search hotel name" value={filters.name || ''} onChange={update} />
      <input name="district" placeholder="City or district" value={filters.district || ''} onChange={update} />
      <input name="state" placeholder="State" value={filters.state || ''} onChange={update} />
      <select name="rating" value={filters.rating || ''} onChange={update}>
        <option value="">Any rating</option>
        <option value="5">5 stars</option>
        <option value="4">4+ stars</option>
        <option value="3">3+ stars</option>
      </select>
      {!compact && <input name="maxPrice" type="number" placeholder="Max room price" value={filters.maxPrice || ''} onChange={update} />}
      <Button type="submit">Search</Button>
    </form>
  );
}
