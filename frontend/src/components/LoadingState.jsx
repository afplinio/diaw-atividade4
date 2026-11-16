import '../css/LoadingState.css'

function LoadingState({ message = 'Carregando Pokémon...' }) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <span className="loading-state__pokeball" aria-hidden="true" />
      <p className="loading-state__message">{message}</p>
    </div>
  )
}

export default LoadingState
