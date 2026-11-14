package br.com.pokedex.seed;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.PokemonTypeRepository;
import br.com.pokedex.repository.TrainerRepository;

/**
 * Popula o MongoDB com os dados do Script_Pokedex.sql ao iniciar a aplicação.
 *
 * As tabelas do script viram três coleções:
 * Pokemon + Rel_Pokemon_Type + Types -> "pokemons" (tipos embutidos pelo nome)
 * Types -> "types"
 * Trainers + Rel_Trainer_Pokemon -> "trainers" (lista pokemonIds)
 *
 * Só executa quando a coleção "pokemons" está vazia, para não apagar alterações
 * feitas pela API. Com pokedex.seed.reset=true os dados são recriados do zero.
 */
@Component
public class DataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final PokemonRepository pokemonRepository;
	private final PokemonTypeRepository typeRepository;
	private final TrainerRepository trainerRepository;
	private final Resource script;
	private final boolean enabled;
	private final boolean reset;

	public DataSeeder(PokemonRepository pokemonRepository, PokemonTypeRepository typeRepository,
			TrainerRepository trainerRepository,
			@Value("${pokedex.seed.script:classpath:Script_Pokedex.sql}") Resource script,
			@Value("${pokedex.seed.enabled:true}") boolean enabled,
			@Value("${pokedex.seed.reset:false}") boolean reset) {
		this.pokemonRepository = pokemonRepository;
		this.typeRepository = typeRepository;
		this.trainerRepository = trainerRepository;
		this.script = script;
		this.enabled = enabled;
		this.reset = reset;
	}

	@Override
	public void run(ApplicationArguments args) throws IOException {
		if (!enabled) {
			return;
		}
		if (!reset && pokemonRepository.count() > 0) {
			log.info("MongoDB já possui {} Pokémon; importação do script ignorada.", pokemonRepository.count());
			return;
		}

		Map<String, List<Map<String, String>>> tables = SqlScriptParser
				.parse(script.getContentAsString(StandardCharsets.UTF_8));

		List<PokemonType> types = buildTypes(tables);
		List<Pokemon> pokemons = buildPokemons(tables, types);
		List<Trainer> trainers = buildTrainers(tables);

		pokemonRepository.deleteAll();
		typeRepository.deleteAll();
		trainerRepository.deleteAll();

		typeRepository.saveAll(types);
		pokemonRepository.saveAll(pokemons);
		trainerRepository.saveAll(trainers);

		log.info("Script_Pokedex.sql importado: {} Pokémon, {} tipos e {} treinadores.",
				pokemons.size(), types.size(), trainers.size());
	}

	private List<PokemonType> buildTypes(Map<String, List<Map<String, String>>> tables) {
		return rows(tables, "Types").stream()
				.map(row -> new PokemonType(toInt(row.get("ID_Type")), row.get("Type")))
				.sorted(Comparator.comparing(PokemonType::getId))
				.toList();
	}

	private List<Pokemon> buildPokemons(Map<String, List<Map<String, String>>> tables, List<PokemonType> types) {
		Map<Integer, String> typeNames = new LinkedHashMap<>();
		types.forEach(type -> typeNames.put(type.getId(), type.getName()));

		// Rel_Pokemon_Type: ID_Pokemon -> nomes dos tipos, na ordem do script.
		Map<Integer, List<String>> typesByPokemon = new LinkedHashMap<>();
		for (Map<String, String> row : rows(tables, "Rel_Pokemon_Type")) {
			String typeName = typeNames.get(toInt(row.get("ID_Type")));
			if (typeName != null) {
				typesByPokemon.computeIfAbsent(toInt(row.get("ID_Pokemon")), key -> new ArrayList<>()).add(typeName);
			}
		}

		return rows(tables, "Pokemon").stream()
				.map(row -> {
					Integer id = toInt(row.get("ID_Pokemon"));
					return new Pokemon(id, row.get("Pokemon"), toInt(row.get("HP")), toInt(row.get("Attack")),
							toInt(row.get("Defense")), toInt(row.get("Special_Attack")),
							toInt(row.get("Special_Defense")), toInt(row.get("Speed")),
							typesByPokemon.getOrDefault(id, List.of()));
				})
				.sorted(Comparator.comparing(Pokemon::getId))
				.toList();
	}

	private List<Trainer> buildTrainers(Map<String, List<Map<String, String>>> tables) {
		// Rel_Trainer_Pokemon: ID_Trainer -> ids dos Pokémon do time, na ordem do script.
		Map<Integer, List<Integer>> teams = new LinkedHashMap<>();
		for (Map<String, String> row : rows(tables, "Rel_Trainer_Pokemon")) {
			teams.computeIfAbsent(toInt(row.get("ID_Trainer")), key -> new ArrayList<>()).add(toInt(row.get("ID_Pokemon")));
		}

		return rows(tables, "Trainers").stream()
				.map(row -> {
					Integer id = toInt(row.get("ID_Trainer"));
					return new Trainer(id, row.get("Trainer"), toInt(row.get("Badges")), teams.getOrDefault(id, List.of()));
				})
				.sorted(Comparator.comparing(Trainer::getId))
				.toList();
	}

	private static List<Map<String, String>> rows(Map<String, List<Map<String, String>>> tables, String table) {
		return tables.getOrDefault(table, List.of());
	}

	private static Integer toInt(String value) {
		return value == null ? null : Integer.valueOf(value.trim());
	}

}
