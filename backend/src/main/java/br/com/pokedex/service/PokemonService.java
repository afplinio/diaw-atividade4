package br.com.pokedex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import br.com.pokedex.exception.DuplicateResourceException;
import br.com.pokedex.exception.InvalidPokemonException;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.PokemonTypeRepository;
import br.com.pokedex.repository.TrainerRepository;

/** Regras de negócio dos Pokémon. O controller só recebe a requisição e delega para cá. */
@Service
public class PokemonService {

	private static final Sort BY_ID = Sort.by("id");

	private final PokemonRepository pokemonRepository;
	private final PokemonTypeRepository typeRepository;
	private final TrainerRepository trainerRepository;
	private final MongoTemplate mongoTemplate;

	public PokemonService(PokemonRepository pokemonRepository, PokemonTypeRepository typeRepository,
			TrainerRepository trainerRepository, MongoTemplate mongoTemplate) {
		this.pokemonRepository = pokemonRepository;
		this.typeRepository = typeRepository;
		this.trainerRepository = trainerRepository;
		this.mongoTemplate = mongoTemplate;
	}

	public List<Pokemon> findAll() {
		return pokemonRepository.findAll(BY_ID);
	}

	/**
	 * Busca com filtros opcionais, combinados com "E":
	 * name - parte do nome, sem diferenciar maiúsculas (ex.: "pika");
	 * type - nome exato do tipo, sem diferenciar maiúsculas (ex.: "fire").
	 */
	public List<Pokemon> search(String name, String type) {
		List<Criteria> filters = new ArrayList<>();
		if (name != null && !name.isBlank()) {
			filters.add(Criteria.where("name").regex(Pattern.quote(name.trim()), "i"));
		}
		if (type != null && !type.isBlank()) {
			filters.add(Criteria.where("types").regex("^" + Pattern.quote(type.trim()) + "$", "i"));
		}
		if (filters.isEmpty()) {
			return findAll();
		}

		Query query = new Query(new Criteria().andOperator(filters)).with(BY_ID);
		return mongoTemplate.find(query, Pokemon.class);
	}

	public Pokemon findById(Integer id) {
		return pokemonRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pokémon #" + id + " não encontrado."));
	}

	/** Cadastra um Pokémon. Sem ID informado, usa o próximo número disponível. */
	public Pokemon create(Pokemon pokemon) {
		if (pokemon.getId() == null) {
			pokemon.setId(nextId());
		} else if (pokemonRepository.existsById(pokemon.getId())) {
			throw new DuplicateResourceException("Já existe um Pokémon com o número #" + pokemon.getId() + ".");
		}
		return pokemonRepository.save(normalize(pokemon));
	}

	/** Atualiza todos os dados do Pokémon. O ID da URL prevalece sobre o do corpo. */
	public Pokemon update(Integer id, Pokemon pokemon) {
		findById(id);
		pokemon.setId(id);
		return pokemonRepository.save(normalize(pokemon));
	}

	/** Remove o Pokémon e também o tira do time dos treinadores. */
	public void delete(Integer id) {
		Pokemon pokemon = findById(id);
		pokemonRepository.delete(pokemon);

		List<Trainer> trainers = trainerRepository.findByPokemonIdsContaining(id);
		trainers.forEach(trainer -> trainer.getPokemonIds().removeIf(id::equals));
		trainerRepository.saveAll(trainers);
	}

	private Integer nextId() {
		return pokemonRepository.findTopByOrderByIdDesc().map(pokemon -> pokemon.getId() + 1).orElse(1);
	}

	/** Limpa espaços, remove tipos repetidos e confere se os tipos existem na coleção "types". */
	private Pokemon normalize(Pokemon pokemon) {
		pokemon.setName(pokemon.getName() == null ? null : pokemon.getName().trim());
		pokemon.setTypes(pokemon.getTypes().stream().map(String::trim).distinct().toList());

		Set<String> validTypes = typeRepository.findAll().stream()
				.map(PokemonType::getName)
				.collect(Collectors.toSet());
		List<String> unknown = pokemon.getTypes().stream().filter(type -> !validTypes.contains(type)).toList();
		if (!unknown.isEmpty()) {
			throw new InvalidPokemonException("Tipo(s) inexistente(s): " + String.join(", ", unknown) + ".");
		}
		return pokemon;
	}

}
