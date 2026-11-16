import { useEffect, useId, useState } from 'react'
import Modal from './Modal'
import TypePicker, { MAX_TYPES } from './TypePicker'
import { MAX_STAT, STATS } from '../utils/pokemon'
import '../css/PokemonForm.css'

const MAX_NAME = 30
const MAX_ID = 9999

const emptyStats = () => Object.fromEntries(STATS.map(({ key }) => [key, '']))

const toFormState = (pokemon) => ({
  id: pokemon ? String(pokemon.id) : '',
  name: pokemon?.name ?? '',
  stats: pokemon
    ? Object.fromEntries(STATS.map(({ key }) => [key, String(pokemon.stats[key])]))
    : emptyStats(),
  types: pokemon?.types ?? [],
})

const isInteger = (value) => /^\d+$/.test(String(value).trim())

export const validatePokemonForm = (form, isEditing) => {
  const errors = {}
  const name = form.name.trim()

  if (!name) errors.name = 'O nome é obrigatório.'
  else if (name.length > MAX_NAME) errors.name = `O nome deve ter no máximo ${MAX_NAME} caracteres.`

  if (!isEditing && form.id.trim() !== '') {
    const id = Number(form.id)
    if (!isInteger(form.id) || id < 1 || id > MAX_ID) {
      errors.id = `O número deve ser um inteiro entre 1 e ${MAX_ID}.`
    }
  }

  STATS.forEach(({ key, label }) => {
    const value = form.stats[key]
    if (String(value).trim() === '') errors[key] = `Informe ${label}.`
    else if (!isInteger(value) || Number(value) < 1 || Number(value) > MAX_STAT) {
      errors[key] = `Use um valor entre 1 e ${MAX_STAT}.`
    }
  })

  if (form.types.length > MAX_TYPES) errors.types = `Escolha no máximo ${MAX_TYPES} tipos.`

  return errors
}

function PokemonForm({
  pokemon = null,
  typeOptions = [],
  onSubmit,
  onClose,
  saving = false,
  serverError = null,
}) {
  const isEditing = pokemon !== null
  const [form, setForm] = useState(() => toFormState(pokemon))
  const [errors, setErrors] = useState({})
  const formId = useId()
  const titleId = `${formId}-title`

  useEffect(() => {
    if (serverError?.fields) setErrors(serverError.fields)
  }, [serverError])

  const setField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: undefined }))
  }

  const setStat = (key, value) => {
    setForm((current) => ({ ...current, stats: { ...current.stats, [key]: value } }))
    setErrors((current) => ({ ...current, [key]: undefined }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    const validationErrors = validatePokemonForm(form, isEditing)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    onSubmit({
      id: form.id.trim() === '' ? undefined : Number(form.id),
      name: form.name.trim(),
      stats: Object.fromEntries(STATS.map(({ key }) => [key, Number(form.stats[key])])),
      types: form.types,
    })
  }

  const fieldError = (field) =>
    errors[field] && (
      <span id={`${formId}-${field}-error`} className="pokemon-form__error">
        {errors[field]}
      </span>
    )

  const describedBy = (field) => (errors[field] ? `${formId}-${field}-error` : undefined)

  return (
    <Modal onClose={onClose} labelledBy={titleId} className="pokemon-form-modal">
      <form className="pokemon-form" onSubmit={handleSubmit} noValidate>
        <h2 id={titleId} className="pokemon-form__title">
          {isEditing ? `Editar ${pokemon.name}` : 'Novo Pokémon'}
        </h2>

        {serverError && (
          <p className="pokemon-form__alert" role="alert">
            {serverError.message}
          </p>
        )}

        <div className="pokemon-form__row">
          <label className="pokemon-form__field pokemon-form__field--id">
            <span className="pokemon-form__label">Número</span>
            <input
              className="pokemon-form__input"
              type="number"
              inputMode="numeric"
              min="1"
              max={MAX_ID}
              value={form.id}
              onChange={(event) => setField('id', event.target.value)}
              placeholder={isEditing ? '' : 'Automático'}
              disabled={isEditing}
              aria-invalid={Boolean(errors.id)}
              aria-describedby={describedBy('id')}
            />
            {fieldError('id')}
          </label>

          <label className="pokemon-form__field pokemon-form__field--name">
            <span className="pokemon-form__label">Nome</span>
            <input
              className="pokemon-form__input"
              type="text"
              value={form.name}
              maxLength={MAX_NAME}
              onChange={(event) => setField('name', event.target.value)}
              placeholder="Ex.: Pikachu"
              autoComplete="off"
              aria-invalid={Boolean(errors.name)}
              aria-describedby={describedBy('name')}
            />
            {fieldError('name')}
          </label>
        </div>

        <fieldset className="pokemon-form__fieldset">
          <legend className="pokemon-form__label">
            Tipos <span className="pokemon-form__hint">({form.types.length}/{MAX_TYPES}, opcional)</span>
          </legend>
          <TypePicker
            types={typeOptions}
            selected={form.types}
            onChange={(types) => setField('types', types)}
          />
          {fieldError('types')}
        </fieldset>

        <fieldset className="pokemon-form__fieldset">
          <legend className="pokemon-form__label">Atributos base (1 a {MAX_STAT})</legend>
          <div className="pokemon-form__stats">
            {STATS.map(({ key, label }) => (
              <label key={key} className="pokemon-form__field">
                <span className="pokemon-form__stat-label">{label}</span>
                <input
                  className="pokemon-form__input"
                  type="number"
                  inputMode="numeric"
                  min="1"
                  max={MAX_STAT}
                  value={form.stats[key]}
                  onChange={(event) => setStat(key, event.target.value)}
                  aria-invalid={Boolean(errors[key])}
                  aria-describedby={describedBy(key)}
                />
                {fieldError(key)}
              </label>
            ))}
          </div>
        </fieldset>

        <div className="pokemon-form__actions">
          <button type="button" className="button button--ghost" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" className="button" disabled={saving}>
            {saving ? 'Salvando...' : isEditing ? 'Salvar alterações' : 'Cadastrar'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export default PokemonForm
