import { useEffect, useState } from 'react'
import { searchPokemons } from '../api/pokedexApi'

const DEBOUNCE_MS = 300

export const isIdSearch = (search) => /^#?\d+$/.test(search.trim())

export function usePokemonSearch(search, selectedType, pokemonList) {
  const name = isIdSearch(search) ? '' : search.trim()
  const type = selectedType === 'all' ? '' : selectedType
  const hasFilter = name !== '' || type !== ''

  const [result, setResult] = useState({ key: '', ids: null, error: null })
  const key = `${name}|${type}`

  useEffect(() => {
    if (!hasFilter) return undefined

    const controller = new AbortController()
    const timer = setTimeout(() => {
      searchPokemons({ name, type }, { signal: controller.signal })
        .then((documents) =>
          setResult({ key, ids: new Set(documents.map((document) => document.id)), error: null }),
        )
        .catch((requestError) => {
          if (requestError.name === 'AbortError') return
          setResult({ key, ids: null, error: requestError })
        })
    }, name ? DEBOUNCE_MS : 0)

    return () => {
      clearTimeout(timer)
      controller.abort()
    }
    // pokemonList: refaz a busca depois de cadastrar, editar ou excluir.
  }, [hasFilter, name, type, key, pokemonList])

  if (!hasFilter) return { ids: null, searching: false, error: null }

  const isCurrent = result.key === key
  return {
    ids: isCurrent ? result.ids : null,
    searching: !isCurrent,
    error: isCurrent ? result.error : null,
  }
}
