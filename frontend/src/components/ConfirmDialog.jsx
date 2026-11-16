import { useId } from 'react'
import Modal from './Modal'
import '../css/ConfirmDialog.css'

function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  busy = false,
  onConfirm,
  onCancel,
}) {
  const titleId = useId()

  return (
    <Modal onClose={busy ? () => {} : onCancel} labelledBy={titleId} className="confirm-dialog">
      <div className="confirm-dialog__body">
        <span className="confirm-dialog__icon" aria-hidden="true">
          !
        </span>
        <h2 id={titleId} className="confirm-dialog__title">
          {title}
        </h2>
        {message && <p className="confirm-dialog__message">{message}</p>}

        <div className="confirm-dialog__actions">
          <button type="button" className="button button--ghost" onClick={onCancel} disabled={busy}>
            Cancelar
          </button>
          <button type="button" className="button button--danger" onClick={onConfirm} disabled={busy}>
            {busy ? 'Aguarde...' : confirmLabel}
          </button>
        </div>
      </div>
    </Modal>
  )
}

export default ConfirmDialog
