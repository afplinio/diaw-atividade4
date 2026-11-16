import { getTypeColor, TYPE_LABELS } from '../utils/pokemon'
import '../css/TypePicker.css'

export const MAX_TYPES = 2

function TypePicker({ types, selected, onChange }) {
  const selectedNames = selected.map((type) => type.name)
  const isFull = selected.length >= MAX_TYPES

  const toggle = (type) => {
    if (selectedNames.includes(type.name)) {
      onChange(selected.filter((item) => item.name !== type.name))
    } else if (!isFull) {
      onChange([...selected, type])
    }
  }

  return (
    <div className="type-picker" role="group" aria-label="Tipos do Pokémon">
      {types.map((type) => {
        const active = selectedNames.includes(type.name)
        return (
          <button
            key={type.id}
            type="button"
            className={`type-picker__option ${active ? 'is-active' : ''}`}
            style={{ '--type-color': getTypeColor(type.name) }}
            onClick={() => toggle(type)}
            aria-pressed={active}
            disabled={!active && isFull}
          >
            {TYPE_LABELS[type.name] ?? type.name}
          </button>
        )
      })}
    </div>
  )
}

export default TypePicker
