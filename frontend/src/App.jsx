import { useCallback, useMemo, useState } from 'react'
import Header from './components/Header'
import SearchBar from './components/SearchBar'
import TypeFilter from './components/TypeFilter'
import SortSelect from './components/SortSelect'
import RandomButton from './components/RandomButton'
import PokemonList from './components/PokemonList'
import PokemonDetails from './components/PokemonDetails'
import TrainerList from './components/TrainerList'
import CompareTray from './components/CompareTray'
import ComparePanel from './components/ComparePanel'
import EmptyState from './components/EmptyState'
import LoadingState from './components/LoadingState'
import Footer from './components/Footer'
import PokemonForm from './components/PokemonForm'
import { useFavorites } from './hooks/useFavorites'
import { usePokedexData } from './hooks/usePokedexData'
import { useTheme } from './hooks/useTheme'
import { createPokemon, updatePokemon } from './api/pokedexApi'
import { buildTypeIndex, fromApiPokemon, toApiPokemon } from './utils/pokemonMapper'
import { getOwners, getUsedTypes, matchesSearch, matchesType } from './utils/pokemon'
import { sortPokemon } from './utils/sort'
import './css/App.css'

function App() {
  const [view, setView] = useState('pokedex')
  const [search, setSearch] = useState('')
  const [selectedType, setSelectedType] = useState('all')
  const [selectedId, setSelectedId] = useState(null)
  const [onlyFavorites, setOnlyFavorites] = useState(false)
  const [sortBy, setSortBy] = useState('id-asc')
  const [compareIds, setCompareIds] = useState([])
  const [showCompare, setShowCompare] = useState(false)
  const { favorites, toggleFavorite } = useFavorites()
  const { theme, toggleTheme } = useTheme()
  const { pokemonList, setPokemonList, types, trainers, status, error, reload } =
    usePokedexData()

  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState(null)

  // Tipos presentes em pelo menos um Pokémon (o filtro só mostra estes).
  const availableTypes = useMemo(() => getUsedTypes(types, pokemonList), [types, pokemonList])

  // Mapa id -> Pokémon para buscas rápidas (usado nos treinadores e na comparação).
  const pokemonById = useMemo(
    () => new Map(pokemonList.map((pokemon) => [pokemon.id, pokemon])),
    [pokemonList],
  )

  // Estado derivado: a lista exibida é recalculada a cada digitação, filtro ou ordenação.
  const filteredPokemon = useMemo(() => {
    const result = pokemonList.filter(
      (pokemon) =>
        matchesSearch(pokemon, search) &&
        matchesType(pokemon, selectedType) &&
        (!onlyFavorites || favorites.includes(pokemon.id)),
    )
    return sortPokemon(result, sortBy)
  }, [pokemonList, search, selectedType, onlyFavorites, favorites, sortBy])

  const hasFilters = search !== '' || selectedType !== 'all' || onlyFavorites

  const clearFilters = () => {
    setSearch('')
    setSelectedType('all')
    setOnlyFavorites(false)
  }

  // Navegação do modal: percorre a lista filtrada (ou a completa, se o Pokémon não estiver nela).
  const navigationList = filteredPokemon.some((pokemon) => pokemon.id === selectedId)
    ? filteredPokemon
    : pokemonList
  const selectedIndex = navigationList.findIndex((pokemon) => pokemon.id === selectedId)
  const selectedPokemon = selectedIndex >= 0 ? navigationList[selectedIndex] : null

  const closeDetails = useCallback(() => setSelectedId(null), [])

  const showPrevious = useCallback(() => {
    const index = (selectedIndex - 1 + navigationList.length) % navigationList.length
    setSelectedId(navigationList[index].id)
  }, [selectedIndex, navigationList])

  const showNext = useCallback(() => {
    const index = (selectedIndex + 1) % navigationList.length
    setSelectedId(navigationList[index].id)
  }, [selectedIndex, navigationList])

  // Sorteia um Pokémon da lista atual (ou de todos, se a lista estiver vazia).
  const showRandom = () => {
    const source = filteredPokemon.length > 0 ? filteredPokemon : pokemonList
    if (source.length === 0) return
    const randomPokemon = source[Math.floor(Math.random() * source.length)]
    setSelectedId(randomPokemon.id)
  }

  // Comparação: no máximo 2 Pokémon. Ao escolher um terceiro, o mais antigo sai.
  const toggleCompare = (id) => {
    setCompareIds((current) => {
      if (current.includes(id)) return current.filter((compareId) => compareId !== id)
      return current.length < 2 ? [...current, id] : [current[1], id]
    })
  }

  const clearCompare = () => {
    setCompareIds([])
    setShowCompare(false)
  }

  const closeCompare = useCallback(() => setShowCompare(false), [])

  const comparePokemon = compareIds.map((id) => pokemonById.get(id))

  const typeOptions = useMemo(() => types.filter((type) => type.name !== 'Typeless'), [types])

  const openCreateForm = () => {
    setSaveError(null)
    setForm({ pokemon: null })
  }

  const openEditForm = useCallback((pokemon) => {
    setSaveError(null)
    setForm({ pokemon })
  }, [])

  const closeForm = useCallback(() => {
    setForm(null)
    setSaveError(null)
  }, [])

  const savePokemon = async (pokemon) => {
    const editing = form.pokemon
    setSaving(true)
    setSaveError(null)
    try {
      const body = toApiPokemon(pokemon)
      const response = editing
        ? await updatePokemon(editing.id, body)
        : await createPokemon(body)
      const saved = fromApiPokemon(response, buildTypeIndex(types))

      setPokemonList((current) =>
        [...current.filter((item) => item.id !== saved.id), saved].sort((a, b) => a.id - b.id),
      )
      setForm(null)
      setSelectedId(saved.id)
    } catch (requestError) {
      setSaveError(requestError)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className={`app ${compareIds.length > 0 ? 'app--with-tray' : ''}`}>
      <Header
        total={pokemonList.length}
        view={view}
        onChangeView={setView}
        theme={theme}
        onToggleTheme={toggleTheme}
      />

      <main className="app__content">
        {status === 'loading' && <LoadingState />}

        {status === 'error' && (
          <EmptyState title="Não foi possível carregar a Pokédex" message={error?.message}>
            <button type="button" className="button" onClick={reload}>
              Tentar novamente
            </button>
          </EmptyState>
        )}

        {status === 'ready' && view === 'trainers' && (
          <TrainerList
            trainers={trainers}
            pokemonById={pokemonById}
            onSelectPokemon={setSelectedId}
          />
        )}

        {status === 'ready' && view === 'pokedex' && (
          <>
            <section className="toolbar">
              <div className="toolbar__row">
                <SearchBar value={search} onChange={setSearch} />
                <div className="toolbar__actions">
                  <SortSelect value={sortBy} onChange={setSortBy} />
                  <button
                    type="button"
                    className={`chip-button ${onlyFavorites ? 'is-active' : ''}`}
                    onClick={() => setOnlyFavorites((current) => !current)}
                    aria-pressed={onlyFavorites}
                  >
                    ♥ Favoritos ({favorites.length})
                  </button>
                  <RandomButton onClick={showRandom} />
                  <button type="button" className="button toolbar__new" onClick={openCreateForm}>
                    + Novo Pokémon
                  </button>
                </div>
              </div>
              <TypeFilter
                types={availableTypes}
                selectedType={selectedType}
                onSelect={setSelectedType}
              />
            </section>

            <div className="results-bar">
              <p className="results-info" aria-live="polite">
                Exibindo <strong>{filteredPokemon.length}</strong> de {pokemonList.length} Pokémon
              </p>
              {hasFilters && (
                <button type="button" className="button button--ghost" onClick={clearFilters}>
                  Limpar filtros
                </button>
              )}
            </div>

            {filteredPokemon.length > 0 ? (
              <PokemonList
                pokemonList={filteredPokemon}
                onSelect={setSelectedId}
                favorites={favorites}
                onToggleFavorite={toggleFavorite}
                compareIds={compareIds}
                onToggleCompare={toggleCompare}
              />
            ) : (
              <EmptyState
                title="Nenhum Pokémon encontrado"
                message={
                  onlyFavorites && favorites.length === 0
                    ? 'Você ainda não tem favoritos. Toque no coração de um Pokémon para favoritá-lo.'
                    : 'Tente pesquisar por outro nome ou ID, ou escolha outro tipo.'
                }
              >
                <button type="button" className="button" onClick={clearFilters}>
                  Limpar filtros
                </button>
              </EmptyState>
            )}
          </>
        )}
      </main>

      <Footer />

      {compareIds.length > 0 && (
        <CompareTray
          selected={comparePokemon}
          onRemove={toggleCompare}
          onClear={clearCompare}
          onCompare={() => setShowCompare(true)}
        />
      )}

      {showCompare && comparePokemon.length === 2 && (
        <ComparePanel
          first={comparePokemon[0]}
          second={comparePokemon[1]}
          onClose={closeCompare}
        />
      )}

      {form && (
        <PokemonForm
          pokemon={form.pokemon}
          typeOptions={typeOptions}
          onSubmit={savePokemon}
          onClose={closeForm}
          saving={saving}
          serverError={saveError}
        />
      )}

      {selectedPokemon && !form && (
        <PokemonDetails
          pokemon={selectedPokemon}
          owners={getOwners(trainers, selectedPokemon.id)}
          isFavorite={favorites.includes(selectedPokemon.id)}
          onToggleFavorite={toggleFavorite}
          onEdit={openEditForm}
          onClose={closeDetails}
          onPrevious={showPrevious}
          onNext={showNext}
        />
      )}
    </div>
  )
}

export default App
