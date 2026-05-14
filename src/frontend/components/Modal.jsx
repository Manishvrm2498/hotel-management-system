import Button from './Button';

export default function Modal({ open, title, children, onClose, actions }) {
  if (!open) return null;

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="modal glass" role="dialog" aria-modal="true" onMouseDown={(e) => e.stopPropagation()}>
        <button type="button" className="icon-btn modal-close" onClick={onClose} aria-label="Close modal">
          x
        </button>
        {title && <h2>{title}</h2>}
        <div>{children}</div>
        {actions && <div className="modal-actions">{actions}</div>}
      </section>
    </div>
  );
}

export function ConfirmModal({ open, title, message, onClose, onConfirm, loading, confirmVariant = 'primary' }) {
  return (
    <Modal
      open={open}
      title={title}
      onClose={onClose}
      actions={
        <>
          <Button type="button" variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          <Button type="button" variant={confirmVariant} onClick={onConfirm} disabled={loading}>
            {loading ? 'Working...' : 'Confirm'}
          </Button>
        </>
      }
    >
      <p>{message}</p>
    </Modal>
  );
}
