import { useCallback, useEffect, useState } from 'react'
import { listPokemons, listTrainers, listTypes } from '../api/pokedexApi'
import { buildTypeIndex, fromApiPokemon } from '../utils/pokemonMapper'

export function usePokedexData() {
  const [pokemonList, setPokemonList] = useState([])
  const [types, setTypes] = useState([])
  const [trainers, setTrainers] = useState([])
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    const controller = new AbortController()
    const options = { signal: controller.signal }

    Promise.all([listPokemons(options), listTypes(options), listTrainers(options)])
      .then(([pokemonDocs, typeDocs, trainerDocs]) => {
        const typeIndex = buildTypeIndex(typeDocs)
        setPokemonList(pokemonDocs.map((document) => fromApiPokemon(document, typeIndex)))
        setTypes(typeDocs)
        setTrainers(trainerDocs)
        setStatus('ready')
      })
      .catch((requestError) => {
        if (requestError.name === 'AbortError') return
        setError(requestError)
        setStatus('error')
      })

    return () => controller.abort()
  }, [reloadKey])

  const reload = useCallback(() => {
    setStatus('loading')
    setError(null)
    setReloadKey((key) => key + 1)
  }, [])

  return { pokemonList, setPokemonList, types, trainers, setTrainers, status, error, reload }
}
