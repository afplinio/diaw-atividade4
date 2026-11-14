package br.com.pokedex.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.MongoTemplate;

import br.com.pokedex.exception.DuplicateResourceException;
import br.com.pokedex.exception.InvalidPokemonException;
import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.model.PokemonType;
import br.com.pokedex.model.Trainer;
import br.com.pokedex.repository.PokemonRepository;
import br.com.pokedex.repository.PokemonTypeRepository;
import br.com.pokedex.repository.TrainerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PokemonServiceTest {

	@Mock
	private PokemonRepository pokemonRepository;

	@Mock
	private PokemonTypeRepository typeRepository;

	@Mock
	private TrainerRepository trainerRepository;

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private PokemonService service;

	@BeforeEach
	void setUp() {
		when(typeRepository.findAll()).thenReturn(List.of(new PokemonType(2, "Fire"), new PokemonType(10, "Flying")));
		when(pokemonRepository.save(any(Pokemon.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	private static Pokemon pokemon(Integer id, String name, String... types) {
		return new Pokemon(id, name, 50, 50, 50, 50, 50, 50, List.of(types));
	}

	@Test
	void cadastrarSemIdUsaOProximoNumero() {
		when(pokemonRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(pokemon(151, "Mew")));

		Pokemon created = service.create(pokemon(null, "  Novo  ", "Fire", "Fire"));

		assertThat(created.getId()).isEqualTo(152);
		assertThat(created.getName()).isEqualTo("Novo");
		assertThat(created.getTypes()).containsExactly("Fire");
	}

	@Test
	void cadastrarComIdRepetidoGeraConflito() {
		when(pokemonRepository.existsById(6)).thenReturn(true);

		assertThatThrownBy(() -> service.create(pokemon(6, "Charizard", "Fire")))
				.isInstanceOf(DuplicateResourceException.class);
		verify(pokemonRepository, never()).save(any());
	}

	@Test
	void cadastrarComTipoInexistenteGeraErro() {
		assertThatThrownBy(() -> service.create(pokemon(200, "Teste", "Fogo")))
				.isInstanceOf(InvalidPokemonException.class)
				.hasMessageContaining("Fogo");
	}

	@Test
	void atualizarUsaOIdDaUrl() {
		when(pokemonRepository.findById(6)).thenReturn(Optional.of(pokemon(6, "Charizard", "Fire")));

		Pokemon updated = service.update(6, pokemon(999, "Charizard X", "Fire", "Flying"));

		assertThat(updated.getId()).isEqualTo(6);
		assertThat(updated.getName()).isEqualTo("Charizard X");
	}

	@Test
	void buscarIdInexistenteGeraNaoEncontrado() {
		when(pokemonRepository.findById(999)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findById(999)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void excluirRemoveOPokemonDoTimeDosTreinadores() {
		Pokemon pidgey = pokemon(16, "Pidgey", "Flying");
		Trainer ash = new Trainer(1, "Ash", 9, List.of(25, 16, 16));
		when(pokemonRepository.findById(16)).thenReturn(Optional.of(pidgey));
		when(trainerRepository.findByPokemonIdsContaining(16)).thenReturn(List.of(ash));

		service.delete(16);

		verify(pokemonRepository).delete(pidgey);
		assertThat(ash.getPokemonIds()).containsExactly(25);
		verify(trainerRepository).saveAll(List.of(ash));
	}

}
