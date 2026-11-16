import { STATS } from './pokemon'

// Converte entre o formato da API (atributos soltos, tipos por nome) e o dos componentes.

export const buildTypeIndex = (types) => new Map(types.map((type) => [type.name, type]))

export const fromApiPokemon = (document, typeIndex = new Map()) => ({
  id: document.id,
  name: document.name,
  stats: Object.fromEntries(STATS.map(({ key }) => [key, document[key] ?? 0])),
  types: (document.types ?? []).map((name) => typeIndex.get(name) ?? { id: name, name }),
})

export const toApiPokemon = (pokemon) => ({
  id: pokemon.id,
  name: pokemon.name.trim(),
  ...Object.fromEntries(STATS.map(({ key }) => [key, Number(pokemon.stats[key])])),
  types: pokemon.types.map((type) => type.name),
})
