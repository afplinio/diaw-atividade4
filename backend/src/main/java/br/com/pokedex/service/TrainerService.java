package br.com.pokedex.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.TrainerRepository;

@Service
public class TrainerService {

	private final TrainerRepository trainerRepository;
	private final PokemonRepository pokemonRepository;

	public TrainerService(TrainerRepository trainerRepository, PokemonRepository pokemonRepository) {
		this.trainerRepository = trainerRepository;
		this.pokemonRepository = pokemonRepository;
	}

	public List<Trainer> findAll() {
		return trainerRepository.findAll(Sort.by("id"));
	}

	public Trainer findById(Integer id) {
		return trainerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Treinador #" + id + " não encontrado."));
	}

	/** Time completo do treinador, na ordem cadastrada (resolve a relação treinador -> Pokémon). */
	public List<Pokemon> findTeam(Integer id) {
		Trainer trainer = findById(id);
		Map<Integer, Pokemon> pokemonById = pokemonRepository.findAllById(trainer.getPokemonIds()).stream()
				.collect(Collectors.toMap(Pokemon::getId, Function.identity()));

		return trainer.getPokemonIds().stream()
				.map(pokemonById::get)
				.filter(Objects::nonNull)
				.toList();
	}

}
