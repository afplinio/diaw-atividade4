package br.com.pokedex.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.pokedex.exception.ResourceNotFoundException;
import br.com.pokedex.model.Pokemon;
import br.com.pokedex.service.PokemonService;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PokemonService pokemonService;

	private static final Pokemon PIKACHU = new Pokemon(25, "Pikachu", 35, 55, 30, 50, 40, 90, List.of("Electric"));

	@Test
	void listarComFiltroDeTipo() throws Exception {
		when(pokemonService.search(null, "Electric")).thenReturn(List.of(PIKACHU));

		mockMvc.perform(get("/api/pokemons").param("type", "Electric"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Pikachu"))
				.andExpect(jsonPath("$[0].types[0]").value("Electric"));
	}

	@Test
	void buscarInexistenteRetorna404() throws Exception {
		when(pokemonService.findById(999)).thenThrow(new ResourceNotFoundException("Pokémon #999 não encontrado."));

		mockMvc.perform(get("/api/pokemons/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Pokémon #999 não encontrado."));
	}

	@Test
	void cadastrarRetorna201ComLocation() throws Exception {
		when(pokemonService.create(any(Pokemon.class))).thenReturn(PIKACHU);

		mockMvc.perform(post("/api/pokemons")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Pikachu","hp":35,"attack":55,"defense":30,
						 "specialAttack":50,"specialDefense":40,"speed":90,"types":["Electric"]}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/pokemons/25"));
	}

	@Test
	void cadastrarComDadosInvalidosRetorna400ComOsCampos() throws Exception {
		mockMvc.perform(post("/api/pokemons")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"","hp":300,"attack":55,"defense":30,
						 "specialAttack":50,"specialDefense":40,"speed":90,"types":["Fire","Water","Grass"]}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fields.name").exists())
				.andExpect(jsonPath("$.fields.hp").value("O campo HP deve ser no máximo 255."))
				.andExpect(jsonPath("$.fields.types").exists());
	}

	@Test
	void excluirRetorna204() throws Exception {
		mockMvc.perform(delete("/api/pokemons/25")).andExpect(status().isNoContent());
		verify(pokemonService).delete(25);
	}

}
