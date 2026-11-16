import { request } from './client'

export const listPokemons = (options) => request('/pokemons', options)

export const getPokemon = (id, options) => request(`/pokemons/${id}`, options)

export const createPokemon = (pokemon) => request('/pokemons', { method: 'POST', body: pokemon })

export const updatePokemon = (id, pokemon) =>
  request(`/pokemons/${id}`, { method: 'PUT', body: pokemon })

export const deletePokemon = (id) => request(`/pokemons/${id}`, { method: 'DELETE' })

export const listTypes = (options) => request('/types', options)

export const listTrainers = (options) => request('/trainers', options)
