export default function InputField({ label, error, className = '', ...props }) {
  return (
    <label className={`field ${className}`}>
      {label && <span>{label}</span>}
      <input {...props} />
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}
